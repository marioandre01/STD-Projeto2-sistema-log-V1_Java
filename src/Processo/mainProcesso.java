/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Processo;

import java.rmi.AlreadyBoundException;

/**
 *
 * @author marioandre e marioallan
 */
public class mainProcesso {

    public static void main(String[] args) throws AlreadyBoundException {
        
        //Exemplo para instanciar processo - p1 123456 3000 2000 processos.txt eventos.txt
        //Integer.valueOf(args[2])- converte string para inteiro
        //Instaciando objeto processo
        Processo p = new Processo(args[0], Integer.valueOf(args[1]), Integer.valueOf(args[2]), Integer.valueOf(args[3]), args[4], args[5]);
            
        //Exibe os dados do objeto instaciado
        p.Exibirdados();
            
        // Obtendo referência do serviço de registro, isto e, se registrando nele
        p.registrarSr();
            
        //Criando objeto remoto do processo pi
        p.criarObjRemoto(p);
           
        // Registrando objeto remoto criado no Serviço de registro
        p.registrarObjRemoto();

        //Aguarda todos os processos listados no arquivo processos.txt estarem instaciados
        System.out.println("Aguardando processos estarem ativos...");
        System.out.println(" ");
        p.sincronizarProcessos();
        System.out.println("os processos estao ativos...");
        System.out.println(" ");
            
        //Executando a logica do processo
        
        int e = 0;
        
        //Numero de vezes que o processo vai ser executado
        int nv = 2;
        
        while(e < nv){
            System.out.println("#################");
            
            p.aguardarTempoAleatorio();
            p.processarEvento();
            System.out.print("jitter antes de enviar para Log: ");
            p.jitter();
            p.getValorTotalEspera();
            p.enviarParaLog();
            
            System.out.println("#################");
            System.out.println();
            e++;
        }   
        System.out.println("Fim da execução do processo!");
    }
    
}
