package org.example.server.modules;

import org.json.JSONObject;

public class User
{
    private String name;
    private String phone_number;
    private String email;
    private String password;
    private String user_role;
    private String address;
    private int userID;
    private String profileImage;
    private String bank_name;
    private String bank_account_number;
    private String token = "";
    private double walletBalance = 0;
    private boolean is_approved;

    public boolean getIs_approved() {
        return is_approved;
    }

    public void setIs_approved(boolean is_approved) {
        this.is_approved = is_approved;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }


    public User(String name, String phone_number, String email, String password, String user_role, String address, String profileImage,
                String bank_name, String bank_account_number, double walletBalance) {
        this.name = name;
        this.phone_number = phone_number;
        this.password = password;
        this.user_role = user_role;
        this.address = address;
        this.email = email;
        this.profileImage = profileImage;
        this.bank_name = bank_name;
        this.bank_account_number = bank_account_number;
        this.walletBalance = walletBalance;
    }

    public User(String name, String phone_number, String email, String password, String user_role, String address, String profileImage,
                String bank_name, String bank_account_number) {
        this.name = name;
        this.phone_number = phone_number;
        this.password = password;
        this.user_role = user_role;
        this.address = address;
        this.email = email;
        this.profileImage = profileImage;
        this.bank_name = bank_name;
        this.bank_account_number = bank_account_number;
        this.walletBalance = 0;
    }

    public User(JSONObject jsonObject){
        this.name = jsonObject.getString("full_name").trim();
        this.phone_number = jsonObject.getString("phone").trim();
        this.password = jsonObject.getString("password").trim();
        this.user_role = jsonObject.getString("role").trim();
        this.address = jsonObject.optString("address", "").trim();
        this.email = jsonObject.getString("email").trim();
        this.profileImage = jsonObject.optString("profileImageBase64", "").trim();
        this.walletBalance = 0;
        JSONObject bankInfo = jsonObject.optJSONObject("bank_info");
        if (bankInfo != null) {
            this.bank_name = bankInfo.optString("bank_name", "").trim();
            this.bank_account_number = bankInfo.optString("account_number", "").trim();
        } else {
            this.bank_name = "";
            this.bank_account_number = "";
        }
    }

    public User(){

    }
    public String getName(){
        return name;
    }
    public String getPhoneNumber() {
        return phone_number;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public String getUserRole() {
        return user_role;
    }
    public String getAddress() {
        return address;
    }
    public int getUserID() {
        return userID;
    }
    public String getProfileImage() {
        return profileImage;
    }
    public String getBankName() {
        return bank_name;
    }
    public String getBankAccountNumber() {
        return bank_account_number;
    }
    public String getToken() {
        return token;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phone_number) {
        this.phone_number = phone_number;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setUserRole(String user_role) {
        this.user_role = user_role;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setUserID(int userID) {
        this.userID = userID;
    }
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    public void setBankName(String bank_name) {
        this.bank_name = bank_name;
    }
    public void setBankAccountNumber(String bank_account_number) {
        this.bank_account_number = bank_account_number;
    }
    public void setToken(String token){
        this.token = token;
    }
    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", user_role='" + user_role + '\'' +
                ", address='" + address + '\'' +
                ", userID='" + userID + '\'' +
                '}';
    }
}
