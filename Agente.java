
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package camara;

import com.hopding.jrpicam.RPiCamera;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author marcos
 */
public class Agente extends SingleAgent {
        
    boolean seguir;
    //RPiCamera camara;
    
    public Agente(AgentID aid) throws Exception{
        super(aid);
        seguir=true;
        //camara = new RPiCamera("/home/pi/Desktop");
        //camara.setQuality(100);
    }
    
    @Override
    public void execute(){
        while(seguir){
            try {
                this.recibirMensaje();
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
    private void recibirMensaje() throws InterruptedException, IOException{
        ACLMessage mensaje = new ACLMessage();
        mensaje=this.receiveACLMessage();
        System.out.println("Recibo un mensaje de "+mensaje.getSender()+"  :"+mensaje.getContent());
        String emisor=this.procesarSender(mensaje.getSender());
        String perfomativa=mensaje.getPerformative();
        switch(perfomativa){
            case "REQUEST": 
                if(mensaje.getContent().contains("foto sensor")){
                    /*Date fecha=new Date();
                    DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                    String nombre=hourdateFormat.format(fecha);
                    camara.takeStill(fecha+".jpg",720,480);*/
                    String ruta=this.hacerFoto();
                    this.enviarMensaje(emisor, ruta, ACLMessage.INFORM);
                    ruta=this.hacerVideo();
                    this.enviarMensaje(emisor, "Proceso multimedia terminado", ACLMessage.INFORM);
                    this.enviarMensaje(emisor, ruta, ACLMessage.INFORM);
                }
                else if(mensaje.getContent().contains("Hacer foto")){
                    String ruta=this.hacerFoto();
                    this.enviarMensaje(emisor, ruta, ACLMessage.INFORM);
                }
                else if(mensaje.getContent().contains("Hacer video")){
                    String ruta=this.hacerVideo();
                    this.enviarMensaje(emisor, ruta, ACLMessage.INFORM);
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
    
    private String hacerFoto() throws IOException{
        String line;
        Process p= Runtime.getRuntime().exec("/usr/bin/python2.7 /home/pi/Desktop/foto.py");
        BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
	if((line = bri.readLine()) != null){
            if(!(line.contains("ERR_CRC") || line.contains("ERR_RNG"))){
		
            }
            else{
                System.out.println("Data Error");
            } 
		
	}
	  
	bri.close();
        return line;
    }
    
    private String hacerVideo() throws IOException{
        String line;
        Process p= Runtime.getRuntime().exec("/usr/bin/python2.7 /home/pi/Desktop/video.py");
        BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
	if((line = bri.readLine()) != null){
            if(!(line.contains("ERR_CRC") || line.contains("ERR_RNG"))){
		
            }
            else{
                System.out.println("Data Error");
            } 
		
	}
	  
	bri.close();
        return line;
    }
    
}
