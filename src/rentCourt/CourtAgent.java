
package rentCourt;

import Util.Serializer;
import Util.ServiceInitializer;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CourtAgent extends Agent{
    
    private List<Court> courts = new ArrayList<>();
    
    protected void setup(){
        ServiceInitializer.initialize(this, "CourtAgent", "DataType");
        addBehaviour(new CyclicBehaviour(){
            @Override
            public void action() {
                ACLMessage msg = receive();
                if(msg!=null)
                {
                    if(msg.getPerformative()==ACLMessage.REQUEST)
                    {
                        Court court;
                        try
                        {
                            court = (Court)Serializer.deserializeObjectFromString(msg.getContent()); 
                            courts.add(court);
                        }
                        catch (Exception ex){
                        }
                        ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
                        reply.setContent("Court Added Successfully");
                        reply.addReceiver(msg.getSender());
                        send(reply);
                     }
                     else if(msg.getPerformative()==ACLMessage.INFORM){
                        String msgContent;
                        try {
                           msgContent = Serializer.serializeObjectToString(courts);
                           ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                           reply.setContent(msgContent);
                           reply.addReceiver(msg.getSender());
                           send(reply);
                        } catch (IOException ex) {
                            Logger.getLogger(CourtAgent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                     }
                 }
                 block();
            }
        });
    }
}
