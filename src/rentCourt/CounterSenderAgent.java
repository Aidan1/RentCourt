
package rentCourt;

import bean.Court;
import bean.BookingDetail;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.codec.binary.Base64;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CounterSenderAgent extends Agent{
    
    private CounterGUI counterGUI;
    private Map<AID, String> agentMap;
    
    protected void setup(){
        counterGUI = new CounterGUI (this);
        counterGUI.ShowGUI();
        
        initializeAgent();
        
        addBehaviour(new CyclicBehaviour(){
            @Override
            public void action() {
                ACLMessage msg = receive();
                if(msg!=null){
                    String senderName = agentMap.get(msg.getSender());
                    switch(senderName) {
                        case "BookingAgent":
                            if(msg.getPerformative()==ACLMessage.INFORM){
                                
                            } else if(msg.getPerformative()==ACLMessage.CONFIRM) {
                                
                            }
                            break;
                        case "CourtAgent":
                            if(msg.getPerformative()==ACLMessage.INFORM){
                                
                            } else if(msg.getPerformative()==ACLMessage.CONFIRM) {
                                
                            }
                            break;
                        case "SearchAgent":
                            if(msg.getPerformative()==ACLMessage.INFORM){
                                
                            }
                            break;
                    }
                }
                block();
            }
        });
    }
    
    public void initializeAgent() {
        counterGUI.AppendLog("Connecting to Agent...");
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription templateSd = new ServiceDescription();
            templateSd.setType("DataType");
            template.addServices(templateSd);
            templateSd = new ServiceDescription();
            templateSd.setType("Search");
            template.addServices(templateSd);

            SearchConstraints sc = new SearchConstraints();
            sc.setMaxResults(new Long(10));

            DFAgentDescription[] results = DFService.search(this, template, sc);
            if (results.length > 0) {
                for (int i = 0; i < results.length; ++i) {
                    DFAgentDescription dfd = results[i];
                    AID provider = dfd.getName();
                    jade.util.leap.Iterator it = dfd.getAllServices();
                    while (it.hasNext()) {
                        ServiceDescription sd = (ServiceDescription) it.next();
                        agentMap.put(provider, sd.getName());
                        counterGUI.AppendLog("Connected to " + sd.getName());
                    }
                }
            }
            if(agentMap.size() != 3) {
                counterGUI.AppendLog("Fail to connect to all agent, please start all required agent and restart the program");
            } else {
                counterGUI.AppendLog("Successfully connected to all agent. Enjoy the program.");
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}
