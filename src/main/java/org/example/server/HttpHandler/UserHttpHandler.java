package org.example.server.HttpHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import netscape.javascript.JSObject;
import org.example.server.Controller.*;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.sql.SQLException;
import java.util.Base64;
import org.example.server.modules.User;
public class UserHttpHandler {
    private final UserController userController;
    public UserHttpHandler() throws SQLException {
        this.userController = new UserController();
    }
    JSONObject jsonResponse = new JSONObject();
    public Object handleCreateUser(Request request, Response response) throws JsonProcessingException {
        try{
            JSONObject json = new JSONObject(request.body());
            String name = json.getString("full_name");
            String phoneNumber = json.getString("phone");
            String password = json.getString("password");
            String userRole = json.getString("role");
            String address = json.getString("address");
            String email = json.getString("email");
            String profileImage = json.optString("profile_image", null);
            byte[] decodedImageBytes = null;
            if (profileImage != null && !profileImage.isEmpty()) {
                decodedImageBytes = Base64.getDecoder().decode(profileImage);
                // Optionally, save the image to a file
                // Files.write(Paths.get("profile_image.jpg"), decodedImageBytes);
            }
            JSONObject bank_info = json.getJSONObject("bank_info");
            String bankName = bank_info.getString("bank_name");
            String bankAccountNumber = bank_info.getString("account_number");
            // Create a new user object

            boolean isCreated = userController.createUser(
                    name,
                    phoneNumber,
                    email,
                    password,
                    userRole,
                    address,
                    profileImage,
                    bankName,
                    bankAccountNumber
            );

            if(!isCreated) {
                response.status(409); //409 conflict status code
                jsonResponse.put("error", "Phone number already exists");
                response.type("application/json");
                response.body(jsonResponse.toString());
                return response.body();
            }
            if(name.isEmpty()){
                response.status(400); //400 bad request status code
                jsonResponse.put("error", "Invalid name");
                response.type("application/json");
                response.body(jsonResponse.toString());
                return response.body();
            }
            if(phoneNumber.isEmpty() || !phoneNumber.matches("\\d{11}")) {
                response.status(400); //400 bad request status code
                jsonResponse.put("error", "invalid phone number");
                response.type("application/json");
                response.body(jsonResponse.toString());
                return response.body();
            }
            if(password.isEmpty()) {
                response.status(400); //400 bad request status code
                jsonResponse.put("error", "Invalid password");
                response.type("application/json");
                response.body(jsonResponse.toString());
                return response.body();
            }
            if(address.isEmpty()) {
                response.status(400); //400 bad request status code
                jsonResponse.put("error", "Invalid address");
                response.type("application/json");
                response.body(jsonResponse.toString());
                return response.body();
            }
            if(!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                response.status(400); //400 bad request status code
                jsonResponse.put("error", "Invalid email ");
                response.type("application/json");
                response.body(jsonResponse.toString());
                return response.body();
            }
            if(!userRole.equals("buyer") && !userRole.equals("seller") && !userRole.equals("courier")) {
                response.status(400); //400 bad request status code
                jsonResponse.put("error", "Invalid user role");
                response.type("application/json");
                response.body(jsonResponse.toString());
                return response.body();
            }

            jsonResponse.put("message", "User created successfully");
            jsonResponse.put("user_id", userController.getUserByPhoneAndPassword(phoneNumber, password).getUserID());
            jsonResponse.put("token",""); // Placeholder for token, implement token generation if needed
            response.status(200); //200 OK status code
            response.type("application/json");
            response.body(jsonResponse.toString());


        }
        catch (Exception e){
            response.status(400);
            jsonResponse.put("error", "An error occurred while creating the user: " + e.getMessage());
            response.body(jsonResponse.toString());
            return response.body();
        }

        return response.body();

    }
    public Object handleLoginUser(Request request, Response response) {
        try {
            JSONObject json = new JSONObject(request.body());
            String phone = json.getString("phone");
            String password = json.getString("password");
            // Here you would typically check the credentials against a database
            // For now, we will just return a success message
            if (password.isEmpty()) { //invalid password format(the password field is empty)
                response.status(400);
                jsonResponse.put("error", "Invalid password");
                response.body(jsonResponse.toString());
                return response.body();
            }
            if (phone.isEmpty() || !phone.matches("\\d{11}")) { //invalid phone number format(either empty or not eleven digits)
                response.status(400);
                jsonResponse.put("error", "Invalid phone");
                response.body(jsonResponse.toString());
                return response.body();
            }
            if( !userController.doesUserExist(phone, password) ) { //user doesn't exist in the database
                response.status(401);
                jsonResponse.put("error", "Unauthorized request");
                response.body(jsonResponse.toString());
                return response.body();

            }
            else{
                // Here you would typically create a session or token for the user
                // For now, we will just return a success message
                response.status(200);
                jsonResponse.put("message", "user logged in successfully");
                jsonResponse.put("token","");// Placeholder for token, implement token generation if needed
                JSONObject userJson = new JSONObject();
                userJson.put("id",userController.getUserByPhoneAndPassword(phone, password).getUserID());
                userJson.put("name", userController.getUserByPhoneAndPassword(phone, password).getName());
                userJson.put("phone", userController.getUserByPhoneAndPassword(phone, password).getPhoneNumber());
                userJson.put("email", userController.getUserByPhoneAndPassword(phone, password).getEmail());
                userJson.put("role", userController.getUserByPhoneAndPassword(phone, password).getUserRole());
                userJson.put("address", userController.getUserByPhoneAndPassword(phone, password).getAddress());
                userJson.put("profile_image", userController.getUserByPhoneAndPassword(phone, password).getProfileImage());
                userJson.put("bank_info", new JSONObject()
                        .put("bank_name", userController.getUserByPhoneAndPassword(phone, password).getBankName())
                        .put("account_number", userController.getUserByPhoneAndPassword(phone, password).getBankAccountNumber()));
                jsonResponse.put("user", userJson);
                response.type("application/json");
                response.body(jsonResponse.toString());
                return response.body();
            }
        } catch (Exception e) {
            response.status(500);
            response.body("Internal server error");
        }
        return response.body();
    }
    public Object handleGetUser(Request request, Response response) {
        try {
            String phone = request.queryParams("phone");
            String password = request.queryParams("password");
            if (phone == null || password == null) {
                response.status(400);
                response.body("Phone number and password must be provided");
                return response.body();
            }
            User user = userController.getUserByPhoneAndPassword(phone, password);
            if (user == null) {
                response.status(404);
                response.body("User not found");
            } else {
                JSONObject userJson = new JSONObject();
                userJson.put("name", user.getName());
                userJson.put("phone", user.getPhoneNumber());
                userJson.put("email", user.getEmail());
                userJson.put("role", user.getUserRole());
                userJson.put("address", user.getAddress());
                userJson.put("profile_image", user.getProfileImage());
                userJson.put("bank_info", new JSONObject()
                        .put("bank_name", user.getBankName())
                        .put("account_number", user.getBankAccountNumber()));

                response.status(200);
                response.body(userJson.toString());
            }
        } catch (Exception e) {
            response.status(500);
            response.body("An error occurred while retrieving the user: " + e.getMessage());
        }
        return response.body();
    }
    public Object handleUpdateUser(Request request, Response response) {
        try {
            JSONObject json = new JSONObject(request.body());
            String phone = json.getString("phone");
            String password = json.getString("password");
            String name = json.optString("full_name", null);
            String address = json.optString("address", null);
            String email = json.optString("email", null);
            String profileImage = json.optString("profile_image", null);
            JSONObject bank_info = json.optJSONObject("bank_info");
            String bankName = bank_info != null ? bank_info.optString("bank_name", null) : null;
            String bankAccountNumber = bank_info != null ? bank_info.optString("account_number", null) : null;

            User user = userController.getUserByPhoneAndPassword(phone, password);
            if (user == null) {
                response.status(404);
                response.body("User not found");
                return response.body();
            }

            // Update user details
            if (name != null) user.setName(name);
            if (address != null) user.setAddress(address);
            if (email != null) user.setEmail(email);
            if (profileImage != null) user.setProfileImage(profileImage);
            if (bankName != null) user.setBankName(bankName);
            if (bankAccountNumber != null) user.setBankAccountNumber(bankAccountNumber);

            // Save updated user
            userController.createUser(
                    user.getName(),
                    user.getPhoneNumber(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getUserRole(),
                    user.getAddress(),
                    user.getProfileImage(),
                    user.getBankName(),
                    user.getBankAccountNumber()
            );

            response.status(200);
            response.body("User updated successfully");
        } catch (Exception e) {
            response.status(500);
            response.body("An error occurred while updating the user: " + e.getMessage());
        }
        return response.body();
    }
}
