/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import jade.core.AID;

/**
 *
 * @author kingw
 */
public class SearchRequest {
    private int timeSlot;
    private AID requester;
    private String courtType;

    public int getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(int timeSlot) {
        this.timeSlot = timeSlot;
    }

    public AID getRequester() {
        return requester;
    }

    public void setRequester(AID requester) {
        this.requester = requester;
    }

    public String getCourtType() {
        return courtType;
    }

    public void setCourtType(String courtType) {
        this.courtType = courtType;
    }
}
