/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rentCourt;

import bean.Court;
import bean.BookingDetail;
import bean.SearchRequest;
import java.io.IOException;
import Util.Serializer;
import Util.ServiceInitializer;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 *
 * @author kingw
 */
public class SearchAgent extends Agent 
{    
    private AID courtAgent;
    private AID bookingAgent;

    List<Court> courts;
    List<BookingDetail> bookings;
    
    AID searchRequester;
    SearchRequest filter;
    
    protected void setup() 
    {
        ServiceInitializer.initialize(this, "SearchAgent", "Search");
        try 
        {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription templateSd = new ServiceDescription();
            templateSd.setType("DataType");
            template.addServices(templateSd);

            SearchConstraints sc = new SearchConstraints();
            sc.setMaxResults(new Long(10));

            DFAgentDescription[] results = DFService.search(this, template, sc);
            if (results.length > 0) 
            {
                for (int i = 0; i < results.length; ++i) 
                {
                    DFAgentDescription dfd = results[i];
                    AID provider = dfd.getName();
                    jade.util.leap.Iterator it = dfd.getAllServices();
                    while (it.hasNext()) 
                    {
                        ServiceDescription sd = (ServiceDescription) it.next();
                        switch (sd.getName()) 
                        {
                            case "BookingAgent":
                                bookingAgent = provider;
                                break;
                                
                            case "CourtAgent":
                                courtAgent = provider;
                                break;
                        }
                    }
                }
            }
            if (bookingAgent == null || courtAgent == null) 
            {
                throw new Exception("Fail to find booking or court Agent");
            }
        }
        catch (FIPAException fe) 
        {
            fe.printStackTrace();
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(SearchAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        addBehaviour(new CyclicBehaviour(this) 
        {
            @Override
            public void action() 
            { 
                ACLMessage msg = receive();
                if (msg != null) 
                {                    
                    if(msg.getPerformative() == ACLMessage.REQUEST) 
                    {
                        try
                        {
                            filter = (SearchRequest)Serializer.deserializeObjectFromString(msg.getContent()); 
                            searchRequester = msg.getSender();
                        }
                        catch (Exception ex){}
                        
                        ACLMessage getData = new ACLMessage(ACLMessage.INFORM);
                        getData.addReceiver(bookingAgent);
                        getData.addReceiver(courtAgent);
                        send(getData);
                    } 
                    else if (msg.getPerformative() == ACLMessage.INFORM) 
                    {
                        if(msg.getSender().equals(bookingAgent)) 
                        {
                            try 
                            {
                                bookings = (List<BookingDetail>)Serializer.deserializeObjectFromString(msg.getContent()); 
                            }
                            catch (Exception ex){}
                        } 
                        else if (msg.getSender().equals(courtAgent)) 
                        {
                            try 
                            {
                                courts = (List<Court>)Serializer.deserializeObjectFromString(msg.getContent()); 
                            }
                            catch (Exception ex){}
                        }
                        if (bookings != null && courts != null && filter != null) 
                        {
                            List<BookingDetail> filteredBooking = new ArrayList();
                            List<Court> noAvailable = new ArrayList();
                            
                            bookings.stream().filter((b) -> (b.getTimeSlot().equals(filter.getTimeSlot()))).forEach((b) -> {
                                filteredBooking.add(b);
                            });
                            
                            for(Court c: courts) 
                            {
                                for(BookingDetail b: filteredBooking) 
                                {
                                    if(b.getCourtType().equals(c.getCourtType()) && b.getCourtNumber() == c.getCourtNumber()) 
                                    {
                                        noAvailable.add(c);
                                    }
                                }
                            }
                            
                            courts.stream().filter((c) -> (!c.getCourtType().equals(filter.getCourtType()))).forEach((c) -> {
                                noAvailable.add(c);
                            });
                            
                            List<Court> result = new ArrayList(courts);
                            result.removeAll(noAvailable);
                            try 
                            {
                                ACLMessage searchReply = new ACLMessage(ACLMessage.INFORM);
                                searchReply.setContent(Serializer.serializeObjectToString(result));
                                searchReply.addReceiver(searchRequester);
                                send(searchReply);
                            } 
                            catch (IOException ex) 
                            {
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
