/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Processo;

import Log.LogDistribuido;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marioandre
 */
public class recebeMensagemThread extends Thread {

    String mensagem;
    String processo;
    int[] relogioLogico;
    Random r;
    int tempJitter;
    String id;
    int intId;
    Registry registro;
    int numeroDeMensagens;
    
    
    public recebeMensagemThread(String m, String p, int[] rl, Random r, int tJ, String id, int iId, Registry reg, int ndm){
        this.mensagem = m;
        this.processo = p;
        this.relogioLogico = rl;
        this.r = r;
        this.tempJitter = tJ;
        this.id = id;
        this.intId = iId;
        this.registro = reg;
        this.numeroDeMensagens = ndm;
        
    }
    
    
    @Override
    public void run() {
        System.err.println("Recebi mensagem de " + processo + ", conteudo: " + mensagem);
        
        System.out.print("jitter antes de enviar para Log(Processo que recebeu de outro processo): ");
        jitter();
        
        enviarParaLogProcEnvReceb("r", mensagem, processo, relogioLogico);
    }
    
    //Metodo que faz uma pausa na execuçao do codigo
    public void jitter(){
        
        int tsdj = r.nextInt(tempJitter); //tsdj tempoSorteadoDoJiter
        System.out.println(tsdj);
        //valorTotalespera += tsdj;
        
        try {
            Thread.sleep(tsdj);
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Processo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Metodo que envia mensagem para o processo Log para processo que enviaram ou receberam mensagens
    public void enviarParaLogProcEnvReceb(String ev, String m, String p, int [] rl) {  ///enviarParaLogProcEnvReceb("r", m, p);
        try {
            // Procurando pelo objeto distribuído registrado com nome Log
            LogDistribuido stub = (LogDistribuido) registro.lookup("Log");
            stub.enviaLogER(id, intId, numeroDeMensagens, p, ev, m, rl);
            numeroDeMensagens++;
            
        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(Processo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
