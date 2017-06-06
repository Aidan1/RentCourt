package rentCourt;

import bean.Court;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import Util.Serializer;
import Util.ServiceInitializer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jade.lang.acl.ACLMessage;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

public class CourtAgent extends Agent
{
    private List<Court> courts = new ArrayList<>();
    
    protected void setup()
    {
        ServiceInitializer.initialize(this, "CourtAgent", "DataType");
        addBehaviour(new CyclicBehaviour()
        {
            @Override
            public void action() 
            {
                ACLMessage msg = receive();
                if (msg != null)
                {
                    if (msg.getPerformative() == ACLMessage.REQUEST)
                    {
                        Court court;
                        boolean duplicate = false;
                        try
                        {
                            court = (Court)Serializer.deserializeObjectFromString(msg.getContent());
                            for(Court c: courts) 
                            {
                                if(c.getCourtType().equals(court.getCourtType()) && c.getCourtNumber() == court.getCourtNumber()) 
                                {
                                    duplicate = true;
                                }
                            }
                            if (duplicate) // Detect duplication in court
                            {
                                ACLMessage reply = new ACLMessage(ACLMessage.FAILURE);
                                reply.setContent("Fail to add court, duplicate entry found!");
                                reply.addReceiver(msg.getSender());
                                send(reply);
                            } 
                            else 
                            {
                                ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
                                reply.setContent("Court Added Successfully");
                                reply.addReceiver(msg.getSender());
                                send(reply);
                                courts.add(court);
                            }
                        }
                        catch (Exception ex){}
                     }
                     else if (msg.getPerformative() == ACLMessage.INFORM)
                     {
                        String msgContent;
                        try 
                        {
                           msgContent = Serializer.serializeObjectToString(courts);
                           ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                           reply.setContent(msgContent);
                           reply.addReceiver(msg.getSender());
                           send(reply);
                        } 
                        catch (IOException ex) 
                        {
                            Logger.getLogger(CourtAgent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                block();
            }
        });
    }
}
