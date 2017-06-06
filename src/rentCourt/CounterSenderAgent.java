package rentCourt;

import bean.Court;
import bean.BookingDetail;
import bean.SearchRequest;
import Util.Serializer;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class CounterSenderAgent extends Agent
{
    private List<Court> searchResult;
    private CounterAgentGUI counterGUI;
    private AID searchAgent, courtAgent, bookingAgent; // List of agents
    
    protected void setup()
    {
        counterGUI = new CounterAgentGUI (this);
        counterGUI.ShowGUI();
        
        initializeAgent();
        
        addBehaviour(new CyclicBehaviour()
        {
            @Override
            public void action() 
            {
                ACLMessage msg = receive();
                if(msg != null)
                {
                    // Identify the agents and perform different operations
                    if(msg.getSender().equals(bookingAgent))
                    {
                        if(msg.getPerformative() == ACLMessage.INFORM)
                        {
                            List<BookingDetail> bookings;
                            try 
                            {
                                bookings = (List<BookingDetail>)Serializer.deserializeObjectFromString(msg.getContent()); 
                                counterGUI.AppendLog("Updated " + bookings.size() + " booking(s).");
                                counterGUI.listBooking(bookings); // Display list of booking in GUI
                            }
                            catch (Exception ex){}
                        } 
                        else if(msg.getPerformative() == ACLMessage.CONFIRM) 
                        {
                            counterGUI.AppendLog(msg.getContent());
                            counterGUI.NextStep();
                        }
                    } 
                    else if (msg.getSender().equals(courtAgent)) 
                    {
                        if(msg.getPerformative() == ACLMessage.INFORM)
                        {
                            List<Court> courts;
                            try 
                            {
                                courts = (List<Court>)Serializer.deserializeObjectFromString(msg.getContent()); 
                                counterGUI.AppendLog("Updated " + courts.size() + " court(s).");
                                counterGUI.listCourt(courts); // Display updated court in GUI
                            }
                            catch (Exception ex){}
                        } 
                        else if(msg.getPerformative() == ACLMessage.CONFIRM) 
                        {
                            counterGUI.AppendLog("*Success* " + msg.getContent());
                        } 
                        else if(msg.getPerformative() == ACLMessage.FAILURE) 
                        {
                            counterGUI.AppendLog("*Fail* " + msg.getContent());
                        }
                    } 
                    else if (msg.getSender().equals(searchAgent)) 
                    {
                        if(msg.getPerformative() == ACLMessage.INFORM)
                        {
                            try 
                            {
                                searchResult = (List<Court>)Serializer.deserializeObjectFromString(msg.getContent()); 
                                counterGUI.AppendLog("Found " + searchResult.size() + " available court(s).");
                                counterGUI.listSearch(searchResult); // Display available court on specific time slot in GUI
                            }
                            catch (Exception ex){}
                        }
                    }
                }
                block();
            }
        });
    }
    
    public void initializeAgent() 
    {
        counterGUI.AppendLog("Connecting to Agent...");
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
                                counterGUI.AppendLog("Connected to " + sd.getName());
                                break;
                                
                            case "CourtAgent":
                                courtAgent = provider;
                                counterGUI.AppendLog("Connected to " + sd.getName());
                                break;
                        }
                    }
                }
            }
            
            template = new DFAgentDescription();
            templateSd = new ServiceDescription();
            templateSd.setType("Search");
            template.addServices(templateSd);

            sc = new SearchConstraints();
            sc.setMaxResults(new Long(10));

            results = DFService.search(this, template, sc);
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
                        if(sd.getName().equals("SearchAgent")) 
                        {
                            searchAgent = provider;
                            counterGUI.AppendLog("Connected to " + sd.getName());
                        }
                    }
                }
            }
            if(searchAgent == null || bookingAgent == null || courtAgent == null) 
            {
                counterGUI.AppendLog("Fail to connect to all agent, please start all required agent and restart the program");
            } 
            else 
            {
                counterGUI.AppendLog("Successfully connected to all agent. Enjoy the program.");
            }
        }
        catch (FIPAException fe) 
        {
            fe.printStackTrace();
        }
    }
    
    public void searchCourt(String timeSlot, String courtType) 
    {
        // Receive request input from user for searching
        SearchRequest request = new SearchRequest();
        request.setTimeSlot(timeSlot);
        request.setCourtType(courtType);
        
        try 
        {
            ACLMessage search = new ACLMessage(ACLMessage.REQUEST);
            search.setContent(Serializer.serializeObjectToString(request));
            search.addReceiver(searchAgent);
            send(search);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(CounterSenderAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void newCourt(String courtType, int courtNumber) 
    {
        // Receive request input from user for adding new court
        Court c = new Court();
        c.setCourtNumber(courtNumber);
        c.setCourtType(courtType);
        try 
        {
            ACLMessage search = new ACLMessage(ACLMessage.REQUEST);
            search.setContent(Serializer.serializeObjectToString(c));
            search.addReceiver(courtAgent);
            send(search);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(CounterSenderAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        getCourt();
    }
    
    public void newBooking(String timeSlot, String courtType, int courtNumber, String matricNo) 
    {
        boolean found = false;
        for(Court c: searchResult) 
        {
            if(courtNumber == c.getCourtNumber()) 
            {
                found = true;
            }
        }
        if(!found) 
        {
            counterGUI.AppendLog("*Error* Please key in the correct court number from the list");
            return;
        }
        BookingDetail b = new BookingDetail();
        b.setCourtNumber(courtNumber);
        b.setCourtType(courtType);
        b.setMatricNo(matricNo);
        b.setTimeSlot(timeSlot);
        
        try 
        {
            ACLMessage search = new ACLMessage(ACLMessage.REQUEST);
            search.setContent(Serializer.serializeObjectToString(b));
            search.addReceiver(bookingAgent);
            send(search);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(CounterSenderAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        getBooking();
    }
    
    public void getCourt() 
    {
        ACLMessage search = new ACLMessage(ACLMessage.INFORM);
        search.addReceiver(courtAgent);
        send(search);
    }
    
    public void getBooking() 
    {
        ACLMessage search = new ACLMessage(ACLMessage.INFORM);
        search.addReceiver(bookingAgent);
        send(search);
    }
}
