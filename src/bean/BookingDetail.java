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
    private String courtType;
    private int courtNumber;
    private String matricNo;
    private int timeSlot;

    public int getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(int timeSlot) {
        this.timeSlot = timeSlot;
    }

    public int getCourtNumber() {
        return courtNumber;
    }

    public void setCourtNumber(int courtNumber) {
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
