/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DTCCompare;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
        // by default report is in English
        reportLanguage="English";
        //  Define a keyword attribute for each line of text
        Title = new SimpleAttributeSet();
        StyleConstants.setBold(Title, true);
        StyleConstants.setItalic(Title, false);
        StyleConstants.setFontSize(Title, 12);
        
        EcuStyle = new SimpleAttributeSet();
        StyleConstants.setBold(EcuStyle, true);
        StyleConstants.setItalic(EcuStyle, false);
        StyleConstants.setFontSize(EcuStyle, 14);
        StyleConstants.setForeground(EcuStyle, Color.blue);

        DtcStyle = new SimpleAttributeSet();
        StyleConstants.setBold(DtcStyle, false);
        StyleConstants.setItalic(DtcStyle, true);
        StyleConstants.setFontSize(DtcStyle, 12);
        StyleConstants.setForeground(DtcStyle, Color.black);
        
        inRed = new SimpleAttributeSet();
        StyleConstants.setForeground(inRed, Color.red);
        inOrange = new SimpleAttributeSet();
        StyleConstants.setForeground(inOrange, Color.orange);
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
                                // Print out the file name
                                System.out.println("File name is '" + file.getName() + "'.");
                                System.out.println("File path is '" + file.getAbsolutePath() + "'.");
                                System.out.println("File path is '" + file.getParent()+ "'.");


                                if (checkFileIsDTCFile(file, true)) {
                                    //Dtc file is ok, let's proceed
                                    dTCDocLeftIsOk=true;
                                    dTCDocLeft = new DTCDocument(file);
                                    dTCDocLeft .parseAndFill();
                                    StyledDocument doc = jTextPaneLeft.getStyledDocument();

                                    //  Add some text
                                    try {
                                        jTextPaneLeft.setText("");
                                        doc.insertString(0, "" + file.getName() + "\n\n", Title);
                                        doc.insertString(doc.getLength(), ""+dTCDocLeft.title+"\n", Title);

                                        /*
                                        doc.insertString(doc.getLength(), "Vehicle with " + dTCDocLeft.totalFailuresCount + " failures\n", Title);
                                        doc.insertString(doc.getLength(), "" + dTCDocLeft.currentFailuresCount + " current Failure, " + dTCDocLeft.historicalFailuresCOunt
                                                + " historical Failures\n", keyWordDetailledFailures);
                                        doc.insertString(doc.getLength(), "" + dTCDocLeft.dtcWithTestNotCompletedCount + " DTC with test not completed\n", keyWordDetailledFailures);
                                        */
                                    } catch (Exception e) {
                                        System.out.println(e);
                                    }
                                    //start comparison (it is inside the function that the final decision is taken
                                    compareDtcFiles();
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
                                /*System.out.println("File path is '" + file.getName() + "'.");*/
                                if (checkFileIsDTCFile(file,false)){
                                    //it is a DTC file, let's proceed
                                    dTCDocRightIsOk=true;
                                    dTCDocRight = new DTCDocument(file);
                                    dTCDocRight.parseAndFill();
                                    StyledDocument doc = jTextPaneRight.getStyledDocument();
                                 
                                    //  Add some text to the drop text to list the total failures
                                    try {
                                        jTextPaneRight.setText("");
                                        doc.insertString(0, ""+file.getName()+"\n\n", Title);                                        
                                        doc.insertString(doc.getLength(), ""+dTCDocRight.title+"\n", Title);

                                        /*doc.insertString(doc.getLength(), "Vehicle with "+dTCDocRight.totalFailuresCount+" failures\n", keyWordFile);
                                        doc.insertString(doc.getLength(), ""+dTCDocRight.currentFailuresCount+" current Failure, "+dTCDocRight.historicalFailuresCOunt+
                                                " historical Failures\n", keyWordDetailledFailures);
                                        doc.insertString(doc.getLength(), ""+dTCDocRight.dtcWithTestNotCompletedCount+" DTC with test not completed\n", keyWordDetailledFailures);*/
                                    } catch (Exception e) {
                                        System.out.println(e);
                                    }
                                    //start comparison (it is inside the function that the final decision is taken
                                    compareDtcFiles();
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
     * @param str
     * @return 
     */
    private String translate (TypeDtcFailure str){
        String convertedString = "";
        switch (reportLanguage) {
            case "French":
                if (str.equals(TypeDtcFailure.CurrentFailure)) {
                    convertedString = "Panne présente";
                }
                if (str.equals(TypeDtcFailure.HistoricalFailure)) {
                    convertedString = "Panne mémorisée";
                }
                break;
            case "English":
                if (str.equals(TypeDtcFailure.CurrentFailure)) {
                    convertedString = "Current Failure";
                }
                if (str.equals(TypeDtcFailure.HistoricalFailure)) {
                    convertedString = "Historical Failure";
                }
                break;
            default:
                convertedString = "";
        }
        return convertedString;    
    }
    
    /**
     *
     * @param str
     * @return
     */
    private String translate (TypeEcuFailure str){
        String convertedString = "";
        switch (reportLanguage) {
            case "French":
                if (str.equals(TypeEcuFailure.FailuresDetected)) {
                    convertedString = "Pannes détectées";
                }
                if (str.equals(TypeEcuFailure.UnderTest)) {
                    convertedString = "en cours de test";
                }
                if (str.equals(TypeEcuFailure.NoFailureDetected)) {
                    convertedString = "Aucune panne détectée";
                }
                break;
            case "English":
                if (str.equals(TypeEcuFailure.FailuresDetected)) {
                    convertedString = "Failures detected";
                }
                if (str.equals(TypeEcuFailure.UnderTest)) {
                    convertedString = "under test";
                }
                if (str.equals(TypeEcuFailure.NoFailureDetected)) {
                    convertedString = "No failure detected";
                }
                break;
            default:
                convertedString = "";
        }
        return convertedString;    
    }
    /**
     * 
     * @param ecuLeft
     * @param ecuRight 
     */
    public void displayECU(ECU ecuLeft, ECU ecuRight) {
        try {
            StyledDocument doc = jTextPaneOutput.getStyledDocument();

            doc.insertString(doc.getLength(), ecuLeft.name + ": ", EcuStyle);
            switch (ecuLeft.failuresStatus) {
                case FailuresDetected:
                    doc.insertString(doc.getLength(), "" + translate(ecuLeft.failuresStatus), inRed);
                    break;
                case NoFailureDetected:
                    doc.insertString(doc.getLength(), "" + translate(ecuLeft.failuresStatus), inGreen);
                    break;
                default:
                    doc.insertString(doc.getLength(), "" + translate(ecuLeft.failuresStatus), inBlack);
                    break;
            }
            switch (ecuRight.failuresStatus) {
                case FailuresDetected:
                    doc.insertString(doc.getLength(), " --> " + translate(ecuRight.failuresStatus) + "\n", inRed);
                    break;
                case NoFailureDetected:
                    doc.insertString(doc.getLength(), " --> " + translate(ecuRight.failuresStatus) + "\n", inGreen);
                    break;
                default:
                    doc.insertString(doc.getLength(), " --> " + translate(ecuRight.failuresStatus) + "\n", inBlack);
                    break;
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(DTCCompareUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * this method is called when the two files are ok and have been dropped
     */
    public void compareDtcFiles() throws BadLocationException {
        boolean noMatch = true;
            StyledDocument doc = jTextPaneOutput.getStyledDocument();

        if (dTCDocLeftIsOk & dTCDocRightIsOk) {
            /* check the two DTC files have the same list of DTC
               If it is not the case, print
               ECU xxx was present in File 1 but not listed in File 2
             */
            jTextPaneOutput.setText("");
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
                    System.out.println("" + ecuFromLeft.name + " is  present in " + dTCDocLeft.file.getName()
                            + " but not in " + dTCDocRight.file.getName());
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
                    System.out.println("" + ecuFromRight.name + " is  present in " + dTCDocRight.file.getName()
                            + " but not in " + dTCDocLeft.file.getName());
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
            if (displayDiffOnECU) {
                for (ECU ecuLeft : dTCDocLeft.ecuList) {
                    for (ECU ecuRight : dTCDocRight.ecuList) {
                        // Enter here on a ECU that is present on Left and Right doc
                        if (ecuRight.name.equals(ecuLeft.name)) {
                            //detect NO Failure or Under test --> Failures
                            if ((ecuLeft.hasNoFailures || ecuLeft.isUnderTest) && ecuRight.hasFailures) {
                                ecuRight.hasStatusEvolution=true;
                                //display ECU
                                displayECU(ecuLeft, ecuRight);
                                //then display all DTC of the right ECU displayDiffOnDTC
                                if (displayDiffOnDTC) {
                                    for (DTC dtc : ecuRight.dtcList) {
                                        dtc.isNew=true;
                                        doc.insertString(doc.getLength(), dtc.name + " : ", DtcStyle);
                                        if (reportLanguage.equals("English"))
                                            doc.insertString(doc.getLength(), " Not present --> ", inGreen);
                                        else if (reportLanguage.equals("French"))
                                            doc.insertString(doc.getLength(), " Absent --> ", inGreen);
                                            
                                        if (dtc.isHistoricalFailure)
                                            doc.insertString(doc.getLength(), translate(dtc.type) + "\n", inOrange);
                                        else if (dtc.isCurrentFailure) {
                                            doc.insertString(doc.getLength(), translate(dtc.type) + "\n", inRed);
                                        }
                                    }
                                }
                            }
                            //detect Failures --> Failures
                            Boolean match=false;
                            if (ecuLeft.hasFailures && ecuRight.hasFailures) {
                                //display ECU only for ECU where DTC have changed or new
                                for (DTC dtcRight : ecuRight.dtcList) {
                                    match=false;
                                    for (DTC dtcLeft : ecuLeft.dtcList) {
                                        if (dtcLeft.name.equals(dtcRight.name)) {
                                            match=true;
                                            //detect Historical-->Current failure or Current Failure --> Historical on a common DTC
                                            if (!dtcLeft.type.equals(dtcRight.type)) {
                                                ecuRight.hasDtcEvolution=true;
                                                dtcRight.fromCurrentToHistorical=(dtcLeft.isCurrentFailure && 
                                                        dtcRight.isHistoricalFailure);
                                                dtcRight.fromHistoricalToCurrent=(dtcLeft.isHistoricalFailure && 
                                                        dtcRight.isCurrentFailure);
                                            }
                                            break;
                                        }
                                        else
                                            match=false;
                                    }
                                    if (!match)
                                        ecuRight.hasDtcEvolution=true;
                                }

                                //display ECUs that have evolution of their DTC or new DTC.                                                              
                                if (ecuRight.hasDtcEvolution)
                                    displayECU(ecuLeft, ecuRight);
                                //then display only DTC that have changed between Left and Right
                                if (displayDiffOnDTC) {                                    
                                    for (DTC dtcRight : ecuRight.dtcList) {
                                        match=false;
                                        for (DTC dtcLeft : ecuLeft.dtcList) {                                            
                                            //detect Historical-->Current failure or Current Failure --> Historical on a common DTC
                                            if (dtcLeft.name.equals(dtcRight.name)){
                                                match = true;
                                                if (!dtcLeft.type.equals(dtcRight.type)) {                                                    
                                                    doc.insertString(doc.getLength(), "* "+dtcRight.name + ":", DtcStyle);                                                    
                                                    if (dtcLeft.isCurrentFailure) {
                                                        doc.insertString(doc.getLength(), translate(dtcLeft.type) + " --> ", inRed);
                                                    }
                                                    if (dtcLeft.isHistoricalFailure) {
                                                        doc.insertString(doc.getLength(), translate(dtcLeft.type) + " --> ", inOrange);
                                                    }
                                                    if (dtcRight.isCurrentFailure) {
                                                        doc.insertString(doc.getLength(), translate(dtcRight.type) + "\n", inRed);
                                                    }
                                                    if (dtcRight.isHistoricalFailure) {
                                                        doc.insertString(doc.getLength(), translate(dtcRight.type) + "\n", inOrange);
                                                    }
                                                    dtcRight.fromCurrentToHistorical = (dtcLeft.isCurrentFailure
                                                            && dtcRight.isHistoricalFailure);
                                                    dtcRight.fromHistoricalToCurrent = (dtcLeft.isHistoricalFailure
                                                            && dtcRight.isCurrentFailure);
                                                }
                                                if (dtcLeft.isCurrentFailure && dtcRight.isCurrentFailure){
                                                    match = true;
                                                }
                                            }
                                        }
                                        //if DTC not found on left but present on right
                                        if (!match) {
                                            doc.insertString(doc.getLength(), dtcRight.name + " : ", DtcStyle);
                                            dtcRight.isNew=true;
                                            if (reportLanguage.equals("English")) {
                                                doc.insertString(doc.getLength(), " Not present --> ", inGreen);
                                            } else if (reportLanguage.equals("French")) {
                                                doc.insertString(doc.getLength(), " Absent --> ", inGreen);
                                            }
                                            if (dtcRight.isHistoricalFailure) {
                                                doc.insertString(doc.getLength(), translate(dtcRight.type) + "\n", inOrange);
                                            } else if (dtcRight.isCurrentFailure) {
                                                doc.insertString(doc.getLength(), translate(dtcRight.type) + "\n", inRed);
                                            }                                            
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                jTextPaneOutput.setText("");
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
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jCheckBoxECU = new javax.swing.JCheckBox();
        jCheckBoxDTC = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jToggleButtonFrench = new javax.swing.JToggleButton();
        jToggleButtonEnglish = new javax.swing.JToggleButton();
        jTextFieldTag = new javax.swing.JTextField();
        jButtonGenerateHtmlReport = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanelRight.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, java.awt.Color.white, null, null));

        jTextPaneRight.setEditable(false);
        jTextPaneRight.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        jTextPaneRight.setText("Drop DTC File 2 here!");
        jTextPaneRight.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextPaneRightMouseClicked(evt);
            }
        });
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
        jTextPaneLeft.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextPaneLeftMouseClicked(evt);
            }
        });
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

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel2.setText("Select what to see on report");

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

        jLabel1.setText("Select report language");

        jToggleButtonFrench.setText("Français");
        jToggleButtonFrench.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonFrenchActionPerformed(evt);
            }
        });

        jToggleButtonEnglish.setSelected(true);
        jToggleButtonEnglish.setText("English");
        jToggleButtonEnglish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonEnglishActionPerformed(evt);
            }
        });

        jTextFieldTag.setFont(new java.awt.Font("sansserif", 2, 10)); // NOI18N
        jTextFieldTag.setText("Enter Tag to be present in htlm report");
        jTextFieldTag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTagActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(83, 83, 83)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jCheckBoxECU)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCheckBoxDTC)
                        .addGap(49, 49, 49)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldTag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jToggleButtonFrench)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jToggleButtonEnglish)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxECU)
                    .addComponent(jCheckBoxDTC)
                    .addComponent(jToggleButtonFrench)
                    .addComponent(jToggleButtonEnglish))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(jTextFieldTag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jButtonGenerateHtmlReport.setText("Generate HTML");
        jButtonGenerateHtmlReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGenerateHtmlReportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 466, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jPanelLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jPanelRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(184, 184, 184)
                        .addComponent(jButtonGenerateHtmlReport)))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelLeft, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelRight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonGenerateHtmlReport)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 550, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxECUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxECUActionPerformed

        displayDiffOnECU = jCheckBoxECU.isSelected();
        jTextPaneOutput.setText("");

        try {
            compareDtcFiles();
        } catch (BadLocationException ex) {
            Logger.getLogger(DTCCompareUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jCheckBoxECUActionPerformed

    private void jCheckBoxDTCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDTCActionPerformed
        // TODO add your handling code here:
        displayDiffOnDTC = jCheckBoxDTC.isSelected();
        jTextPaneOutput.setText("");

        try {
            compareDtcFiles();
        } catch (BadLocationException ex) {
            Logger.getLogger(DTCCompareUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jCheckBoxDTCActionPerformed

    private void jToggleButtonFrenchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonFrenchActionPerformed
        // TODO add your handling code here:
        jToggleButtonEnglish.setSelected(false);
        reportLanguage = "French";
        try {
            compareDtcFiles();
        } catch (BadLocationException ex) {
            Logger.getLogger(DTCCompareUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jToggleButtonFrenchActionPerformed

    private void jToggleButtonEnglishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonEnglishActionPerformed
        // TODO add your handling code here:
        jToggleButtonFrench.setSelected(false);
        reportLanguage = "English";
        try {
            compareDtcFiles();
        } catch (BadLocationException ex) {
            Logger.getLogger(DTCCompareUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jToggleButtonEnglishActionPerformed

    private void jTextFieldTagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTagActionPerformed
        // TODO add your handling code here:
        tagForHtmlreport=jTextFieldTag.getText();
    }//GEN-LAST:event_jTextFieldTagActionPerformed

    /**
     * Generate the html file. 
     * First, duplicate the file droped on Right drop box
     * Second, insert the html tag for ECU and DTC in the duplicated file
     * Third, search for ECU whose status has changed and put the label
     * Fourth, same thing for DTC
     * @param evt 
     */
    private void jButtonGenerateHtmlReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGenerateHtmlReportActionPerformed
        // TODO add your handling code here:
        JOptionPane pane;
        
        if(dTCDocLeftIsOk && dTCDocRightIsOk){
            
            pane = new JOptionPane("The TAG: " + tagForHtmlreport + " will be added in " + dTCDocRight.file.getName() + "\n");
            d = pane.createDialog((JFrame) null, "Generate HTML report");           
        } else {
            pane = new JOptionPane("Drop 2 html files first! \n");
            d = pane.createDialog((JFrame) null, "Generate HTML report");
            System.out.println("Test");
        }

        d.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.out.println("jdialog window closed event received");
            }

            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("jdialog window closing event received");
            }
        });

        d.addComponentListener(new ComponentListener() {
            @Override
            public void componentHidden(ComponentEvent e) {
                try {
                    System.out.println("dialog hidden");
                    //check output file for html report already exists, if yes, delete it
                    String aa =dTCDocRight.file.getParent()+"\\"+
                            FilenameUtils.removeExtension(dTCDocRight.file.getName())+
                            "_DTCCompare.html";
                    File htmlReport = new File(aa);

                    if (htmlReport.exists() && htmlReport.isFile()) {
                        htmlReport.delete();
                    }
                    htmlReport.createNewFile();
                    //copyFileUsingStream(dTCDocRight.file,htmlReport);
                    //now, parse output file and insert the TAG definition
                    String tagECUStyle = ".mytagecu    { color:  #ffffff; background-color: #6600ff; font-size: 90%; font-style: italic; font-weight: bold; border:1px solid #ffffff; padding-left:4px; padding-right:4px;}";
                    String tagDTCStyle= ".mytagdtc    { font-size: 90%; font-weight: bold ; color: #6600ff;}";
                    String tagECU =	"&nbsp;<SPAN class=mytagecu>"+tagForHtmlreport+"</SPAN>";
                    String tagDTCFromCurrentToHistorical =    "&nbsp;<SPAN class=mytagdtc>"+tagForHtmlreport+": Current --> Historical</SPAN>";
                    String tagDTCFromHistoricaltoCurrent =    "&nbsp;<SPAN class=mytagdtc>"+tagForHtmlreport+": Historical --> Current</SPAN>";
                    String tagDTCNew =    "&nbsp;<SPAN class=mytagdtc>"+tagForHtmlreport+": absent --> New</SPAN>";

                    final Scanner scanner = new Scanner(dTCDocRight.file);
                    Writer output;
                    output = new BufferedWriter(new FileWriter(htmlReport,true));
                    
                    Pattern underTestPattern = Pattern.compile("\\.underTest\\s*\\{\\s*color\\:");
                    Pattern ecuPattern = Pattern.compile("(\\<LI\\s*class\\=tdmecu.*\\>)(.*)(\\s+\\-\\s+\\<B.*)");
                    Pattern dtcPattern = Pattern.compile("(\\<LI\\s*class\\=tdmdev.*\\>)(.*)(\\<\\/A.*)(\\<\\/FONT.*)");
                    Matcher underTestMatcher;
                    Matcher ecuMatcher;
                    Matcher dtcMatcher;
                    ECU ecuFromDocRight = new ECU();
                    DTC dtcFromEcuRight = new DTC();
                    while (scanner.hasNextLine()){
                        final String lineFromFile = scanner.nextLine();
                         underTestMatcher = underTestPattern.matcher(lineFromFile);
                         ecuMatcher = ecuPattern.matcher(lineFromFile);
                         dtcMatcher = dtcPattern.matcher(lineFromFile);
                        //search for the line .underTest { color ...
                        if (underTestMatcher!=null && underTestMatcher.find()){
                            output.append(lineFromFile+"\n");
                            output.append(tagECUStyle+"\n");
                            output.append(tagDTCStyle+"\n");
                        }
                        //search for ECU in the ECU Right list that matches the ECU from the input file
                        if (ecuMatcher!=null && ecuMatcher.find()){
                            String ecuFromFile = ecuMatcher.group(2);
                            ecuFromDocRight = dTCDocRight.findEcuByName(ecuFromFile);
                            //add ECU tag if ECU status changed or if ECUs'DTC have changed
                            if (ecuFromDocRight.hasDtcEvolution || ecuFromDocRight.hasStatusEvolution) {
                                String s = ecuMatcher.group(1) + ecuMatcher.group(2) + ecuMatcher.group(3) + tagECU;
                                output.append(s + "\n");
                            } else
                                output.append(lineFromFile + "\n");
                        }
                        //search for DTC in the ECU Dtc list that matches the DTC from the input file of the last ECU.
                        // In htl, it is either several lines of tdmecu or tdmecu follows by several tdmdev
                        else if (dtcMatcher != null && dtcMatcher.find()) {
                            String dtcFromFile = dtcMatcher.group(2);                          
                            dtcFromEcuRight = ecuFromDocRight.findDtcByName(dtcFromFile);
                            //add DTC tag if DTC status changed or is new
                            if (dtcFromEcuRight.isNew){                          
                                output.append(lineFromFile.replaceFirst("\\<\\/FONT\\>", "\\<\\/FONT\\>"+tagDTCNew) + "\n");
                            } else if (dtcFromEcuRight.fromCurrentToHistorical) {                          
                                output.append(lineFromFile.replaceFirst("\\<\\/FONT\\>", "\\<\\/FONT\\>" + tagDTCFromCurrentToHistorical) + "\n");
                            } else if (dtcFromEcuRight.fromHistoricalToCurrent) {                      
                                output.append(lineFromFile.replaceFirst("\\<\\/FONT\\>", "\\<\\/FONT\\>" + tagDTCFromHistoricaltoCurrent) + "\n");
                            }                    
                            else
                                output.append(lineFromFile + "\n");
                        }
                        else {
                            output.append(lineFromFile + "\n");
                        }
                    }
                    output.close();
                    Desktop.getDesktop().open(htmlReport);

                } catch (IOException ex) {
                    Logger.getLogger(DTCCompareUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentResized(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }
        });

        d.setLocation(jButtonGenerateHtmlReport.getLocationOnScreen().x-50,jButtonGenerateHtmlReport.getLocationOnScreen().y+100);
        d.setVisible(true);       
    }//GEN-LAST:event_jButtonGenerateHtmlReportActionPerformed

    private void jTextPaneLeftMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextPaneLeftMouseClicked
        // TODO add your handling code here:
        if (dTCDocLeftIsOk)
            try {
                Desktop.getDesktop().open(dTCDocLeft.file);
        } catch (IOException ex) {
            Logger.getLogger(DTCCompareUI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_jTextPaneLeftMouseClicked

    private void jTextPaneRightMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextPaneRightMouseClicked
        // TODO add your handling code here:
                if (dTCDocRightIsOk)
            try {
                Desktop.getDesktop().open(dTCDocRight.file);
        } catch (IOException ex) {
            Logger.getLogger(DTCCompareUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jTextPaneRightMouseClicked

    /**
     * Copy File
     * @param source
     * @param dest
     * @throws IOException 
     */    
    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }
    
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
                /*System.out.println("File path is '.");*/
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
    private final SimpleAttributeSet inRed;
    private final SimpleAttributeSet inGreen;
    private final SimpleAttributeSet inBlack;
    private final SimpleAttributeSet inOrange;
    private SimpleAttributeSet Title;
    private final SimpleAttributeSet EcuStyle;
    private final SimpleAttributeSet DtcStyle;
    private String reportLanguage;
    private JDialog d;
    private String tagForHtmlreport;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonGenerateHtmlReport;
    private javax.swing.JCheckBox jCheckBoxDTC;
    private javax.swing.JCheckBox jCheckBoxECU;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelLeft;
    private javax.swing.JPanel jPanelRight;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextFieldTag;
    private javax.swing.JTextPane jTextPaneLeft;
    private javax.swing.JTextPane jTextPaneOutput;
    private javax.swing.JTextPane jTextPaneRight;
    private javax.swing.JToggleButton jToggleButtonEnglish;
    private javax.swing.JToggleButton jToggleButtonFrench;
    // End of variables declaration//GEN-END:variables


}
