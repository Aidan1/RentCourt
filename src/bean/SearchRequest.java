/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import java.io.Serializable;
/**
 *
 * @author kingw
 */
public class SearchRequest implements Serializable 
{
    private String timeSlot;
    private String courtType;

    public String getTimeSlot() 
    {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) 
    {
        this.timeSlot = timeSlot;
    }

    public String getCourtType() 
    {
        return courtType;
    }

    public void setCourtType(String courtType) 
    {
        this.courtType = courtType;
    }
}
