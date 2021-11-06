/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DTCCompare;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Utilisateur
 */
public class DTCDocument {
    public Document doc;
    public ArrayList<ECU> ecuList = new ArrayList<>();
    public int totalFailuresCount=0;
    public int currentFailuresCount=0;
    public int historicalFailuresCOunt=0;
    public int dtcWithTestNotCompletedCount=0;
    public File file;
    public String title;
    public boolean isInFrench=false;
    public boolean isInEnglish=false;
    
    
    
    //default constructor    
    public DTCDocument(File f) {
        try {
            doc = (Document) Jsoup.parse(f, "UTF-8", "");
            file = f;           
        } catch (IOException ex) {
            Logger.getLogger(DTCDocument.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * parse the document and fill the ECU information and its DTC.
     * detect language based on title
     * @return 
     */
    public boolean parseAndFill() {
        TypeEcuFailure ecuFailureType=TypeEcuFailure.FailuresDetected;
        TypeDtcFailure dtcFailureType=TypeDtcFailure.CurrentFailure;
        ECU newEcu = null;
        DTC newDtc= null;
        // isolate the summary line where the generic info are present for the different failures count
        Element summary = doc.getElementById("summary");
        /*System.out.println("summary" + summary.text());*/
        String s= summary.text();
        title = s.replace(",","\n"); // TO DO Remplacer tous les chiffres par \nles chiffres en question
        String patternEn = "Vehicle with .*";
        String patternFr = "Véhicule avec .*";
        String pattern;
        Pattern rEn = Pattern.compile(patternEn);
        Pattern rFr = Pattern.compile(patternFr);
        Pattern r;
        Matcher mEn = rEn.matcher(summary.text());
        Matcher mFr = rFr.matcher(summary.text());
        
        this.isInEnglish = mEn.find();
        this.isInFrench = mFr.find();
        Matcher m = (this.isInEnglish)?mEn:(this.isInFrench)?mFr:null;
        if (m!=null && m.find()) {
            /*totalFailuresCount = Integer.parseInt(m.group(1));
            currentFailuresCount = Integer.parseInt(m.group(2));
            historicalFailuresCOunt = Integer.parseInt(m.group(3));
            dtcWithTestNotCompletedCount = Integer.parseInt(m.group(4));*/
        } else {
            System.out.println("NO MATCH");
        }

        //serious stuff starts here. First let's grab the different ECUs
        //<LI class=tdmecu><A href="#ID0AB01B0A" name=#tocID0AB01B0A>IDM-CDM - <B>CDM_Sweet200_V2.4</B></A>&nbsp; &nbsp; <SPAN class=hasfailure>Failures detected</SPAN>
        Elements ecus = doc.getElementsByClass("tdmecu");
        for (Element ecu : ecus) {
            /*System.out.println("ECU: " + ecu.getElementsByClass("tdmecu").text());*/
            String ECU = ecu.getElementsByClass("tdmecu").text();
            patternEn = "(.*) - (.*) [Failures detected|Some devices are still under test|No failure detected]";
            patternFr = "(.*) - (.*) [Pannes détectées sur ce calculateur|Certains organes sont en cours de test]";
            
            if (this.isInEnglish){
                r = Pattern.compile(patternEn);
            } else
                r = Pattern.compile(patternFr);
            m = r.matcher(ECU);
            if (m.find()) {
                /*System.out.println("ECU name: " + m.group(1));
                System.out.println("ECU Extended name: " + m.group(2));
                System.out.println("ECU: " + ecu.getElementsByClass("hasfailure").text());
                System.out.println("ECU: " + ecu.getElementsByClass("underTest").text());
                System.out.println("ECU: " + ecu.getElementsByClass("nofailure").text());*/
                
                if (!ecu.getElementsByClass("hasfailure").text().equals(""))
                    ecuFailureType=TypeEcuFailure.FailuresDetected;
                else if (!ecu.getElementsByClass("underTest").text().equals(""))
                    ecuFailureType=TypeEcuFailure.UnderTest;
                else if (!ecu.getElementsByClass("nofailure").text().equals(""))
                    ecuFailureType=TypeEcuFailure.NoFailureDetected;
                
                newEcu = new ECU(m.group(1), m.group(2), ecuFailureType);
                                
            } else {
                System.out.println("NO MATCH");
            }
            
            // now let's see the DTC type of the ECU
            Elements dtcs = ecu.getElementsByClass("tdmdev");
            
            for (Element dtc: dtcs){
                // this section is just for debugging
                /*System.out.println("DTC: " + dtc.getElementsByClass("tdmdev").text());
                System.out.println("DTC: " + dtc.getElementsByClass("tdmerror").text());
                System.out.println("DTC: " + dtc.getElementsByClass("tdmwarning").text());*/

                String sDTC = dtc.getElementsByClass("tdmdev").text();
                if (this.isInEnglish)                    
                    pattern = "(.+) (Current Failure|Historical Failure)";
                else
                    pattern = "(.+) (Panne|Panne présente|Pannes détectées sur ce calculateur)";
 
                r = Pattern.compile(pattern);
                m = r.matcher(sDTC);
                if (m.find()) {
                    if (!dtc.getElementsByClass("tdmerror").text().equals("")) {
                        dtcFailureType = TypeDtcFailure.CurrentFailure;
                    } else if (!dtc.getElementsByClass("tdmwarning").text().equals("")) {
                        dtcFailureType = TypeDtcFailure.HistoricalFailure;
                    }
                    System.out.println("DTC: "+m.group(1)+" --> "+dtcFailureType);
                    newDtc = new DTC(m.group(1), dtcFailureType);
                    newEcu.dtcList.add(newDtc);
                } else {
                System.out.println("NO MATCH FOR DTC");
            }
                
            }
            ecuList.add(newEcu);

        }
        return true;
    }
    
    /**
     * return ECU from the list of ECU when name is matching
     * @param name
     * @return 
     */
    public ECU findEcuByName(String name){
        ECU matchEcu = new ECU();
        
        for (ECU ecu: ecuList){
            if (ecu.name.equals(name))
                matchEcu = ecu;                
        }
        return matchEcu;
    }
}
    
