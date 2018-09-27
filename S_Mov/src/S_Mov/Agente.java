
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package S_Mov;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
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
    
    private final GpioController gpio; 
    private GpioPinDigitalInput Pin;
    boolean disponible;
        
    
    public Agente(AgentID aid) throws Exception{
        super(aid);
        gpio = GpioFactory.getInstance();
        Pin = gpio.provisionDigitalInputPin (RaspiPin.GPIO_21);
        Pin.setShutdownOptions(true);
        disponible=true;
    }
    
    @Override
    public void execute(){
        try {
            this.ActivarSensor();
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
            case "INFORM":
                if(mensaje.getContent().contains("ok")){
                    disponible=true;
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
    
    private void ActivarSensor() throws InterruptedException{
        

        // create and register gpio pin listener
        Pin.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                if(gpio.isLow(Pin) && disponible){
                    disponible=false;
                    enviarMensaje("Marcos", "Intruso", ACLMessage.INFORM);
                    try {
                        recibirMensaje();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }

        });
        
        while(true){
            Thread.sleep(500);
        }

    }
    
}
