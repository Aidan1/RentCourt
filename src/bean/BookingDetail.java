/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import java.io.Serializable;

public class BookingDetail implements Serializable 
{
    private int courtNumber;
    private String matricNo;
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

    public int getCourtNumber() 
    {
        return courtNumber;
    }

    public void setCourtNumber(int courtNumber) 
    {
        this.courtNumber = courtNumber;
    }
    
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
}
