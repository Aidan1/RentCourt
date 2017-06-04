/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rentCourt;

import Util.Serializer;
import Util.ServiceInitializer;
import bean.BookingDetail;
import bean.Court;
import bean.SearchRequest;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kingw
 */
public class SearchAgent extends Agent {
    
    private AID bookingAgent;
    private AID courtAgent;
    
    List<Court> courts;
    List<BookingDetail> bookings;
    
    SearchRequest filter;
    AID searchRequester;
    
    protected void setup() {
        
        ServiceInitializer.initialize(this, "SearchAgent", "Search");
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription templateSd = new ServiceDescription();
            templateSd.setType("DataType");
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
                        if (sd.getName().equals("BookingAgent")) {
                            bookingAgent = provider;
                        } else if (sd.getName().equals("CourtAgent")) {
                            courtAgent = provider;
                        }
                    }
                }
            }
            if(bookingAgent == null || courtAgent == null) {
                throw new Exception("Fail to find booking or court Agent");
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        } catch (Exception ex) {
            Logger.getLogger(SearchAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() { 
                ACLMessage msg = receive();
                if (msg!= null) {                    
                    if(msg.getPerformative()==ACLMessage.REQUEST) {
                        try
                        {
                            filter = (SearchRequest)Serializer.deserializeObjectFromString(msg.getContent()); 
                            searchRequester = msg.getSender();
                        }
                        catch (Exception ex){
                        }
                        ACLMessage getData = new ACLMessage(ACLMessage.INFORM);
                        getData.addReceiver(bookingAgent);
                        getData.addReceiver(courtAgent);
                        send(getData);
                    } else if(msg.getPerformative()==ACLMessage.INFORM) {
                        if(msg.getSender().equals(bookingAgent)) {
                            try {
                                bookings = (List<BookingDetail>)Serializer.deserializeObjectFromString(msg.getContent()); 
                            }
                            catch (Exception ex){
                            }
                        } else if (msg.getSender().equals(courtAgent)) {
                            try {
                                courts = (List<Court>)Serializer.deserializeObjectFromString(msg.getContent()); 
                            }
                            catch (Exception ex){
                            }
                        }
                        if(bookings != null && courts != null && filter != null) {
                            List<BookingDetail> filteredBooking = new ArrayList();
                            List<Court> noAvailable = new ArrayList();
                            
                            for(BookingDetail b: bookings) {
                                if(b.getTimeSlot().equals(filter.getTimeSlot())) {
                                    filteredBooking.add(b);
                                }
                            }
                            for(Court c: courts) {
                                for(BookingDetail b: filteredBooking) {
                                    if(b.getCourtType().equals(c.getCourtType()) && b.getCourtNumber() == c.getCourtNumber()) {
                                        noAvailable.add(c);
                                    }
                                }
                            }
                            for(Court c: courts) {
                                if(!c.getCourtType().equals(filter.getCourtType())) {
                                    noAvailable.add(c);
                                }
                            }
                            List<Court> result = new ArrayList(courts);
                            result.removeAll(noAvailable);
                            try {
                                ACLMessage searchReply = new ACLMessage(ACLMessage.INFORM);
                                searchReply.setContent(Serializer.serializeObjectToString(result));
                                searchReply.addReceiver(searchRequester);
                                send(searchReply);
                            } catch (IOException ex) {
                                Logger.getLogger(CourtAgent.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            bookings = null;
                            courts = null;
                            filter = null;
                        }
                    }
                }
                block();
            }
        });
    }    
}

