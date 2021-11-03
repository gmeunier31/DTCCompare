/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DTCCompare;

import java.util.ArrayList;

enum TypeEcuFailure {
    FailuresDetected,
    UnderTest,
    NoFailureDetected
}
/**
 *
 * @author Utilisateur
 */
public class ECU {
    public String name;
    public String extendedName;
    public TypeEcuFailure failuresStatus;
    boolean hasFailures = false;
    boolean isUnderTest = false;
    boolean hasNoFailures = false;
    
    public ArrayList<DTC> DtcList = new ArrayList<>();

    // Default constructor
    public ECU(String n, String en, TypeEcuFailure efs){
        name=n;
        extendedName=en;
        failuresStatus=efs;
        hasFailures=efs.equals(TypeEcuFailure.FailuresDetected);
        isUnderTest=efs.equals(TypeEcuFailure.UnderTest);
        hasNoFailures=efs.equals(TypeEcuFailure.NoFailureDetected);
    }
}
