/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author marioandre e marioallan
 */
public class Log implements LogDistribuido{
    
    //Metodos
    private int numLinha = 1;
    String arqProc = "processos.txt";
    String tempo;
    String[] vetorArqProc = getVetorArqProcessos();
    Queue<mensagemLog> fila;
   
    public Log() {
        this.fila = new LinkedList<>();
        
    }
    
    @Override
    public void enviaLog(String id, int intId,  int nm, String e, String ve, int [] rl) throws RemoteException{
        
        boolean procPermitido;
        procPermitido = processoPermitido(id);
        //verifica se processo recebido pode ser aceito pelo Log
        if(procPermitido == true){
            mensagemLog ml = new mensagemLog(id, intId, nm, e, ve, rl);
       
            logicaLog(ml);
        }
    }
    
    //enviaLogER - E:envio, R:recebimento
    @Override
    public void enviaLogER(String id, int intId, int nm, String idqe, String e, String ve, int [] rl) throws RemoteException{
       
        boolean procPermitido;
        procPermitido = processoPermitido(id);
        //verifica se processo recebido pode ser aceito pelo Log
        if(procPermitido == true){
             mensagemLog ml = new mensagemLog(id, intId, nm, idqe, e, ve, rl);
       
            logicaLog(ml);
        } 
    }
    
    public void logicaLog(mensagemLog obj) throws RemoteException{
        
        //atualiza o relogio
        tempo = timestamp();
        
        //armazenar a mensagem na fila de espera
        fila.offer(obj);
        
    }
    
    @Override
    public void imprirLogV1(){
        if(!fila.isEmpty()){
            m1 = fila.poll();
            imprimeMensagemLog(m1);
        }
        
    }
    
    mensagemLog m1;
    mensagemLog m2;
    boolean passo1 = true;
    int n = 0;
    int c=1;
    
    @Override
    public void imprirLogV2(){
        // System.out.println("tamanho fila:" + fila.size());
        if(!fila.isEmpty()){
            
            
            if(passo1 == true){
                //if(fila.size() > 2){
                    m1 = fila.poll();
                    passo1 = false;
               // }
                
            }else{
                //System.out.println("Entrei: "+n);
                //System.out.println("tamanho fila:" + fila.size());
            
                m2 = fila.peek();
                
                boolean p;
                p = r1PrecedeR2(m1, m2);
                
                if(p == true){
                    
                    imprimeMensagemLog(m1);
                    m1 = fila.poll();
                    
                    //if(fila.isEmpty()){
                   //         imprimeMensagemLog(m1);
                   // }
                   
                }else{
                    
                    boolean temCon;
                    temCon = temConcorrencia(m1, m2);
                    
                    if(temCon == true){
                        m2 = fila.poll();
                        fila.offer(m2);
                        
                    }else{
                        m2 = fila.poll();
                        imprimeMensagemLog(m2);
                        
                        /*if(fila.isEmpty()){
                            imprimeMensagemLog(m1);
                        }*/
                    }  
                }    
            } 
            n++;
        }
        
    }
    
    public boolean temConcorrencia(mensagemLog obj1, mensagemLog obj2){
        
        boolean tc;
        
        boolean [] resultadoMenorIgual;
        resultadoMenorIgual = new boolean[obj1.relogioLogico.length];
        boolean [] resultadoMenorIgual2;
        resultadoMenorIgual2 = new boolean[obj1.relogioLogico.length];
        
        boolean multiResultadoMI = true;
        boolean multiResultadoMI2 = true;
        
        for(int i=0; i < obj1.relogioLogico.length; i++){
            resultadoMenorIgual[i] = obj1.relogioLogico[i] <= obj2.relogioLogico[i];
            resultadoMenorIgual2[i] = obj2.relogioLogico[i] <= obj1.relogioLogico[i];
        }
        
        for(int i=0; i < obj1.relogioLogico.length; i++){
           
           multiResultadoMI = multiResultadoMI && resultadoMenorIgual[i];
           multiResultadoMI2 = multiResultadoMI2 && resultadoMenorIgual2[i];  
        }
        
        tc = (multiResultadoMI == false && multiResultadoMI2 == false);
        
        return tc;
    }
    
