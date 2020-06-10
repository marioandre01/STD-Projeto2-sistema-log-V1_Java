/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Log;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marioallan e marioandre
 */
public class mainLog {

    // Constantes que indicam onde está sendo executado o serviço de registro,
    // qual porta e qual o nome do objeto distribuído
    private static final String IPSERVIDOR = "127.0.0.1";
    private static final int PORTA =1234;
    private static final String NOMEOBJDIST = "Log";
    
    public static void main(String[] args) {
        try {
            // Criando objeto LogDist
            Log l = new Log();

            //criando objeto remoto de LogDist
            LogDistribuido stub = (LogDistribuido) UnicastRemoteObject.exportObject(l, 0);

            // Criando serviço de registro
            Registry registro = LocateRegistry.createRegistry(PORTA);

            // Registrando objeto distribuído
            registro.bind(NOMEOBJDIST, stub);

            System.out.println("Log em execuçao!\n");
            System.out.println("Pressione CTRL + C para encerrar...");
            System.out.println(" ");
            System.out.println("Legenda: l: Evento local | e: Evento enviou para | r: Evento recebeu de");
            System.out.println(" ");
            System.out.println("#Timestamp               FROM  Message       LogicalClock    * ");
            
            
            
            while(true){
                
              stub.imprirLogV1();
              //stub.imprirLogV2();
            }


        } catch (RemoteException | AlreadyBoundException ex) {
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
