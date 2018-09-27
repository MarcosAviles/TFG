
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package TFG;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author marcos
 */
public class Agente extends SingleAgent {
    
    private boolean escuchar, offline;
    private String accion, emisorConsulta, emisorPeticion, estado_anterior_persiana;

    
    public Agente(AgentID aid) throws Exception{
        super(aid);
        escuchar=true;
        offline=false;
        estado_anterior_persiana=emisorPeticion=accion=emisorConsulta="";
    }
    
    @Override
    public void execute(){
        try {
            while(escuchar){
                recibirMensaje();
            }
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void enviarMensaje(String receptor, String contenido, int performativa){
        ACLMessage outbox= new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID(receptor));
        outbox.setContent(contenido);
        outbox.setPerformative(performativa);
        this.send(outbox);
        System.out.println("\nEnvio mensaje: "+contenido+" a: "+receptor);
    }
    
    /*Recibir cualquier mensaje y se filtra según el tipo de mensaje recibido*/
    private void recibirMensaje() throws InterruptedException{
        ACLMessage mensaje = new ACLMessage();
        mensaje=this.receiveACLMessage();
        System.out.println("Recibo un mensaje de "+mensaje.getSender()+"  :"+mensaje.getContent());
        String emisor=this.procesarSender(mensaje.getSender());
        String perfomativa=mensaje.getPerformative();
        switch(perfomativa){
            case "REQUEST":
                emisorPeticion=emisor;
                accion=mensaje.getContent();
                if(mensaje.getContent().contains("Subir persiana")){
                    this.enviarMensaje("Persiana", "Estado persiana", ACLMessage.QUERY_IF);
                }
                else if(mensaje.getContent().contains("Bajar persiana")){
                    this.enviarMensaje("Persiana", "Estado persiana", ACLMessage.QUERY_IF);
                }
                else if(mensaje.getContent().contains("Abrir cochera")){
                    this.enviarMensaje("Cochera", "Estado cochera", ACLMessage.QUERY_IF);
                }
                else if(mensaje.getContent().contains("Cerrar cochera")){
                    this.enviarMensaje("Cochera", "Estado cochera", ACLMessage.QUERY_IF);
                }
                else if(mensaje.getContent().contains("Hacer foto")){
                    this.enviarMensaje("Camara", "Hacer foto", ACLMessage.REQUEST);
                }
                else if(mensaje.getContent().contains("Hacer video")){
                    this.enviarMensaje("Camara", "Hacer video", ACLMessage.REQUEST);
                }
                else if(mensaje.getContent().contains("Activa Offline")){
                    this.activarOffline();
                }
                else if(mensaje.getContent().contains("Desactiva Offline")){
                    this.desactivarOffline();
                }
                else if(mensaje.getContent().contains("Cambio Offline")){
                    if(offline){
                        this.desactivarOffline();
                        this.enviarMensaje("Telegram", "Se ha desactivado el sistema de seguridad manualmente.", ACLMessage.INFORM);
                    }
                    else{
                        this.activarOffline();
                        this.enviarMensaje("Telegram", "Se ha activado el sistema de seguridad manualmente.", ACLMessage.INFORM);
                    }
                    this.enviarMensaje("Offline", "ok", ACLMessage.INFORM);
                    
                }
                else {
                    this.enviarMensaje(emisor, "No te comprendo", ACLMessage.NOT_UNDERSTOOD);
                }
                break;
            case "INFORM":
                if(mensaje.getContent().contains("Intruso")){
                    if(offline){
                        this.enviarMensaje("Camara", "foto sensor", ACLMessage.REQUEST);
                    }
                    else{
                        this.enviarMensaje("Mov", "ok", ACLMessage.INFORM);
                    }
                }
                else if(mensaje.getContent().contains(".jpeg")){
                    this.enviarMensaje("Telegram", "Envia foto,"+mensaje.getContent(), ACLMessage.REQUEST);
                }
                else if(mensaje.getContent().contains(".mp4")){
                    this.enviarMensaje("Telegram", "Envia video,"+mensaje.getContent(), ACLMessage.REQUEST);
                }
                else if(mensaje.getContent().contains("Proceso multimedia terminado")){
                    this.enviarMensaje("Mov", "ok", ACLMessage.INFORM);
                }
                else if(mensaje.getContent().contains("Comienza a llover")){
                    if(offline){
                        this.enviarMensaje("Persiana", "Estado persiana", ACLMessage.QUERY_IF);
                        ACLMessage mensaje2 = new ACLMessage();
                        mensaje2=this.receiveACLMessage();
                        System.out.println("Recibo un mensaje de "+mensaje2.getSender()+"  :"+mensaje2.getContent());
                        if(mensaje2.getContent().contains("Persiana abierta")){
                            estado_anterior_persiana="Abierta";
                            this.enviarMensaje("Persiana", "baja", ACLMessage.REQUEST);
                            this.enviarMensaje("Telegram", "Ha comenzado a llover, la persiana estaba abierta y se ha cerrado", ACLMessage.INFORM);
                        }
                        else {
                            estado_anterior_persiana="Cerrada";
                            this.enviarMensaje("Telegram", "Ha comenzado a llover, la persiana estaba cerrada", ACLMessage.INFORM);
                        }
                    }

                }
                else if(mensaje.getContent().contains("Ya no llueve")){
                    if(estado_anterior_persiana=="Abierta"){
                        this.enviarMensaje("Persiana", "sube", ACLMessage.REQUEST);
                        this.enviarMensaje("Telegram", "Ha parado de llover, la persiana anteriormente estaba abierta y se ha abierto de nuevo", ACLMessage.INFORM);
                    }
                    else{
                        this.enviarMensaje("Telegram", "Ha parado de llover, la persiana estaba cerrada", ACLMessage.INFORM);
                    }
                } 
                else if(mensaje.getContent().contains("Persiana")){
                        if(mensaje.getContent().contains("cerrada")){
                            if(accion.contains("Subir")){
                                this.enviarMensaje("Persiana", "sube", ACLMessage.REQUEST);
                                this.enviarMensaje(emisorPeticion, "Se ha subido la persiana", ACLMessage.INFORM);
                            }
                            else{
                                this.enviarMensaje(emisorPeticion, "La persiana ya se encuentra bajada", ACLMessage.INFORM);
                            }
                        }
                        else{
                            if(accion.contains("Subir")){
                                this.enviarMensaje(emisorPeticion, "La persiana ya se encuentra subida", ACLMessage.INFORM);
                            }
                            else{
                                this.enviarMensaje("Persiana", "baja", ACLMessage.REQUEST);
                                this.enviarMensaje(emisorPeticion, "Se ha bajado la persiana", ACLMessage.INFORM);
                            }
                        }
                }
                else if(mensaje.getContent().contains("Cochera")){
                        if(mensaje.getContent().contains("cerrada")){
                            if(accion.contains("Abrir")){
                                this.enviarMensaje("Cochera", "abre", ACLMessage.REQUEST);
                                this.enviarMensaje(emisorPeticion, "La cochera se ha abierto", ACLMessage.INFORM);
                            }
                            else{
                                this.enviarMensaje(emisorPeticion, "La cochera ya se encuentra cerrada", ACLMessage.INFORM);
                            }
                        }
                        else{
                            if(accion.contains("Abrir")){
                                this.enviarMensaje(emisorPeticion, "La cochera ya se encuentra abierta", ACLMessage.INFORM);
                            }
                            else{
                                this.enviarMensaje("Cochera", "cierra", ACLMessage.REQUEST);
                                this.enviarMensaje(emisorPeticion, "Se ha cerrado la cochera", ACLMessage.INFORM);
                            }
                        }
                }
                else if(mensaje.getContent().contains("Temperatura") || mensaje.getContent().contains("Humedad")){
                    this.enviarMensaje(emisorConsulta, mensaje.getContent(), ACLMessage.INFORM);
                }
                else if(mensaje.getContent().contains("lloviendo")){
                    this.enviarMensaje(emisorConsulta, mensaje.getContent(), ACLMessage.INFORM);
                }
                else {
                    this.enviarMensaje(emisor, "No te comprendo", ACLMessage.NOT_UNDERSTOOD);
                }
                break;
            case "QUERY-IF": 
                emisorConsulta=emisor;
                if(mensaje.getContent().contains("Estado Offline")){
                    if(offline){
                        this.enviarMensaje(emisor, "Activado", ACLMessage.INFORM);
                    }
                    else{
                        this.enviarMensaje(emisor, "Desactivado", ACLMessage.INFORM);
                    }
                }
                else if(mensaje.getContent().contains("llueve")){
                    this.enviarMensaje("Lluvia", "llueve", ACLMessage.QUERY_IF);
                }
                else if(mensaje.getContent().contains("temperatura")){
                  //Consultar la temperatura al sensor
                  this.enviarMensaje("Temperatura", "temperatura", ACLMessage.QUERY_IF);
                  
                } 
                else if(mensaje.getContent().contains("humedad")){
                  //Consultar la humedad
                  this.enviarMensaje("Temperatura", "humedad", ACLMessage.QUERY_IF);
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
    
    private void activarOffline() throws InterruptedException{
        if(!offline){
            offline=true;
            this.enviarMensaje("Led", "enciende", ACLMessage.REQUEST);
            this.enviarMensaje("Cochera", "Estado cochera", ACLMessage.QUERY_IF);
            ACLMessage mensaje2 = new ACLMessage();
            mensaje2=this.receiveACLMessage();
            System.out.println("Recibo un mensaje de "+mensaje2.getSender()+"  :"+mensaje2.getContent());
            if(mensaje2.getContent().contains("Cochera abierta")){
                this.enviarMensaje("Cochera", "cierra", ACLMessage.REQUEST);
                this.enviarMensaje("Telegram", "Te has dejado la cochera abierta y se ha cerrado automáticamente", ACLMessage.INFORM);
            }
        }
    }
    
    private void desactivarOffline(){
        if(offline){
            offline=false;
            this.enviarMensaje("Led", "apaga", ACLMessage.REQUEST);
        }
    }
    
}
