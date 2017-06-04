/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

/**
 *
 * @author kingw
 */
public class ServiceInitializer {
    public static void initialize(Agent a, String serviceName, String serviceType){
  	try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(a.getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName(serviceName);
            sd.setType(serviceType);
            dfd.addServices(sd);
            DFService.register(a, dfd);
  	}
  	catch (FIPAException fe) {
            fe.printStackTrace();
  	}
    }
}
