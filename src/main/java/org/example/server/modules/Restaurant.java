package org.example.server.modules;

public class Restaurant {
    private String restaurantID;
    private String ownerID;
    private String name;
    private String address;
    private String phoneNumber;
    private String workingHours;
    private String logoImage;
    private boolean isApproved;

    public Restaurant() {}

    public Restaurant(String restaurantID, String ownerID, String name, String address,
                      String phoneNumber, String workingHours, String logoImage, boolean isApproved) {
        this.restaurantID = restaurantID;
        this.ownerID = ownerID;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.workingHours = workingHours;
        this.logoImage = logoImage;
        this.isApproved = isApproved;
    }

    // Getters & Setters
    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.restaurantID = restaurantID;
    }

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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public String getLogoImage() {
        return logoImage;
    }

    public void setLogoImage(String logoImage) {
        this.logoImage = logoImage;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }
}
