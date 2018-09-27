/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cochera;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

/**
 *
 * @author pi
 */
public class Cochera {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic hereAgente a;
        Agente a;
        AgentsConnection.connect("localhost", 5672, "test", "guest", "guest", false);
        try {
            a = new Agente(new AgentID("Cochera"));
            a.start();
        } catch (Exception ex) {
            System.out.println("Error al crear el agente");
        }
    }
    
}
