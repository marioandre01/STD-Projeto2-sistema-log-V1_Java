/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Processo;

import Log.LogDistribuido;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marioandre e marioallan
 */
public class Processo implements ProcDistribuido{
    
    //Atributos
    String id;
    int intId;
    int semente;
    int tempoEspera;
    int tempoJitter;
    String arqProc;
    String arqEventos;
    Random r;
    
    private static final String IPSERVIDOR = "127.0.0.1";
    private static final int PORTA = 1234;
    Registry registro;
    ProcDistribuido objRemotoDoProc;
     
    int numeroDeMensagens = 1;
    int valorTotalespera;
    int[] relogioLogico;
    int[] relogioLogicoTemp;
    String valorDoEvento;
    String evento;
    String idQueEnviou;
    
    //Metodo contrutor de 6 parametros
    public Processo(String id, int semente, int tempoEspera, int tempoJitter, String arqProc, String arqEventos){
        this.id = id;
        this.semente = semente;
        this.tempoEspera = tempoEspera;
        this.tempoJitter = tempoJitter;
        this.arqProc = arqProc;
        this.arqEventos = arqEventos;
        this.r = new Random(semente);
        this.intId = converterIdEmInt(id);
       
        int nlap = getNumlinhasArqProc(); //nlap - numeroLinhasArquivoProcessos
        relogioLogico = new int[nlap-1]; // -1 para tirar a linha do Log
        relogioLogicoTemp = new int[nlap-1];
        
    }
    
    //Metodo que retorna o numero do processo que e passado como parametro, isto e, passa p1, retorna 1
    public final int converterIdEmInt(String s) {
        int nump = 0;
        String idp = "";
        
        if (!s.isEmpty()) idp = s.substring (1); //Se String s nao estiver fazia faz, pega os caracter de s apartir da posiçao 1 e coloca em idp
        nump = Integer.valueOf(idp); //converte string em inteiro
        
        return nump;
    }
    
    //Metodo que recebe mensagem do processo que o chamar
    @Override
    public void recebeMensagem(String m, String p) throws RemoteException{
        
        Thread t = new recebeMensagemThread(m, p, relogioLogico, r, tempoJitter, id, intId, registro, numeroDeMensagens);
        t.start(); 
        numeroDeMensagens++;
      
    }
    
    @Override
    public void atualizar(int[] relogioRecebido) throws RemoteException{
        for(int i=0; i < relogioLogico.length; i++){
            if(relogioLogico[i] < relogioRecebido[i]){
                relogioLogico[i] = relogioRecebido[i];
            }
        }
        atualizaRelogio();
    }
    
