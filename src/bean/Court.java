/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import jade.core.AID;
import java.io.Serializable;

public class Court implements Serializable
{
    private AID provider;
    private int courtNumber;
    private String courtType;

    public String getCourtType() 
    {
        return courtType;
    }

    public void setCourtType(String courtType) 
    {
        this.courtType = courtType;
    }
    
    public int getCourtNumber() 
    {
        return courtNumber;
    }

    public void setCourtNumber(int courtNumber) 
    {
        this.courtNumber = courtNumber;
    }

    public AID getProvider() 
    {
        return provider;
    }

    public void setProvider(AID provider) 
    {
        this.provider = provider;
    }
}
