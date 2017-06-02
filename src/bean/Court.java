/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import java.io.Serializable;
import jade.core.AID;

public class Court implements Serializable
{
    private String courtType;
    private int available;
    private AID provider;

    public String getCourtType() 
    {
        return courtType;
    }

    public void setCourtType(String courtType) 
    {
        this.courtType = courtType;
    }
    
    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public AID getProvider() {
        return provider;
    }

    public void setProvider(AID provider) 
    {
        this.provider = provider;
    }
}
