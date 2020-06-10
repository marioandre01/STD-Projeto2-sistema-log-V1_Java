/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Log;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author marioandre e marioallan
 */
public interface LogDistribuido extends Remote{
   
    public void enviaLog(String id, int intId, int nm, String e, String ve, int [] rl) throws RemoteException;
    public void enviaLogER(String id, int intId, int nm, String idqe, String e, String ve, int [] rl) throws RemoteException;
    public void imprirLogV1() throws RemoteException;
    public void imprirLogV2() throws RemoteException;
}
