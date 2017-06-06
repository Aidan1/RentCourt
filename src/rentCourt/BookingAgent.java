package rentCourt;

import bean.BookingDetail;
import java.io.IOException;
import Util.Serializer;
import Util.ServiceInitializer;
import jade.lang.acl.ACLMessage;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

public class BookingAgent extends Agent 
{    
    private List<BookingDetail> bookings = new ArrayList<>();
    
    protected void setup() 
    {
        ServiceInitializer.initialize(this, "BookingAgent", "DataType");
        addBehaviour(new CyclicBehaviour(this) 
        {
            @Override
            public void action() 
            { 
                ACLMessage msg = receive(); // Receive message from controller agent
                if (msg != null) 
                {                    
                    if (msg.getPerformative() == ACLMessage.REQUEST) 
                    {
                        BookingDetail booking;
                        try
                        {   // Deserialize object that contain booking info
                            booking = (BookingDetail)Serializer.deserializeObjectFromString(msg.getContent()); 
                            bookings.add(booking);
                        }
                        catch (Exception ex){}
                        
                        ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
                        reply.setContent("Booking Added Successfully");
                        reply.addReceiver(msg.getSender());
                        send(reply); // Reply booked successful message to controller agent
                    } 
                    else if (msg.getPerformative() == ACLMessage.INFORM) 
                    {
                        String msgContent;
                        try 
                        {
                           msgContent = Serializer.serializeObjectToString(bookings);
                           ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                           reply.setContent(msgContent);
                           reply.addReceiver(msg.getSender());
                           send(reply); // Reply with the list of bookings
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
