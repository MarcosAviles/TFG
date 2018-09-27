/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegram;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.send.SendVideo;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
/**
 *
 * @author marcos
 */
public class Bot extends TelegramLongPollingBot{
 
    
    private LinkedList<Long> usuarios; 
    private Agente a;
    private String clave;

    public Bot() throws Exception {
        super();
        usuarios = new LinkedList<Long>();
        a = new Agente(new AgentID("Telegram"));
        a.start();
        clave= "1234";
        a.asignarBot(this);
        
    }
    
      
    
    
    
    @Override
    public void onUpdateReceived(final Update update) {
            // Esta función se invocará cuando nuestro bot reciba un mensaje

            // Se obtiene el mensaje escrito por el usuario
            String mensajeRecibido = update.getMessage().getText();
            
            // Se obtiene el id de chat del usuario
            long chatId = update.getMessage().getChatId();
            SendMessage mensaje = new SendMessage().setChatId(chatId);
            if(usuarioRegistrado(chatId)){
                switch (mensajeRecibido){
                    case "/start":
                        this.enviarMensaje(chatId, "Bienvenido de nuevo, usted ya se encuentra registrado en el sistema.");
                        break;
                    case "/hacer_foto": 
                        try {
                            a.solicitarMultimedia("foto");
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "/obtener_video": 
                        try {
                            a.solicitarMultimedia("video");
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "/estado_seguridad":
                        try {
                            a.estadoOffline(chatId, "");
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "/fuera_de_casa":
                        try {
                            a.estadoOffline(chatId, "Activar");
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "/estoy_en_casa":
                        try {
                            a.estadoOffline(chatId, "Desactivar");
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "/subir_persiana":
                        try {
                            a.accionarPersiana(chatId, "Subir");
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
		    case "/bajar_persiana":
                        try {
                            a.accionarPersiana(chatId, "Bajar");
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "/cerrar_cochera":
                        try {
                            a.accionarCochera(chatId, "Cerrar");
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "/abrir_cochera":
                        try {
                            a.accionarCochera(chatId, "Abrir");
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "/estado_lluvia":
                        try {
                            a.estadoLluvia(chatId);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "/temperatura":
                        try {
                            a.temperatura(chatId);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    default: 
                        this.enviarMensaje(chatId, "Lo siento no estoy preparado aún, o no le comprendo");
                        break;
                }
            }
            else{
                if(mensajeRecibido.equals(clave)){
                    usuarios.add(chatId);
                    String salida="Registrado con éxito.";
                    Date fecha = new Date();
                    DateFormat formato = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
                    salida+=formato.format(fecha);
                    this.enviarMensaje(salida);
                    
                }
                else if(mensajeRecibido.equals("/start")){
                        this.enviarMensaje(chatId, "Bienvenido a DomoticsAgents, por favor introduzca la clave");
                }
                else{
                    this.enviarMensaje(chatId, "Por favor introduzca la clave");
                }
                
                
            }
            
    }

    @Override
    public String getBotUsername() {
            // Se devuelve el nombre que dimos al bot al crearlo con el BotFather
            return "DomoticsAgentsBot";
    }

    @Override
    public String getBotToken() {
            // Se devuelve el token que nos generó el BotFather de nuestro bot
            return "584124852:AAEcg8nSewrq7vXoAgrUl0mqx2m57mqTovg";
    }
    
    public boolean usuarioRegistrado(long Id ){
        return usuarios.contains(Id);
    }
    
    
    public void enviaFoto(String ruta, String descripcion) throws TelegramApiException{
        for(int i=0; i<usuarios.size(); i++){
            SendPhoto msg = new SendPhoto()
                        .setChatId(usuarios.get(i)).setNewPhoto(new File(ruta))
                        .setCaption(descripcion);
        sendPhoto(msg);
        }
        
    }
    
    public void enviaVideo(String ruta, String descripcion) throws TelegramApiException{
        for(int i=0; i<usuarios.size(); i++){
            SendVideo msg = new SendVideo()
                        .setChatId(usuarios.get(i)).setNewVideo(new File(ruta))
                        .setCaption(descripcion);
        sendVideo(msg);
        }
        
    }
    
    public void enviarMensaje(long usuario, String descripcion){
        SendMessage mensaje = new SendMessage().setChatId(usuario);
        mensaje.setText(descripcion);
        try {
            // Se envía el mensaje
            execute(mensaje);
        } 
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    public void enviarMensaje(String descripcion){
        for(int i=0; i<usuarios.size(); i++){
            SendMessage mensaje = new SendMessage().setChatId(usuarios.get(i));
            mensaje.setText(descripcion);
            try {
            // Se envía el mensaje
                execute(mensaje);
            } 
            catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

}
    