    public boolean r1PrecedeR2(mensagemLog obj1, mensagemLog obj2){
        
        boolean p;
        
        boolean [] resultadoMenorIgual;
        resultadoMenorIgual = new boolean[obj1.relogioLogico.length];
        
        boolean multiResultadoMI = true;
        
        for(int i=0; i < obj1.relogioLogico.length; i++){
            resultadoMenorIgual[i] = obj1.relogioLogico[i] <= obj2.relogioLogico[i];
        }
        
        for(int i=0; i < obj1.relogioLogico.length; i++){
           
           multiResultadoMI = multiResultadoMI && resultadoMenorIgual[i];
        }
        
        p = multiResultadoMI; 
                
        return p;
    }
    
    public void imprimeMensagemLog(mensagemLog o){
        if(o.evento.equals("l")){
            imprimeMensagemLogL(o);
        }else{
            imprimeMensagemLogER(o);
        }
     }
     
    public void imprimeMensagemLogL(mensagemLog o){
        System.out.print(tempo + "  "+ o.id + "    m" + o.nm + "=" +o.evento+ ","+ o.valorEvento + "     [");
        imprimeReLogico(o.relogioLogico);
        System.out.print("]       " + numLinha);
        System.out.println();
        numLinha++;
    }
    
    public void imprimeMensagemLogER(mensagemLog o){
        
        System.out.print(tempo + "  "+ o.id + "    m" + o.nm + "=" +o.evento+ ","+ o.valorEvento + "," +o.idQEnviou+ "  [");
        imprimeReLogico(o.relogioLogico);
        System.out.print("]       " + numLinha);
        System.out.println();
        numLinha++;
    }
    
    public void imprimeReLogico(int [] r){
        
        for(int i=0; i < r.length; i++){
            
            System.out.print(r[i]);
            if(i != (r.length-1) ){
                System.out.print(",");   
            }
        }
        
    }
    
    //Metodo que mostra a data e hora do processo Log
    public String timestamp() throws RemoteException{
        
        //Pegar Data e tempo do sistema ex: 2017-11-15T16:19:09.736
        LocalDateTime agora = LocalDateTime.now();
        
        //Formatando data e tempo , ex: 15-11-2017 16:19:09
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss:SSS");
        String DataTempoFormatado = agora.format(formatter);
       
        return DataTempoFormatado;
    }
    
    public String[] getVetorArqProcessos(){
        
        int linha;
        linha = getNumlinhasArqProc();
        
        String vetorProcessos[] = new String[linha];
        vetorProcessos = linhaArqToVetor(linha, arqProc); 
        
        return vetorProcessos;
    }
    
    public final int getNumlinhasArqProc(){
        int n;
        n = numLinhaArq(arqProc);
        
        return n;
    }
    
    //metodo que le cada linha do arquivo processos.txt e coloca em um vetor 
    public String[] linhaArqToVetor(int nl, String arq) {
        
        String v[] = new String[nl];
        int i;
        for(i=0; i < nl;i++){
            v[i]= "";
        }
        
        try {
            String caminho = "src/Log/";
            String caminhoEarq = caminho + arq;
            
            File arquivo; 
        
            // Lendo do arquivo  
            arquivo = new File(caminhoEarq);  
            FileInputStream fis = new FileInputStream(arquivo);  
            
            //coletando caracteres do arquivo e coloca numa posiçao do vetor 
            i = 0;
            int ln;  
            while ( (ln = fis.read()) != -1 ) {  
                if ((char)ln != '\n'){
                    v[i] += (char)ln;
                }else{
                    i++;
                }
            }  
            fis.close(); 
        }catch (IOException ee) {  
        
        }
        return v;
        
    }
    
    //metodo que retorna o numero de linhas do arquivo fornecido
    public int numLinhaArq(String arq) {
        int n = 0;
        
        try {
            //definindo caminho do arquivo
            String caminho = "src/Log/";
            String caminhoEarq = caminho + arq;
            
            File arquivo; 
        
            // Lendo do arquivo  
            arquivo = new File(caminhoEarq);  
            FileInputStream fis = new FileInputStream(arquivo);  
                
            int ln;  
            while ( (ln = fis.read()) != -1 ) {  
                
                if ((char)ln == '\n'){
                    n++;
                }
            }  
            fis.close(); 
        }catch (IOException ee) {  
        
        }
        return n;
    }
    
    public boolean processoPermitido(String p) {
        boolean pp = false;
        
        for(int i=0; i < vetorArqProc.length; i++){
            if(p.equals(vetorArqProc[i])){
                pp = true;
                break;
            }
        }
        
        return pp;
    }
    
}
