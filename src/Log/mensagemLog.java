/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Log;

/**
 *
 * @author marioandre e marioallan
 */
public class mensagemLog {
    
    //atributos
    String id;
    int intId;
    int nm;
    String evento;
    String valorEvento;
    int [] relogioLogico;
    String idQEnviou = "";
    int ti = 0;
    
    public mensagemLog(String id, int intId,  int nm, String e, String ve, int [] rl){
        this.id = id;
        this.intId = intId;
        this.nm = nm;
        this.evento = e;
        this.valorEvento = ve;
        this.relogioLogico = rl;
                
    }
    
     public mensagemLog(String id, int intId, int nm, String idqe, String e, String ve, int [] rl){
        this.id = id;
        this.intId = intId;
        this.nm = nm;
        this.evento = e;
        this.valorEvento = ve;
        this.relogioLogico = rl;
        this.idQEnviou = idqe;
                
    }
}
