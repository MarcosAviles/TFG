
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cochera;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
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
    private GpioPinDigitalOutput Pin1;
    private GpioPinDigitalOutput Pin2;
    private boolean salir;
    private boolean cerrada; 
    
    public Agente(AgentID aid) throws Exception{
        super(aid);
        gpio = GpioFactory.getInstance();
        Pin1 = gpio.provisionDigitalOutputPin (RaspiPin.GPIO_03);
        Pin1.setShutdownOptions(false, PinState.LOW);
        Pin2 = gpio.provisionDigitalOutputPin (RaspiPin.GPIO_05);
        Pin2.setShutdownOptions(false, PinState.LOW);
        salir=false;
        cerrada=true;
    }
    
    @Override
    public void execute(){
        while(!salir){
            try {
                this.recibirMensaje();
            } catch (InterruptedException ex) {
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
    private void recibirMensaje() throws InterruptedException{
        ACLMessage mensaje = new ACLMessage();
        mensaje=this.receiveACLMessage();
        System.out.println("Recibo un mensaje de "+mensaje.getSender()+"  :"+mensaje.getContent());
        String emisor=this.procesarSender(mensaje.getSender());
        String perfomativa=mensaje.getPerformative();
        switch(perfomativa){
            case "REQUEST":
                if(mensaje.getContent().contains("abre") && cerrada){
                    cerrada=false;
                    Pin2.pulse(28,true);
                    for(int i=0; i<2; i++){
                        Pin2.pulse(26,true);
                        Thread.sleep(200);
                    }
                    
                }
                else if(mensaje.getContent().contains("cierra") && !cerrada){
                    cerrada=true;
                    for(int i=0; i<4; i++){
                        Pin1.pulse(10,true);
                        Thread.sleep(200);
                    }
                    Pin2.pulse(10,true);
                }
                else {
                    this.enviarMensaje(emisor, "No te comprendo", ACLMessage.NOT_UNDERSTOOD);
                }
                break;
            case "QUERY-IF":
                if(mensaje.getContent().contains("Estado cochera")){
                    if(cerrada){
                        this.enviarMensaje(emisor, "Cochera cerrada", ACLMessage.INFORM);
                    }
                    else {
                        this.enviarMensaje(emisor, "Cochera abierta", ACLMessage.INFORM);
                    }
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
    
    
    
}
