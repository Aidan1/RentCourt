
package rentCourt;

import bean.Room;
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

public class salesmanAgent extends Agent{
    
    static final Base64 base64 = new Base64();
    private ArrayList<Room> rooms = new ArrayList<>();
    private ArrayList<AID> customers = new ArrayList<>();
    
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
        catch(Exception ex){}
        
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
        InitializeSalesman(this);
       
        addBehaviour(new CyclicBehaviour(){
            @Override
            public void action() {
                 ACLMessage msg = receive();
                 
                 if(msg!=null)
                 {
                     if(msg.getPerformative()==ACLMessage.REQUEST)
                     {
                         Room room = new Room();

                         String msgContent = msg.getContent();
                         try
                         {
                             room = (Room)deserializeObjectFromString(msgContent);  
                         }
                         catch (Exception ex){
                         }
                         
                         SearchForRooms(room.getType(), room.getPlace(), msg);
                     }
                     
                     else if(msg.getPerformative()==ACLMessage.INFORM){
                         RentDetail rentdetail = new RentDetail();
                         
                         String msgContent = msg.getContent();
                         try{
                             rentdetail = (RentDetail)deserializeObjectFromString(msgContent);
                         }
                         catch(Exception ex){
                         }
                         
                         BookRoom(rentdetail);
                     }
                     
                     else if(msg.getPerformative()==ACLMessage.AGREE){
                         RentDetail rentdetail = new RentDetail();
                         String msgContent = msg.getContent();
                         
                         try{
                             rentdetail = (RentDetail)deserializeObjectFromString(msgContent);
                         }
                         catch (Exception ex){}
                         
                         ACLMessage reply = new ACLMessage(ACLMessage.AGREE);
                         reply.setContent(msgContent);
                         reply.addReceiver(rentdetail.getRenter());
                         send(reply);
                     }
                     
                     else if(msg.getPerformative()==ACLMessage.REFUSE){
                         RentDetail rentdetail = new RentDetail();
                         String msgContent = msg.getContent();
                         
                         try{
                             rentdetail = (RentDetail)deserializeObjectFromString(msgContent);
                         }
                         catch (Exception ex){}
                         
                         ACLMessage reply = new ACLMessage(ACLMessage.REFUSE);
                         reply.setContent(msgContent);
                         reply.addReceiver(rentdetail.getRenter());
                         send(reply);
                     }
                 }
                 
                 else
                    block();
            }
        });
    }
    
    public void InitializeSalesman(Agent a){
        String serviceName = "Salesman-Power";
        
  	try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName(serviceName);
            sd.setType("Salesman");
            dfd.addServices(sd);
  		
            DFService.register(a, dfd);
  	}
  	catch (FIPAException fe) {
            fe.printStackTrace();
  	}
    }
    
    public void SearchForRooms(String type, String place, ACLMessage msg){
        try {
            rooms.clear();
            // Build the description used as template for the search
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription templateSd = new ServiceDescription();
            templateSd.setType("Renting");
            templateSd.addProperties(new Property("roomtype", type));
            templateSd.addProperties(new Property("place", place));
            template.addServices(templateSd);
  		
            SearchConstraints sc = new SearchConstraints();
            sc.setMaxResults(new Long(10));
  		
            DFAgentDescription[] results = DFService.search(this, template, sc);
            if (results.length > 0) {
  		for (int i = 0; i < results.length; ++i) {
                    DFAgentDescription dfd = results[i];
                    jade.util.leap.Iterator it = dfd.getAllServices();
                    while (it.hasNext()) {
                        ServiceDescription sd = (ServiceDescription) it.next();
  			if (sd.getType().equals("Renting")) {
                            Room temp = new Room();
                            jade.util.leap.Iterator properties = sd.getAllProperties();
                            temp.setProvider(dfd.getName());
                            while(properties.hasNext()){
                                 Property p = (Property)properties.next();
                                 if(p.getName().equals("roomtype"))
                                     temp.setType((String)p.getValue());
                                 else if(p.getName().equals("place"))
                                     temp.setPlace((String)p.getValue());
                                 else if(p.getName().equals("price"))
                                     temp.setPrice(Double.parseDouble((String)p.getValue()));
                            }
                            rooms.add(temp);
  			}
                    }
  		}
            }	
            else {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.FAILURE);
                send(reply);
            }
            if(rooms.size()>0)
            {
                String strObj = ""; 
                try{
                    strObj = serializeObjectToString(rooms);
                }
                catch (Exception ex)
                {
                }  
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(strObj);
                send(reply);
            }
  	}
  	catch (FIPAException fe) {
            fe.printStackTrace();
  	}
    }
    
    public void BookRoom(RentDetail rd){
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        
        String strObj = "";
        try{
            strObj = serializeObjectToString(rd);
        }
        catch(Exception ex){  
        }
        
        msg.setContent(strObj);
        msg.addReceiver(rd.getProvider());
        send(msg);
    }
}
