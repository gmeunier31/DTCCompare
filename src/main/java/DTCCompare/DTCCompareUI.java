/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DTCCompare;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import jdk.nashorn.internal.parser.TokenType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Utilisateur
 */
public class DTCCompareUI extends javax.swing.JFrame {

    /**
     * Creates new form NewJFrame
     */
    public DTCCompareUI() {
        initComponents();
        //  Define a keyword attribute for each line of text
        keyWordFile = new SimpleAttributeSet();
        StyleConstants.setBold(keyWordFile, true);
        StyleConstants.setItalic(keyWordFile, false);
        SimpleAttributeSet keyWordTotalFailures = new SimpleAttributeSet();
        StyleConstants.setBold(keyWordTotalFailures, true);
        StyleConstants.setFontSize(keyWordTotalFailures, 14);
        SimpleAttributeSet keyWordDetailledFailures = new SimpleAttributeSet();
        StyleConstants.setBold(keyWordDetailledFailures, true);
        StyleConstants.setFontSize(keyWordDetailledFailures, 10);

        doc = jTextPaneOutput.getStyledDocument();
        inRed = new SimpleAttributeSet();
        StyleConstants.setForeground(inRed, Color.red);
        inGreen = new SimpleAttributeSet();
        StyleConstants.setForeground(inGreen, Color.green);
        inBlack = new SimpleAttributeSet();
        StyleConstants.setForeground(inBlack, Color.black);
                                    
        // center the Text in the jTextPane inside the jPanel
        StyledDocument documentStyleLeft = jTextPaneLeft.getStyledDocument();
        StyledDocument documentStyleRight = jTextPaneRight.getStyledDocument();

        SimpleAttributeSet centerAttribute = new SimpleAttributeSet();
        StyleConstants.setAlignment(centerAttribute, StyleConstants.ALIGN_CENTER);
        documentStyleLeft.setParagraphAttributes(0, documentStyleLeft.getLength(), centerAttribute, false);
        documentStyleRight.setParagraphAttributes(0, documentStyleRight.getLength(), centerAttribute, false);

        // Connect the label with a drag and drop listener
        DropTarget targetL = new DropTarget(jTextPaneLeft, new DropTargetListener() {

            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
            }

            @Override
            public void drop(DropTargetDropEvent event) {
                // Accept copy drops
                event.acceptDrop(DnDConstants.ACTION_COPY);

                // Get the transfer which can provide the dropped item data
                Transferable transferable = event.getTransferable();

                // Get the data formats of the dropped item
                DataFlavor[] flavors = transferable.getTransferDataFlavors();

                // Loop through the flavors
                for (DataFlavor flavor : flavors) {
                    try {
                        // If the drop items are files
                        if (flavor.isFlavorJavaFileListType()) {
                            // Get all of the dropped files
                            List<File> files = (List) transferable.getTransferData(flavor);
                            // Loop them through
                            for (File file : files) {
                                // Print out the file path
                                System.out.println("File path is '" + file.getName() + "'.");
                                if (checkFileIsDTCFile(file, true)) {
                                    //Dtc file is ok, let's proceed
                                    dTCDocLeftIsOk=true;
                                    dTCDocLeft = new DTCDocument(file);
                                    dTCDocLeft .parseAndFill();
                                    StyledDocument doc = jTextPaneLeft.getStyledDocument();

                                    //  Add some text
                                    try {
                                        jTextPaneLeft.setText("");
                                        doc.insertString(0, "" + file.getName() + "\n\n", keyWordFile);
                                        doc.insertString(doc.getLength(), "Vehicle with " + dTCDocLeft.totalFailuresCount + " failures\n", keyWordFile);
                                        doc.insertString(doc.getLength(), "" + dTCDocLeft.currentFailuresCount + " current Failure, " + dTCDocLeft.historicalFailuresCOunt
                                                + " historical Failures\n", keyWordDetailledFailures);
                                        doc.insertString(doc.getLength(), "" + dTCDocLeft.dtcWithTestNotCompletedCount + " DTC with test not completed\n", keyWordDetailledFailures);
                                    } catch (Exception e) {
                                        System.out.println(e);
                                    }
                                    //start comparison (it is inside the function that the final decision is taken
                                    startCompareDtcFiles();
                                } else {
                                    jTextPaneLeft.setText(file.getName() + ": KO");
                                    dTCDocLeftIsOk = false;
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Print out the error stack
                        e.printStackTrace();
                    }
                }
                // Inform that the drop is complete
                event.dropComplete(true);
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
            }
        });
        DropTarget targetR = new DropTarget(jTextPaneRight, new DropTargetListener() {

            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
            }

            @Override
            public void drop(DropTargetDropEvent event) {
                // Accept copy drops
                event.acceptDrop(DnDConstants.ACTION_COPY);

                // Get the transfer which can provide the dropped item data
                Transferable transferable = event.getTransferable();

                // Get the data formats of the dropped item
                DataFlavor[] flavors = transferable.getTransferDataFlavors();

                // Loop through the flavors
                for (DataFlavor flavor : flavors) {

                    try {

                        // If the drop items are files
                        if (flavor.isFlavorJavaFileListType()) {

                            // Get all of the dropped files
                            List<File> files = (List) transferable.getTransferData(flavor);

                            // Loop them through
                            for (File file : files) {

                                // Print out the file path
                                System.out.println("File path is '" + file.getName() + "'.");
                                if (checkFileIsDTCFile(file,false)){
                                    //it is a DTC file, let's proceed
                                    dTCDocRightIsOk=true;
                                    dTCDocRight = new DTCDocument(file);
                                    dTCDocRight.parseAndFill();
                                    StyledDocument doc = jTextPaneRight.getStyledDocument();
                                 
                                    //  Add some text to the drop text to list the total failures
                                    try {
                                        jTextPaneRight.setText("");
                                        doc.insertString(0, ""+file.getName()+"\n\n", keyWordFile);
                                        doc.insertString(doc.getLength(), "Vehicle with "+dTCDocRight.totalFailuresCount+" failures\n", keyWordFile);
                                        doc.insertString(doc.getLength(), ""+dTCDocRight.currentFailuresCount+" current Failure, "+dTCDocRight.historicalFailuresCOunt+
                                                " historical Failures\n", keyWordDetailledFailures);
                                        doc.insertString(doc.getLength(), ""+dTCDocRight.dtcWithTestNotCompletedCount+" DTC with test not completed\n", keyWordDetailledFailures);
                                    } catch (Exception e) {
                                        System.out.println(e);
                                    }
                                    //start comparison (it is inside the function that the final decision is taken
                                    startCompareDtcFiles();
                                } else {
                                    jTextPaneRight.setText(file.getName()+": KO");
                                    dTCDocRightIsOk=false;
                                }
                            }

                        }

                    } catch (Exception e) {

                        // Print out the error stack
                        e.printStackTrace();

                    }
                }

                // Inform that the drop is complete
                event.dropComplete(true);
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
            }
        });
    }
    /**
     * This method is called to verify dropped file is an .html file and its
     * content is DTC compatible.
     * @param file
     * @return 
     */
    boolean checkFileIsDTCFile(File file, boolean isFromLeft){
        try {
            String extension = "";
            boolean isADTCFile = false;
            extension = getFileExtension(file.getPath());
            if (isFromLeft){
                DTCLeft = (Document) Jsoup.parse(file, "UTF-8", "");
                Element summary = DTCLeft.getElementById("summary");
                if (summary != null) {
                    isADTCFile = true;
                }
            } else {
                DTCRight = (Document) Jsoup.parse(file, "UTF-8", "");
                Element summary = DTCRight.getElementById("summary");
                if (summary != null) {
                    isADTCFile = true;
                }
            }

            return (isADTCFile & isADTCFile & extension.equals("html"));
        } catch (IOException ex) {
            Logger.getLogger(DTCCompareUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    /**
     * This method is called by checkFileIsDTC File
     *
     * @param fileName
     * @return
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName must not be null!");
        }
        String extension = "";

        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            extension = fileName.substring(index + 1);
        }
        return extension;
    }

    /**
     * 
     * @param ecuLeft
     * @param ecuRight 
     */
    public void displayECU(ECU ecuLeft, ECU ecuRight) {
        try {

            doc.insertString(doc.getLength(), ecuLeft.name + ": ", keyWordFile);
            switch (ecuLeft.failuresStatus) {
                case FailuresDetected:
                    doc.insertString(doc.getLength(), "" + ecuLeft.failuresStatus, inRed);
                    break;
                case NoFailureDetected:
                    doc.insertString(doc.getLength(), "" + ecuLeft.failuresStatus, inGreen);
                    break;
                default:
                    doc.insertString(doc.getLength(), "" + ecuLeft.failuresStatus, inBlack);
                    break;
            }
            switch (ecuRight.failuresStatus) {
                case FailuresDetected:
                    doc.insertString(doc.getLength(), " --> " + ecuRight.failuresStatus + "\n", inRed);
                    break;
                case NoFailureDetected:
                    doc.insertString(doc.getLength(), " --> " + ecuRight.failuresStatus + "\n", inGreen);
                    break;
                default:
                    doc.insertString(doc.getLength(), " --> " + ecuRight.failuresStatus + "\n", inBlack);
                    break;
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(DTCCompareUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * this method is called when the two files are ok and have been dropped
     */
    public void startCompareDtcFiles() throws BadLocationException {
        boolean noMatch = true;

        if (dTCDocLeftIsOk & dTCDocRightIsOk) {
            //dTCDocLeftIsOk = false;
            //dTCDocRightIsOk = false;
            System.out.println("Start comparing files");
            /* check the two DTC files have the same list of DTC
               If it is not the case, print
               ECU xxx was present in File 1 but not listed in File 2
             */
            for (ECU ecuFromLeft : dTCDocLeft.ecuList) {
                for (ECU ecuFromRight : dTCDocRight.ecuList) {
                    if (!ecuFromLeft.name.equals(ecuFromRight.name)) {
                        noMatch = true;
                    } else {
                        //System.out.println("" + ecuFromLeft.name + " is present in both files");
                        noMatch = false;
                        break;
                    }
                }
                if (noMatch) {
                    System.out.println("" + ecuFromLeft.name + " is  present in " + dTCDocLeft.fileName
                            + " but not in " + dTCDocRight.fileName);
                }
            }
            /* ECU yyy was  present in File 2 and is not present in File 1 */
            for (ECU ecuFromRight : dTCDocRight.ecuList) {
                for (ECU ecuFromLeft : dTCDocLeft.ecuList) {
                    if (!ecuFromRight.name.equals(ecuFromLeft.name)) {
                        noMatch = true;
                    } else {
                        //System.out.println("" + ecuFromLeft.name + " is present in both files");
                        noMatch = false;
                        break;
                    }
                }
                if (noMatch) {
                    System.out.println("" + ecuFromRight.name + " is  present in " + dTCDocRight.fileName
                            + " but not in " + dTCDocLeft.fileName);
                }
            }

            /* Then check the change in ECU failure status to print only when
               there is a change
               ECU xxx No error reported --> Failures detected
               ECU yyy No error reported --> Under test
               ECU zzz No Failures detected --> No error reported
               ECU aaa No Failures detected --> Under test
               ECU bbb No Under test --> No error reported
               ECU ccc No Under test --> Failures detected
             */
            //StyledDocument doc = jTextPaneOutput.getStyledDocument();

            for (ECU ecuLeft : dTCDocLeft.ecuList) {
                for (ECU ecuRight : dTCDocRight.ecuList) {
                    //detect different failure type for the ECU between left and right
                    if ((ecuRight.name.equals(ecuLeft.name)) &&
                            (!ecuLeft.failuresStatus.equals(ecuRight.failuresStatus))) {
                        System.out.println("ECU " + ecuLeft.name + ": " + ecuLeft.failuresStatus + " --> " + ecuRight.failuresStatus);
                        //Display only if the Check box of ECU diff is selected
                        if (displayDiffOnECU) {
                            //display ECU: failure type Left --> failure type right
                            displayECU(ecuLeft,ecuRight);
                        }  else {
                            jTextPaneOutput.setText("");
                        }

                    } else if (displayDiffOnDTC && ecuRight.name.equals(ecuLeft.name) &&
                            (ecuRight.hasFailures && ecuLeft.hasFailures)) {
                        //display ECU: failure type Left --> failure type right
                        displayECU(ecuLeft,ecuRight);
                        for (DTC dtcRight : ecuRight.DtcList) {
                            for (DTC dtcLeft : ecuLeft.DtcList) {
                                //detect Historical-->Current failure or Current Failure --> Historical on a common DTC
                                if (dtcLeft.name.equals(dtcRight.name) && !dtcLeft.type.equals(dtcRight.type)) {                                 
                                    doc.insertString(doc.getLength(), dtcRight.name + ": ", keyWordFile);
                                    if (dtcLeft.isCurrentFailure) {
                                        doc.insertString(doc.getLength(), dtcLeft.type + " --> ", inRed);
                                    }
                                    if (dtcLeft.isHistoricalFailure) {
                                        doc.insertString(doc.getLength(), dtcLeft.type + " --> ", inBlack);
                                    }
                                    if (dtcRight.isCurrentFailure) {
                                        doc.insertString(doc.getLength(), dtcRight.type + "\n", inRed);
                                    }
                                    if (dtcRight.isHistoricalFailure) {
                                        doc.insertString(doc.getLength(), dtcRight.type + "\n", inBlack);
                                    }
                                }
                            }
                        }
                    } //now, check the changes of DTC inside the ECU if it has issues. compare right versus left
                    // if ECU has no errors and before it had no error or was under test, display ll its DTC
                    else if (displayDiffOnDTC && ecuRight.name.equals(ecuLeft.name) &&
                            (ecuRight.hasFailures && (ecuLeft.hasNoFailures || ecuLeft.isUnderTest))) {
                       //display ECU: failure type Left --> failure type right
                        displayECU(ecuLeft, ecuRight);
                        for (DTC dtc : ecuRight.DtcList) {
                            doc.insertString(doc.getLength(), "" + dtc.name + " Not present --> " + dtc.type + "\n", inBlack);
                        }
                    } // if ECU right has error and it has also error in left, then compare status

                    //System.out.println("ECU Left" + ecuLeft.name + ": " + ecuLeft.failuresStatus + " --> ECU Right "+ecuRight.name + ": "+ecuRight.failuresStatus);
                }
            }
        }     
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelRight = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPaneRight = new javax.swing.JTextPane();
        jPanelLeft = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPaneLeft = new javax.swing.JTextPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPaneOutput = new javax.swing.JTextPane();
        jCheckBoxECU = new javax.swing.JCheckBox();
        jCheckBoxDTC = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanelRight.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, java.awt.Color.white, null, null));

        jTextPaneRight.setEditable(false);
        jTextPaneRight.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        jTextPaneRight.setText("Drop DTC File 2 here!");
        jScrollPane1.setViewportView(jTextPaneRight);

        javax.swing.GroupLayout jPanelRightLayout = new javax.swing.GroupLayout(jPanelRight);
        jPanelRight.setLayout(jPanelRightLayout);
        jPanelRightLayout.setHorizontalGroup(
            jPanelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRightLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelRightLayout.setVerticalGroup(
            jPanelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRightLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelLeft.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, java.awt.Color.white, null, null));

        jTextPaneLeft.setEditable(false);
        jTextPaneLeft.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        jTextPaneLeft.setText("Drop DTC File1 here");
        jScrollPane2.setViewportView(jTextPaneLeft);

        javax.swing.GroupLayout jPanelLeftLayout = new javax.swing.GroupLayout(jPanelLeft);
        jPanelLeft.setLayout(jPanelLeftLayout);
        jPanelLeftLayout.setHorizontalGroup(
            jPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLeftLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelLeftLayout.setVerticalGroup(
            jPanelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLeftLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, java.awt.Color.white, null, null));
        jScrollPane3.setViewportView(jTextPaneOutput);

        jCheckBoxECU.setText("Diff on ECU");
        jCheckBoxECU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxECUActionPerformed(evt);
            }
        });

        jCheckBoxDTC.setText("Diff on DTC");
        jCheckBoxDTC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDTCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanelLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jCheckBoxECU)
                                .addGap(26, 26, 26)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(36, 36, 36)
                                .addComponent(jCheckBoxDTC)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxDTC)
                    .addComponent(jCheckBoxECU))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 550, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxECUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxECUActionPerformed

        displayDiffOnECU = jCheckBoxECU.isSelected();
        jTextPaneOutput.setText("");

        try {
            startCompareDtcFiles();
        } catch (BadLocationException ex) {
            Logger.getLogger(DTCCompareUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jCheckBoxECUActionPerformed

    private void jCheckBoxDTCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDTCActionPerformed
        // TODO add your handling code here:
        displayDiffOnDTC = jCheckBoxDTC.isSelected();
                jTextPaneOutput.setText("");

        try {
            startCompareDtcFiles();
        } catch (BadLocationException ex) {
            Logger.getLogger(DTCCompareUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jCheckBoxDTCActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DTCCompareUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DTCCompareUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DTCCompareUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DTCCompareUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DTCCompareUI().setVisible(true);
                System.out.println("File path is '.");
            }
        });
    }
    // my variables
    private Document DTCLeft;
    private Document DTCRight;
    private DTCDocument dTCDocLeft;
    private DTCDocument dTCDocRight;
    private boolean dTCDocLeftIsOk=false;
    private boolean dTCDocRightIsOk=false;
    private boolean displayDiffOnECU=false;
    private boolean displayDiffOnDTC=false;
    private final StyledDocument doc;
    private final SimpleAttributeSet inRed;
    private final SimpleAttributeSet inGreen;
    private final SimpleAttributeSet inBlack;
    private SimpleAttributeSet keyWordFile;

           
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxDTC;
    private javax.swing.JCheckBox jCheckBoxECU;
    private javax.swing.JPanel jPanelLeft;
    private javax.swing.JPanel jPanelRight;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextPane jTextPaneLeft;
    private javax.swing.JTextPane jTextPaneOutput;
    private javax.swing.JTextPane jTextPaneRight;
    // End of variables declaration//GEN-END:variables


}
