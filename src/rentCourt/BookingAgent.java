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
                        boolean duplicate = false;
                        try
                        {   // Deserialize object that contain booking info
                            booking = (BookingDetail)Serializer.deserializeObjectFromString(msg.getContent()); 
                            
                            for(BookingDetail b: bookings) 
                            {
                                if(b.getCourtType().equals(booking.getCourtType()) && b.getCourtNumber() == booking.getCourtNumber() && b.getTimeSlot() == booking.getTimeSlot()) 
                                {
                                    duplicate = true;
                                }
                            }
                            
                            if (duplicate) // Detect duplication in court
                            {
                                ACLMessage reply = new ACLMessage(ACLMessage.FAILURE);
                                reply.setContent("Fail to book court, duplicate entry found!");
                                reply.addReceiver(msg.getSender());
                                send(reply);
                            } 
                            else 
                            {
                                ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
                                bookings.add(booking);;
                                reply.setContent("Booking Added Successfully");
                                reply.addReceiver(msg.getSender());
                                send(reply);                 

                                
                            }
                            
                        }
                        catch (Exception ex){}
                        
                        
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
