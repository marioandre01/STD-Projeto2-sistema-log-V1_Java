/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Processo;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author marioandre e marioallan
 */
public interface ProcDistribuido extends Remote{
    
    public void recebeMensagem(String m, String p) throws RemoteException;
    public void atualizar(int[] relogioRecebido) throws RemoteException;
    
}
