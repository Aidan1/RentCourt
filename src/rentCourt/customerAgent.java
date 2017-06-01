
package rentCourt;

import bean.Court;
import bean.RentDetail;
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

public class customerAgent extends Agent{
    
    static final Base64 base64 = new Base64();
    private customerAgentGUI custAgentGUI;
    private ArrayList<Court> rooms = new ArrayList<>();
    private ArrayList<AID> salesman = new ArrayList<>();
    private RentDetail rentdetail;
    
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
        custAgentGUI = new customerAgentGUI(this);
        custAgentGUI.ShowGUI();
        
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
                                rooms = (ArrayList)deserializeObjectFromString(msgContent);
                            }
                            catch (Exception ex)
                            {            
                            }

                            if(rooms!=null){
                               custAgentGUI.NextStep();
                               custAgentGUI.ClearAvailableRoom();
                               custAgentGUI.AppendBookingLog("Available rooms found by :" + msg.getSender() + "\n");
                               custAgentGUI.AppendAvailableRoom("No. \t Type \t Place \t Price \t AID"+"\n");
                               for(int i=0;i<rooms.size();i++)
                               {
                                   custAgentGUI.AppendAvailableRoom(i+1 + "\t" + rooms.get(i).getType() + "\t" 
                                           + rooms.get(i).getPlace() + "\t" + rooms.get(i).getPrice() + "\t" + rooms.get(i).getProvider() + "\n");
                               }
                            }
                        }

                        else if(msg.getPerformative()==ACLMessage.FAILURE){
                            custAgentGUI.AppendLog("No available room found by the salesman.\n");
                        }
                        
                        else if(msg.getPerformative()==ACLMessage.AGREE){
                            String msgContent = msg.getContent();
                            RentDetail rentdetail = new RentDetail();
                            try{
                                rentdetail = (RentDetail)deserializeObjectFromString(msgContent);
                            }
                            catch(Exception ex){}
                            
                            custAgentGUI.AppendBookingLog("Renting Success!\n");
                            custAgentGUI.AppendBookingLog("Rent Detail:\n");
                            custAgentGUI.AppendBookingLog("Room Owner :" + rentdetail.getProvider());
                            custAgentGUI.AppendBookingLog("Room Tpye :" + rentdetail.getType());
                            custAgentGUI.AppendBookingLog("Room Place :" + rentdetail.getPlace());
                            custAgentGUI.AppendBookingLog("Room Price :" + rentdetail.getPrice());
                        }
                        
                        else if(msg.getPerformative()==ACLMessage.REFUSE){
                            String msgContent = msg.getContent();
                            RentDetail rentdetail = new RentDetail();
                            try{
                                rentdetail = (RentDetail)deserializeObjectFromString(msgContent);
                            }
                            catch(Exception ex){}
                            
                            custAgentGUI.AppendBookingLog("Renting Failed!\n");
                            custAgentGUI.AppendBookingLog("Reason :" + rentdetail.getReason());
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
            custAgentGUI.AppendLog("Searching the DF/Yellow-Pages for Salesman service");
            
            // Build the description used as template for the search
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription templateSd = new ServiceDescription();
            templateSd.setType("Salesman");
            template.addServices(templateSd);
  		
            SearchConstraints sc = new SearchConstraints();
            sc.setMaxResults(new Long(10));
  		
            DFAgentDescription[] results = DFService.search(this, template, sc);
            if (results.length > 0) {
  		custAgentGUI.AppendLog("Agent "+getLocalName()+" found the following Salesman services:");
  		for (int i = 0; i < results.length; ++i) {
                    DFAgentDescription dfd = results[i];
                    AID provider = dfd.getName();
                    jade.util.leap.Iterator it = dfd.getAllServices();
                    while (it.hasNext()) {
                        ServiceDescription sd = (ServiceDescription) it.next();
  			if (sd.getType().equals("Salesman")) {
                            salesman.add(provider);
                            custAgentGUI.AppendLog("- Service \""+sd.getName()+"\" provided by agent "+provider.getName());
                            Court room = new Court();
                            room.setType(custAgentGUI.getRoomType());
                            room.setPlace(custAgentGUI.getPlace());
                            
                            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                            
                            String strObj = ""; 
                            try
                            {
                                strObj = serializeObjectToString(room);
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
                custAgentGUI.AppendLog("Agent "+getLocalName()+" did not find any Salesman service");
            }
  	}
  	catch (FIPAException fe) {
            fe.printStackTrace();
  	}
        custAgentGUI.AppendLog("\n");
    }
    
    public void InitializeBookingRequest(int selection){
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        rentdetail = new RentDetail();
        
        custAgentGUI.AppendBookingLog("Sending booking request to the salesman.\n");
        custAgentGUI.AppendBookingLog("Please wait.");
        custAgentGUI.AppendBookingLog("......");
        
        rentdetail.setType(rooms.get(selection-1).getType());
        rentdetail.setPlace(rooms.get(selection-1).getPlace());
        rentdetail.setPrice(rooms.get(selection-1).getPrice());
        rentdetail.setProvider(rooms.get(selection-1).getProvider());
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
