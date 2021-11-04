/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DTCCompare;

enum TypeDtcFailure {
    CurrentFailure,
    HistoricalFailure
}
/**
 *
 * @author Utilisateur
 */
public class DTC {
    String name;
    TypeDtcFailure type;
    boolean isCurrentFailure=false;
    boolean isHistoricalFailure=false;
    
    // constructor
    public DTC(String n, TypeDtcFailure t){
        name=n;type=t;
        isCurrentFailure=type.equals(TypeDtcFailure.CurrentFailure);
        isHistoricalFailure=type.equals(TypeDtcFailure.HistoricalFailure);
    }
    
}
