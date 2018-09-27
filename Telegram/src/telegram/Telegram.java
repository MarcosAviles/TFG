/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegram;

import es.upv.dsic.gti_ia.core.AgentsConnection;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 *
 * @author pi
 */
public class Telegram {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        AgentsConnection.connect("localhost", 5672, "test", "guest", "guest", false);
        ApiContextInitializer.init();
 
	// Se crea un nuevo Bot API
	final TelegramBotsApi myBot = new TelegramBotsApi();
 
	try{
            // Se registra el bot
            myBot.registerBot(new Bot());
            
	} 
        catch (TelegramApiException e) {
            e.printStackTrace();
	}
    }
    
}
