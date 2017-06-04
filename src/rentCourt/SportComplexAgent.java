/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rentCourt;

import Util.Const;
import bean.BookingDetail;
import bean.SportComplex;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author kingw
 */
public class SportComplexAgent extends Agent {
    SportComplex complex;
    
    protected void setup() {
        
        //Type of Room and Place
        Object[] args = getArguments();
        
        //set room type
	String type = (String) args[0];
        complex.setName(type);
        
  	try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName(Const.SERVICE_NAME);
            sd.setType("complex");
            dfd.addServices(sd);
  		
            DFService.register(this, dfd);
  	}
  	catch (FIPAException fe) {
            fe.printStackTrace();
  	}
        
        addBehaviour(new CyclicBehaviour(this) {
            public void action() { 
                block();
            }
        });
    }
}
