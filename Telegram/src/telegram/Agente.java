/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegram;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author marcos
 */
public class Agente extends SingleAgent {
    
    private Queue<ACLMessage> cola;
    private boolean salir, solicitudMultimedia;
    private Bot bot;
    private long usuario;
    private String cambio;
    
    public Agente(AgentID aid) throws Exception{
        super(aid);
        cola=new LinkedList<ACLMessage>();
        salir=false;
        cambio="";
        solicitudMultimedia=false;
        
    }
    
    @Override
    public void onMessage(ACLMessage msg){
        cola.add(msg);
    }
    
    /*@Override
    public void init(){
        
    }*/
    @Override
    
    //Mientras se ejecuta el agente
    public void execute(){
        while(!salir){
                while(cola.isEmpty()){
                  
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    recibirMensaje(cola.poll());
                } catch (InterruptedException ex) {
                    Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) { 
                Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
            } 
            }

    }
    
    public void enviarMensaje(String receptor, String contenido, int performativa) throws InterruptedException{
        ACLMessage outbox= new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID(receptor));
        outbox.setContent(contenido);
        outbox.setPerformative(performativa);
        this.send(outbox);
        System.out.println("\nEnvio mensaje: ");
    }
    
    /*Recibir cualquier mensaje y se filtra según el tipo de mensaje recibido*/
    private void recibirMensaje(ACLMessage mensaje) throws InterruptedException, IOException, Exception{
        System.out.println("Recibo un mensaje de "+mensaje.getSender()+"  :"+mensaje.getContent());
        String emisor=this.procesarSender(mensaje.getSender());
        String perfomativa=mensaje.getPerformative();
        switch(perfomativa){
            case "REQUEST": 
                if(mensaje.getContent().contains("Envia foto")){
                    String aux=mensaje.getContent();
                    String[] data=aux.split(",");
                    String ruta=data[1];
                    String [] data2=ruta.split("Desktop/");
                    String descripcion=data2[1].substring(0, data2[1].length()-5);
                    if(solicitudMultimedia){
                        descripcion="Foto solicitada: "+descripcion;
                        bot.enviaFoto(ruta,descripcion);
                        solicitudMultimedia=false;
                    }
                    else{
                        descripcion="Atención se ha detectado movimiento: "+descripcion;
                        bot.enviaFoto(ruta,descripcion);
                        bot.enviarMensaje("Generando video...");
                    }
                    
                }
                else if(mensaje.getContent().contains("Envia video")){
                    String aux=mensaje.getContent();
                    String[] data=aux.split(",");
                    String ruta=data[1];
                    String [] data2=ruta.split("Desktop/");
                    String descripcion=data2[1].substring(0, data2[1].length()-5);
                    if(solicitudMultimedia){
                        descripcion="Video solicitado: "+descripcion;
                        bot.enviaVideo(ruta,descripcion);
                    }
                    else{
                        descripcion="Video obtenido desde la intrusión: "+descripcion;
                        bot.enviaVideo(ruta,descripcion);
                    }
                    
                }
                
                break;
            case "INFORM":
                if (mensaje.getContent().contains("Activado")){
                    if(cambio.equals("Activar")){
                        bot.enviarMensaje(usuario, "El estado de seguridad ya se encuentra ACTIVADO");
                    }
                    else if(cambio.equals("Desactivar")){
                        this.enviarMensaje("Marcos", "Desactiva Offline", ACLMessage.REQUEST);
                        bot.enviarMensaje("Se ha desactivado el sistema de seguridad");
                    }
                    else{
                        bot.enviarMensaje(usuario, "El estado de seguridad está ACTIVADO");
                    }
                    
                }
                else if (mensaje.getContent().contains("Desactivado")){
                    if(cambio.equals("Desactivar")){
                        bot.enviarMensaje(usuario, "El estado de seguridad ya se encuentra DESACTIVADO");
                    }
                    else if(cambio.equals("Activar")){
                        this.enviarMensaje("Marcos", "Activa Offline", ACLMessage.REQUEST);
                        bot.enviarMensaje("Se ha activado el sistema de seguridad");
                    }
                    else{
                        bot.enviarMensaje(usuario, "El estado de seguridad está DESACTIVADO");
                    }
                }
                else if (mensaje.getContent().contains("Temperatura") || mensaje.getContent().contains("Humedad")){
                    bot.enviarMensaje(usuario, mensaje.getContent());
                }
                else{
                    bot.enviarMensaje(mensaje.getContent());
                }
                break;
            default: 
                //Informar error
                this.enviarMensaje(emisor, "Ahora mismo no estoy preparado para responder a esa performativa", ACLMessage.NOT_UNDERSTOOD);
                break;
        }
        
        
    }
    
    /*Procesar el agente emisor para convertirlo en String y poder responderle*/
    private String procesarSender(AgentID emisor){
        String retorno=emisor.toString();
        String [] parts=retorno.split("@");
        parts=parts[0].split("//");
        retorno=parts[1];
        return retorno;
    }
    
    public void asignarBot(Bot bot){
        this.bot=bot;
    }
    
    
    public void estadoOffline(long usuario, String cambio) throws InterruptedException{
        this.usuario=usuario;
        this.cambio=cambio;
        this.enviarMensaje("Marcos", "Estado Offline", ACLMessage.QUERY_IF);
    }
    
    public void accionarPersiana(long usuario, String cambio) throws InterruptedException{
        this.usuario=usuario;
        this.cambio=cambio;
        if(cambio.equals("Subir")){
	    this.enviarMensaje("Marcos","Subir persiana",ACLMessage.REQUEST);
	}
	else {
	    this.enviarMensaje("Marcos","Bajar persiana",ACLMessage.REQUEST);
	}
    }
    
    public void accionarCochera(long usuario, String cambio) throws InterruptedException{
        this.usuario=usuario;
        this.cambio=cambio;
        if(cambio.equals("Abrir")){
	    this.enviarMensaje("Marcos","Abrir cochera",ACLMessage.REQUEST);
	}
	else {
	    this.enviarMensaje("Marcos","Cerrar cochera",ACLMessage.REQUEST);
	}
    }
    
    public void estadoLluvia(long usuario) throws InterruptedException{
        this.usuario=usuario;
        this.enviarMensaje("Marcos", "llueve", ACLMessage.QUERY_IF);
    }
    
    public void temperatura(long usuario) throws InterruptedException{
        this.usuario=usuario;
        this.enviarMensaje("Marcos", "temperatura", ACLMessage.QUERY_IF);
        this.enviarMensaje("Marcos", "humedad", ACLMessage.QUERY_IF);
    }
    
    public void solicitarMultimedia(String accion) throws InterruptedException{
        solicitudMultimedia=true;
        if(accion=="foto"){
            this.enviarMensaje("Marcos", "Hacer foto", ACLMessage.REQUEST);
        }
        else{
            this.enviarMensaje("Marcos", "Hacer video", ACLMessage.REQUEST);
        }
    }
    
}
