/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package TFG;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import TFG.Agente;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 *
 * @author marcos
 */
public class Principal {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // TODO code application logic here
        Agente a;
        AgentsConnection.connect("localhost", 5672, "test", "guest", "guest", false);
        try {
            a = new Agente(new AgentID("Marcos"));
            a.start();
        } catch (Exception ex) {
            System.out.println("Error al crear el agente");
        }
    }
    
}
