/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import java.io.Serializable;
import jade.core.AID;

public class BookingDetail implements Serializable 
{
    private boolean approval;
    private String courtType;
    private AID provider;
    private String reason;
    private String matricNo;
    private AID renter;

    public String getMatricNo() 
    {
        return matricNo;
    }

    public void setMatricNo(String matricNo) 
    {
        this.matricNo = matricNo;
    }
    
    public String getCourtType() 
    {
        return courtType;
    }

    public void setCourtType(String courtType) 
    {
        this.courtType = courtType;
    }
    
    public boolean isApproval() 
    {
        return approval;
    }

    public void setApproval(boolean approval) 
    {
        this.approval = approval;
    }

    public AID getProvider() 
    {
        return provider;
    }

    public void setProvider(AID provider) 
    {
        this.provider = provider;
    }

    /**
     * @return the reason
     */
    public String getReason() 
    {
        return reason;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(String reason) 
    {
        this.reason = reason;
    }  
    
    public AID getRenter() {
        return renter;
    }

    /**
     * @param renter the renter to set
     */
    public void setRenter(AID renter) {
        this.renter = renter;
    }
}