    //Metodo que faz o registro no Serviço de registro
    public void registrarSr() {
        try {
            registro = LocateRegistry.getRegistry(IPSERVIDOR, PORTA);
        } catch (RemoteException ex) {
            Logger.getLogger(Processo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Metodo que cria objeto remoto do objeto do processo
    public void criarObjRemoto(Processo p) {
        try {
             objRemotoDoProc = (ProcDistribuido) UnicastRemoteObject.exportObject(p, 0);
            
        } catch (RemoteException ex) {
            Logger.getLogger(Processo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Metodo que registra o objeto remoto criado no Serviço de registro
    public void registrarObjRemoto() {
        try {
            registro.bind(id, objRemotoDoProc); //id e o nome que vai ser dado ao objeto, para depois poder ser procurado no Serviço de registro
        } catch (RemoteException | AlreadyBoundException ex) {
            Logger.getLogger(Processo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Metodo que aguarda todos os processos listados no arquivo processos.txt serem instanciados
    public void sincronizarProcessos() {
        
        converterIdEmInt(id);
        
        //cria vetor v, e recebe o vetor com o valor das linhas do arquivo processos.txt
        String[] vetorArqProc = getVetorArqProcessos();
        
        //cria inteiro l e recebe o numero de linhas do arquivo processos.txt
        int numDeLinhasArqProc = getNumlinhasArqProc();
        int numeroDeProcAtivos = 0;
        
        //cria vetor lib, que sera usado para saber se todos os processos estao instanciados
        boolean processosAtivos[] = new boolean[numDeLinhasArqProc];
        String objRegSr[]; //objRegSr - objetoRegistradoServicoRegistro
        
        //colocando o valor false nas posiçoes no vetor lib
        for(int c=0; c < numDeLinhasArqProc; c++){
            processosAtivos[c] = false;
        }        
               
        //Laço que fica verificando se todos os processos listados no arquivo processos.txt estao instanciados
        while(numeroDeProcAtivos < numDeLinhasArqProc){  //
            
            try {
                
                numeroDeProcAtivos = 0;
                
                //recebendo em objRegSr um vetor com os objetos registrados no Serviço de registro
                objRegSr = registro.list();
            
                //Verificando se os processo do arquivo processos.txt estao registrados no Serviço de registro
                for(int a=0; a < numDeLinhasArqProc; a++){
                
                    for (String b : objRegSr) {
                        if(vetorArqProc[a].equals(b)){
                            processosAtivos[a] = true;
                            break;
                        }
                    }
                }
                
                //Contando o numero de verdades do vetor processosAtivos
                for(int d=0; d < numDeLinhasArqProc; d++){
                    if (processosAtivos[d] == true){
                        numeroDeProcAtivos++;
                    }
                }
            
            }catch (RemoteException ex) {
                Logger.getLogger(Processo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //Metodo que envia mensagem para o processo Log
    public void enviarParaLog() {
        if(evento.equals("l")){  //se o conteudo de evento for igual a l(evento local) entao faz
            try {
                // Procurando pelo objeto distribuído registrado com nome Log
                LogDistribuido stub = (LogDistribuido) registro.lookup("Log");
                //stub.enviaLog(id, numeroDeMensagens, evento, valorDoEvento, relogioLogicoTemp);
                stub.enviaLog(id, intId, numeroDeMensagens, evento, valorDoEvento, relogioLogicoTemp);
                numeroDeMensagens++;
            
            } catch (RemoteException | NotBoundException ex) {
                Logger.getLogger(Processo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{  //senao faz,  se o conteudo de evento for igual a e(evento de envio)
            
            enviarParaLogProcEnv(evento, valorDoEvento, idQueEnviou, relogioLogicoTemp);
        }
    }
    
    //Metodo que envia mensagem para o processo Log para processo que enviaram ou receberam mensagens
    public void enviarParaLogProcEnv(String ev, String m, String p, int [] rl) {  ///enviarParaLogProcEnvReceb("r", m, p);
        try {
            // Procurando pelo objeto distribuído registrado com nome Log
            LogDistribuido stub = (LogDistribuido) registro.lookup("Log");
            //stub.enviaLogER(id, numeroDeMensagens, p, ev, m, rl);
            stub.enviaLogER(id, intId, numeroDeMensagens, p, ev, m, rl);
            numeroDeMensagens++;
            
        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(Processo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Metodo que aguarda um tempo gerado aleatoriamente e depois continua a execuçao do codigo
    public void aguardarTempoAleatorio(){
        
        //sorteia um numero de 0 ate o valor passado pela variavel tempoEspera, e coloca em tempoEsperaSorteado
        int tempoEsperaSorteado = r.nextInt(tempoEspera);
        System.out.println("Tempo de espera gerado: " + tempoEsperaSorteado);
        valorTotalespera = tempoEsperaSorteado;
        
        try {
            Thread.sleep(tempoEsperaSorteado);
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Processo.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    //Metodo que executa um evento da lista de eventos do arquivo eventos.txt
    public void processarEvento(){
        //vetor vetorEventos[] recebe um vetor com o conteudo do arquivo eventos.txt
        //String matrizEventos[][] = getMatrizArqEventos();
        String vetorEventos[] = getVetorArqEventos();
        
        //sorteia um numero de 0 ate o valor de colunas do vetor e coloca na variavel csdm
        int csdv = r.nextInt(vetorEventos.length); //csdm - colunaSorteadoDoVetor
            
        System.out.println("linha sorteada do arquivo de Eventos: " + (csdv+1));
        
        //atraves do valor da linha sorteda se escolhe a linhas da matriz a ser usada
        String eventoSorteado = vetorEventos[csdv];
        
        //Sorteia um valor de 0 ate 10000
        int ns = r.nextInt(10000); //ns - numero sorteado
        String valorEventoSorteado = String.valueOf(ns);
        //String valorEventoSorteado = matrizEventos[csdv][1];
        
        System.out.println("evento: " + eventoSorteado + ", valor: " + valorEventoSorteado);
        
        if(eventoSorteado.equals("l")){  //se eventoSorteado igual a "l" (evento local) entao
            System.out.println("Evento local realizado");
            valorDoEvento = valorEventoSorteado;
            evento = eventoSorteado;
            //atualizaRelogio();
            
            //copia o conteudo de relogioLogico para relogioLogicoTemp
            System.arraycopy(relogioLogico, 0, relogioLogicoTemp, 0, relogioLogico.length);
        }else { //senao , eventoSorteado igual a "e" (evento de envio)
            System.out.println("Evento de envio de mensagem realizado");
                
            String[] vetorArqProc = getVetorArqProcessos();
                
            while(true){
                    
                int lsdv = r.nextInt(vetorArqProc.length); //lsdv - linhaSorteadaDoVetor
                //System.out.println("Processo sorteado: "+ v[lsv]);
                    
                if (!vetorArqProc[lsdv].equals("Log") && !vetorArqProc[lsdv].equals(id)){
                    try {
                        System.out.println("Processo sorteado: "+ vetorArqProc[lsdv]);
                        
                        //atualizaRelogio();
                        
                        //copia o conteudo de relogioLogico para relogioLogicoTemp
                        System.arraycopy(relogioLogico, 0, relogioLogicoTemp, 0, relogioLogico.length);
                        
                        //System.out.println("p0: "+ relogioLogicoTemp[0]);
                        
                        // Procurando pelo objeto distribuído registrado com nome passado em lookup(), isto e, id de um processo
                        ProcDistribuido stub = (ProcDistribuido) registro.lookup(vetorArqProc[lsdv]);
                        
                        System.out.print("jitter antes de enviar para processo "+ vetorArqProc[lsdv] + ": ");
                        jitter();
                        //stub.atualizar(relogioLogicoTemp);
                        stub.recebeMensagem(valorEventoSorteado, id);
                        
                        
                        valorDoEvento = valorEventoSorteado;
                        evento = eventoSorteado;
                        idQueEnviou = vetorArqProc[lsdv];
                        
                        break;
                        
                    } catch (RemoteException | NotBoundException ex) {
                        Logger.getLogger(Processo.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    
    //Metodo que faz uma pausa na execuçao do codigo
    public void jitter(){
        
        int tsdj = r.nextInt(tempoJitter); //tj tempoSorteadoDoJiter
        System.out.println(tsdj);
        valorTotalespera += tsdj;
        
        try {
            Thread.sleep(tsdj);
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Processo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void getValorTotalEspera(){
        System.out.println("Tempo total esperado para poder enviar para Log: " + valorTotalespera);
    }
    
    public void atualizaRelogio(){
       
        relogioLogico[intId - 1]++;
    }
    
    //Metodo que exibe os dados de um processo
    public void Exibirdados() {
        System.out.println("### Dados do processo ###");
        System.out.println("-------------------------");
        System.out.println("Id: " + id);
        System.out.println("semente: " + semente);
        System.out.println("tempoEspera: " + tempoEspera);
        System.out.println("tempoJitter: " + tempoJitter);
        System.out.println("arqProc: " + arqProc);
        System.out.println("arqEventos: " + arqEventos);
        System.out.println("IntId: " + intId);
        System.out.println("Nº posiçoes relogio: " + relogioLogico.length);
        System.out.println("-------------------------");
        System.out.println("");

    }
    
    public String getIdProcesso(){
        
        return id;
    }
    
    public final int getNumlinhasArqProc(){
        int n;
        n = numLinhaArq(arqProc);
        
        return n;
    }
    
    public int getNumlinhasArqEventos(){
        int n;
        n = numLinhaArq(arqEventos);
        
        return n;
    }
    
    public String[] getVetorArqProcessos(){
        
        int linha;
        linha = getNumlinhasArqProc();
        
        String vetorProcessos[] = new String[linha];
        vetorProcessos = linhaArqToVetor(linha, arqProc); 
        
        return vetorProcessos;
    }
    
    public String[] getVetorArqEventos(){
        
        int linha;
        linha = getNumlinhasArqEventos();
        
        String vetorEventos[] = new String[linha];
        vetorEventos = linhaArqToVetor(linha, arqEventos); 
        
        return vetorEventos;
    }
    
    //metodo que le cada linha do arquivo processos.txt e coloca em um vetor 
    public String[] linhaArqToVetor(int nl, String arq) {
        
        String v[] = new String[nl];
        int i;
        for(i=0; i < nl;i++){
            v[i]= "";
        }
        
        try {
            String caminho = "src/Processo/";
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
            String caminho = "src/Processo/";
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
    
    //Metodo que le o conteudo de um vetor
    public void lerVetor(String v[]) {
        int x;
        for(x=0; x < v.length ;x++){
            System.out.println("linha "+ x +":" + v[x]);
        }
    }
    
}
