/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import java.io.Serializable;
import jade.core.AID;

public class RentDetail implements Serializable {
    private boolean approval;
    private String type;
    private double price;
    private String place;
    private AID provider;
    private AID renter;
    private String reason;
    
    public boolean isApproval() {
        return approval;
    }

    public void setApproval(boolean approval) {
        this.approval = approval;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public AID getProvider() {
        return provider;
    }

    public void setProvider(AID provider) {
        this.provider = provider;
    }

    /**
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @return the renter
     */
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
