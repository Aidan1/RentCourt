
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

public class CounterAgent extends Agent{
    
    static final Base64 base64 = new Base64();
    private CounterAgentGUI counterAgentGUI;
    private ArrayList<Court> courts = new ArrayList<>();
    private ArrayList<AID> salesman = new ArrayList<>();
    private BookingDetail rentdetail;
    
    public String serializeObjectToString(Object object) throws IOException {
        String s = null;
        
        try 
        {
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(arrayOutputStream);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(gzipOutputStream);         
        
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            gzipOutputStream.close();
            
            objectOutputStream.flush();
            objectOutputStream.close();
            
            s = new String(base64.encode(arrayOutputStream.toByteArray()));
            
            arrayOutputStream.flush();
            arrayOutputStream.close();
        }
        catch(Exception ex){
            System.out.println(ex.toString());
        }
        
        return s;
    }
    
    public Object deserializeObjectFromString(String objectString) throws IOException, ClassNotFoundException {
        Object obj = null;
        try
        {    
            ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(base64.decode(objectString));
            GZIPInputStream gzipInputStream = new GZIPInputStream(arrayInputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(gzipInputStream);
            obj =  objectInputStream.readObject();
            
            objectInputStream.close();
            gzipInputStream.close();
            arrayInputStream.close();
        }
        catch(Exception ex){}
        return obj;
    } 
    
    protected void setup(){
        counterAgentGUI = new CounterAgentGUI (this);
        counterAgentGUI.ShowGUI();
        
        addBehaviour(new CyclicBehaviour(){
            @Override
            public void action() {
                ACLMessage msg = receive();
                if(msg!=null){
                    if(salesman.contains(msg.getSender())){
                        if(msg.getPerformative()==ACLMessage.INFORM){
                            String msgContent = msg.getContent();
                            try
                            {
                                courts = (ArrayList)deserializeObjectFromString(msgContent);
                            }
                            catch (Exception ex)
                            {            
                            }

                            if(courts!=null){
                               counterAgentGUI.NextStep();
                               counterAgentGUI.ClearAvailableRoom();
                               counterAgentGUI.AppendBookingLog("Available rooms found by :" + msg.getSender() + "\n");
                               counterAgentGUI.AppendAvailableRoom("No. \t Type \t Place \t Price \t AID"+"\n");
                               for(int i=0;i<courts.size();i++)
                               {
                                   counterAgentGUI.AppendAvailableRoom(i+1 + "\t" + courts.get(i).getCourtType() + "\t" 
                                           + "\t" + courts.get(i).getProvider() + "\n");
                               }
                            }
                        }

                        else if(msg.getPerformative()==ACLMessage.FAILURE){
                            counterAgentGUI.AppendLog("No available room found by the salesman.\n");
                        }
                        
                        else if(msg.getPerformative()==ACLMessage.AGREE){
                            String msgContent = msg.getContent();
                            BookingDetail rentdetail = new BookingDetail();
                            try{
                                rentdetail = (BookingDetail)deserializeObjectFromString(msgContent);
                            }
                            catch(Exception ex){}
                            
                            counterAgentGUI.AppendBookingLog("Renting Success!\n");
                            counterAgentGUI.AppendBookingLog("Rent Detail:\n");
                            counterAgentGUI.AppendBookingLog("Court Owner :" + rentdetail.getProvider());
                            counterAgentGUI.AppendBookingLog("Court Tpye :" + rentdetail.getCourtType());
                           
                        }
                        
                        else if(msg.getPerformative()==ACLMessage.REFUSE){
                            String msgContent = msg.getContent();
                            BookingDetail rentdetail = new BookingDetail();
                            try{
                                rentdetail = (BookingDetail)deserializeObjectFromString(msgContent);
                            }
                            catch(Exception ex){}
                            
                            counterAgentGUI.AppendBookingLog("Renting Failed!\n");
                            counterAgentGUI.AppendBookingLog("Reason :" + rentdetail.getReason());
                        }
                    }
                }
                else
                    block();
            }
        });
    }
    
    public void InitializeRoomRequest(){
        try {
            salesman.clear();
            counterAgentGUI.AppendLog("Searching the DF/Yellow-Pages for Renting service");
            
            // Build the description used as template for the search
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription templateSd = new ServiceDescription();
            templateSd.setType("Renting");
            template.addServices(templateSd);
  		
            SearchConstraints sc = new SearchConstraints();
            sc.setMaxResults(new Long(10));
  		
            DFAgentDescription[] results = DFService.search(this, template, sc);
            if (results.length > 0) {
  		counterAgentGUI.AppendLog("Agent "+getLocalName()+" found the following Renting services:");
  		for (int i = 0; i < results.length; ++i) {
                    DFAgentDescription dfd = results[i];
                    AID provider = dfd.getName();
                    jade.util.leap.Iterator it = dfd.getAllServices();
                    while (it.hasNext()) {
                        ServiceDescription sd = (ServiceDescription) it.next();
  			if (sd.getType().equals("Renting")) {
                            salesman.add(provider);
                            counterAgentGUI.AppendLog("- Service \""+sd.getName()+"\" provided by agent "+provider.getName());
                            Court court = new Court();
                            court.setCourtType(counterAgentGUI.getCourtType());
                            
                            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                            
                            String strObj = ""; 
                            try
                            {
                                strObj = serializeObjectToString(court);
                            }
                            catch (Exception ex)
                            {

                            }  
                            msg.setContent(strObj);
                            msg.addReceiver(provider);
                            send(msg);
  			}
                    }
  		}
            }	
            else {
                counterAgentGUI.AppendLog("Agent "+getLocalName()+" did not find any Renting service");
            }
  	}
  	catch (FIPAException fe) {
            fe.printStackTrace();
  	}
        counterAgentGUI.AppendLog("\n");
    }
    
    public void InitializeBookingRequest(int selection){
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        rentdetail = new BookingDetail();
        
        counterAgentGUI.AppendBookingLog("Sending booking request to the service.\n");
        counterAgentGUI.AppendBookingLog("Please wait.");
        counterAgentGUI.AppendBookingLog("......");
        
        rentdetail.setCourtType(courts.get(selection-1).getCourtType());
        rentdetail.setProvider(courts.get(selection-1).getProvider());
        rentdetail.setRenter(this.getAID());
        
        String strObj = ""; 
        try{
            strObj = serializeObjectToString(rentdetail);
        }
       
        catch(Exception ex){}
        
        msg.setContent(strObj);
        msg.addReceiver(salesman.get(0));
        send(msg);   
    }
}
