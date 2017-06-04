
package rentCourt;

import Util.Serializer;
import bean.Court;
import bean.BookingDetail;
import bean.SearchRequest;
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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CounterSenderAgent extends Agent{
    
    private CounterGUI counterGUI;
    private AID searchAgent, courtAgent, bookingAgent;
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
                    if(msg.getSender() == bookingAgent) {
                        if(msg.getPerformative()==ACLMessage.INFORM){
                            List<BookingDetail> bookings;
                            try {
                                bookings = (List<BookingDetail>)Serializer.deserializeObjectFromString(msg.getContent()); 
                                counterGUI.AppendLog("Received " + bookings.size() + " booking(s).");
                                counterGUI.listBooking(bookings);
                            }
                            catch (Exception ex){
                            }
                        } else if(msg.getPerformative()==ACLMessage.CONFIRM) {
                            counterGUI.AppendLog(msg.getContent());
                        }
                    } else if (msg.getSender() == courtAgent) {
                        if(msg.getPerformative()==ACLMessage.INFORM){
                            List<Court> courts;
                            try {
                                courts = (List<Court>)Serializer.deserializeObjectFromString(msg.getContent()); 
                                counterGUI.AppendLog("Received " + courts.size() + " court(s).");
                                counterGUI.listCourt(courts);
                            }
                            catch (Exception ex){
                            }
                        } else if(msg.getPerformative()==ACLMessage.CONFIRM) {
                            counterGUI.AppendLog(msg.getContent());
                        }
                    } else if (msg.getSender() == searchAgent) {
                        if(msg.getPerformative()==ACLMessage.INFORM){
                            List<Court> courts;
                            try {
                                courts = (List<Court>)Serializer.deserializeObjectFromString(msg.getContent()); 
                                counterGUI.AppendLog("Found " + courts.size() + " available court(s).");
                                counterGUI.listSearch(courts);
                            }
                            catch (Exception ex){
                            }
                        }
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
                        if(sd.getName().equals("BookingAgent")) {
                            bookingAgent = provider;
                            counterGUI.AppendLog("Connected to " + sd.getName());
                        } else if(sd.getName().equals("CourtAgent")) {
                            courtAgent = provider;
                            counterGUI.AppendLog("Connected to " + sd.getName());
                        } else if(sd.getName().equals("SearchAgent")) {
                            searchAgent = provider;
                            counterGUI.AppendLog("Connected to " + sd.getName());
                        }
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
    
    public void searchCourt(int timeSlot, String courtType) {
        SearchRequest request = new SearchRequest();
        request.setTimeSlot(timeSlot);
        request.setCourtType(courtType);
        request.setRequester(this.getAID());
        
        try {
            ACLMessage search = new ACLMessage(ACLMessage.REQUEST);
            search.setContent(Serializer.serializeObjectToString(request));
            search.addReceiver(searchAgent);
            send(search);
        } catch (IOException ex) {
            Logger.getLogger(CounterSenderAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void newCourt(String courtType, int courtNumber) {
        Court c = new Court();
        c.setCourtNumber(courtNumber);
        c.setCourtType(courtType);
        
        try {
            ACLMessage search = new ACLMessage(ACLMessage.REQUEST);
            search.setContent(Serializer.serializeObjectToString(c));
            search.addReceiver(courtAgent);
            send(search);
        } catch (IOException ex) {
            Logger.getLogger(CounterSenderAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void newBooking(int timeSlot, String courtType, int courtNumber, String matricNo) {
        BookingDetail b = new BookingDetail();
        b.setCourtNumber(courtNumber);
        b.setCourtType(courtType);
        b.setMatricNo(matricNo);
        b.setTimeSlot(timeSlot);
        
        try {
            ACLMessage search = new ACLMessage(ACLMessage.REQUEST);
            search.setContent(Serializer.serializeObjectToString(b));
            search.addReceiver(bookingAgent);
            send(search);
        } catch (IOException ex) {
            Logger.getLogger(CounterSenderAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void getCourt() {
        ACLMessage search = new ACLMessage(ACLMessage.INFORM);
        search.addReceiver(courtAgent);
        send(search);
    }
    
    public void getBooking() {
        ACLMessage search = new ACLMessage(ACLMessage.INFORM);
        search.addReceiver(bookingAgent);
        send(search);
    }
}
