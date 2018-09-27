/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package lluvia;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;
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
    
    private boolean salir;
    private boolean lluvia;
    private boolean cambio;
    private Queue<ACLMessage> cola;
    private final GpioController gpio; 
    private GpioPinDigitalInput Pin;

    
    public Agente(AgentID aid) throws Exception{
        super(aid);
        salir=false;
        lluvia=false;
        cola=new LinkedList<ACLMessage>();
        gpio = GpioFactory.getInstance();
        Pin = gpio.provisionDigitalInputPin (RaspiPin.GPIO_00);
        Pin.setShutdownOptions(true);
        
    }
    
    @Override
    public void onMessage(ACLMessage msg){
        cola.add(msg);
    }
    
    @Override
    public void execute(){
            while(!salir){
                while(cola.isEmpty()){
                    this.checkLluvia();
                    if(lluvia && !cambio){
                        System.out.println("Llueve");
                        //Informar al controlador que esta lloviendo
                        this.enviarMensaje("Marcos", "Comienza a llover", ACLMessage.INFORM);
                        cambio=true;
                    }
                    if(!lluvia && cambio){
                        this.enviarMensaje("Marcos", "Ya no llueve", ACLMessage.INFORM);
                        cambio=false;
                    }
                  
                    try {
                        Thread.sleep(3000);
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
                } 
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
    private void recibirMensaje(ACLMessage mensaje) throws InterruptedException, IOException{
        System.out.println("Recibo un mensaje de "+mensaje.getSender()+"  :"+mensaje.getContent());
        String emisor=this.procesarSender(mensaje.getSender());
        String perfomativa=mensaje.getPerformative();
        switch(perfomativa){
            case "QUERY-IF": 
                if(mensaje.getContent().contains("llueve")){
                  //Enviar mensaje si está lloviendo o no
                  if(lluvia) this.enviarMensaje(emisor, "Esta lloviendo", ACLMessage.INFORM);
                  else this.enviarMensaje(emisor, "No esta lloviendo", ACLMessage.INFORM);
                } 
                else{
                  this.enviarMensaje(emisor, "No te comprendo", ACLMessage.NOT_UNDERSTOOD);
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
    
    
    private void checkLluvia(){
        if(gpio.isLow(Pin)) lluvia=true;
        else lluvia=false;
    }
    
}
