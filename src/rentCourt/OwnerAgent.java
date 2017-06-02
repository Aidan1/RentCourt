
package rentCourt;

import bean.Court;
import bean.RentDetail;
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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.codec.binary.Base64;

public class OwnerAgent extends Agent {
    private Court court = new Court();
    static final Base64 base64 = new Base64();
    
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
    
    protected void setup() {
        
        //Type of Room and Place
        Object[] args = getArguments();
        
        //set room type
	String type = (String) args[0];
        court.setType(type);



        int availability = Integer.parseInt((String)args[3]);
        court.setAvailable(availability);
        
        String serviceName = "Rent-Court";
        
  	try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName(serviceName);
            sd.setType("Renting");
            sd.addProperties(new Property("courttype", court.getType()));
            dfd.addServices(sd);
  		
            DFService.register(this, dfd);
  	}
  	catch (FIPAException fe) {
            fe.printStackTrace();
  	}
        
        addBehaviour(new CyclicBehaviour(this) {
            public void action() { 
                ACLMessage msg = receive();
                RentDetail rent = new RentDetail();
                
                if (msg!= null) {                    
                    String msgContent = msg.getContent();
                    try
                    {
                        rent = (RentDetail)deserializeObjectFromString(msgContent);  
                    }
                    catch (Exception ex)
                    {            
                    }
                    
                    String rentType = rent.getType();
		    String rentPlace = rent.getPlace(); 
                    
	 	    String roomType = court.getType();
                    
                    System.out.println("Rent Type : " + rentType + " Rent Place : " + rentPlace);
                    
                    if (rentType.equals(roomType)) {
                        if (rentPlace.equals("Kedah")) {
			    if (court.getAvailable() > 0) {                              
                                court.setAvailable(court.getAvailable()-1);
                                rent.setApproval(true);                          
                            }
                            else{
                                rent.setApproval(false);
                                rent.setReason("No more available court.");

                                 // Deregister from the yellow pages
                                try {
                                    DFService.deregister(this.getAgent());
                                }
                                catch (FIPAException fe) {
                                     fe.printStackTrace();
                                }     
                            }
                        }
                        else {
                            rent.setApproval(false);
                            rent.setReason("Wrong booking place.");
                        }
                    }
                    else{
                        rent.setApproval(false);
                        rent.setReason("Wrong booking type.");
                    }
                    
                    ACLMessage reply = msg.createReply();
                    
                    if(rent.isApproval())
                        reply.setPerformative(ACLMessage.AGREE);
                    else
                        reply.setPerformative(ACLMessage.REFUSE);
                    
                    String strObj = "";
                    try{
                        strObj = serializeObjectToString(rent);
                    }
                    catch(Exception ex){}
                                
                    reply.setContent(strObj);
                    send(reply);
                }
                
                block();
            }
        });
    }    
}
