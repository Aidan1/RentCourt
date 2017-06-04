
package rentCourt;

import Util.Serializer;
import Util.ServiceInitializer;
import bean.Court;
import bean.BookingDetail;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.codec.binary.Base64;

public class BookingAgent extends Agent {
    
    private List<BookingDetail> bookings = new ArrayList<>();
    
    protected void setup() {
        ServiceInitializer.initialize(this, "BookingAgent", "DataType");
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() { 
                ACLMessage msg = receive();
                if (msg!= null) {                    
                    if(msg.getPerformative()==ACLMessage.REQUEST) {
                        BookingDetail booking;
                        try
                        {
                            booking = (BookingDetail)Serializer.deserializeObjectFromString(msg.getContent()); 
                            bookings.add(booking);
                        }
                        catch (Exception ex){
                        }
                        ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
                        reply.setContent("Booking Added Successfully");
                        reply.addReceiver(msg.getSender());
                        send(reply);
                    } else if(msg.getPerformative()==ACLMessage.INFORM) {
                        String msgContent;
                        try {
                           msgContent = Serializer.serializeObjectToString(bookings);
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
