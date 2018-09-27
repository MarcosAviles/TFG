/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package temperatura;

import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioUtil;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private int temperatura;
    private int humedad;
    private Queue<ACLMessage> cola;

    
    public Agente(AgentID aid) throws Exception{
        super(aid);
        salir=false;
        temperatura=0;
        humedad=0;
        cola=new LinkedList<ACLMessage>();
        
    }
    
    @Override
    public void onMessage(ACLMessage msg){
        cola.add(msg);
    }
    
    @Override
    public void execute(){
            while(!salir){
                while(cola.isEmpty()){
                    try {
                        System.out.println("Temperatura :"+temperatura+" Humedad: "+humedad);
                        this.obtenerTemperatura();
                    } catch (IOException ex) {
                        Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
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
                if(mensaje.getContent().contains("temperatura")){
                  //Enviar mensaje de la temperatura actual.
                  this.enviarMensaje(emisor, "La Temperatura actual es: "+temperatura+" ºC", ACLMessage.INFORM);
                } 
                else if(mensaje.getContent().contains("humedad")){
                  //Enviar mensaje de la humedad actual. 
                  this.enviarMensaje(emisor, "La Humedad actual es: "+humedad+" %", ACLMessage.INFORM);
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
    
    
    public void getTemperature() {
        int MAXTIMINGS  = 85;
        int[] dht11_dat   = { 0, 0, 0, 0, 0 };
        /*if (Gpio.wiringPiSetup() == -1) {
            System.out.println(" ==>> GPIO SETUP FAILED");
            return;
        }*/
        
        GpioUtil.export(3, GpioUtil.DIRECTION_OUT);
        int laststate = Gpio.HIGH;
        int j = 0;
        dht11_dat[0] = dht11_dat[1] = dht11_dat[2] = dht11_dat[3] = dht11_dat[4] = 0;

        Gpio.pinMode(4, Gpio.OUTPUT);
        Gpio.digitalWrite(4, Gpio.LOW);
        Gpio.delay(18);

        Gpio.digitalWrite(4, Gpio.HIGH);
        Gpio.pinMode(4, Gpio.INPUT);
        Gpio.delayMicroseconds(7); // No se si funciona sino hay que quitarlo
        for (int i = 0; i < MAXTIMINGS; i++) {
            int counter = 0;
            while (Gpio.digitalRead(4) == laststate) {
                counter++;
                Gpio.delayMicroseconds(1);
                if (counter == 255) {
                    break;
                }
            }

            laststate = Gpio.digitalRead(4);

            if (counter == 255) {
                break;
            }

            /* ignore first 3 transitions */
            if (i >= 4 && i % 2 == 0) {
                /* shove each bit into the storage bytes */
                dht11_dat[j / 8] <<= 1;
                if (counter > 30) { // o 16 si no funciona
                    dht11_dat[j / 8] |= 1;
                }
                j++;
            }
        }
        // check we read 40 bits (8bit x 5 ) + verify checksum in the last
        // byte
        if (j >= 40 && checkParity(dht11_dat)) {
            float h = (float) ((dht11_dat[0] << 8) + dht11_dat[1]) / 10;
            if (h > 100) {
                h = dht11_dat[0]; // for DHT11
            }
            float c = (float) (((dht11_dat[2] & 0x7F) << 8) + dht11_dat[3]) / 10;
            if (c > 125) {
                c = dht11_dat[2]; // for DHT11
            }
            if ((dht11_dat[2] & 0x80) != 0) {
                c = -c;
            }
            final float f = c * 1.8f + 32;
            humedad=(int) h;
            temperatura=(int) c;
       
            System.out.println("Humidity = " + h + " Temperature = " + c + "(" + f + "f)");
        } else {
            System.out.println("Data not good, skip");
        }

    }
    
    private boolean checkParity(int dht11_dat[]) {
        return dht11_dat[4] == (dht11_dat[0] + dht11_dat[1] + dht11_dat[2] + dht11_dat[3] & 0xFF);
    }
    
    private void obtenerTemperatura() throws IOException, InterruptedException{
        String line;
	int humidity=0;
        int temperature=0;
        Runtime rt= Runtime.getRuntime();
        Process p=rt.exec("python /home/pi/Desktop/temperatura.py");
        BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
	if((line = bri.readLine()) != null){
            if(!(line.contains("ERR_CRC") || line.contains("ERR_RNG"))){
		String[] data=line.split(",");
                String[] data2=data[0].split(":");
                String[] data3=data[1].split(":");
		humedad=Integer.parseInt(data2[1]);
		temperatura=Integer.parseInt(data3[1]);
            }
            else{
                System.out.println("Data Error");
            } 
		
	}
	  
	bri.close();
      	p.waitFor();
    }
    
}
