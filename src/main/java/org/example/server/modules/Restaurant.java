package org.example.server.modules;

public class Restaurant {
    private String name;
    private String address;
    private String phone;
    private String logoBase64;
    private int taxFee;
    private int additionalFee;
//    private String restaurantID;
    private String ownerID;

    // Getters and Setters
//    public String getRestaurantID() {
//        return restaurantID;
//    }
//
//    public void setRestaurantID(String restaurantID) {
//        this.restaurantID = restaurantID;
//    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public int getTaxFee() {
        return taxFee;
    }

    public void setTaxFee(int taxFee) {
        this.taxFee = taxFee;
    }

    public int getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(int additionalFee) {
        this.additionalFee = additionalFee;
    }
}
