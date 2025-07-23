package org.example.server.Util;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;

import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.example.server.Controller.RestaurantController;
import org.example.server.Controller.UserController;
import org.example.server.modules.User;


public class JWTHandler {
    private static final String SECRET_PASS = // Safer in an encrypted file.
            "@AmirKabirUniversity-APFinalProject";
    private static final SecretKeySpec SECRET_KEY = // same key every runtime.
            new SecretKeySpec(SECRET_PASS.getBytes(StandardCharsets.UTF_8), "HmacSHA256");


    public static String generateToken(String subject) {
        if (subject == null || subject.isEmpty()) return null;

        long currentTimeMillis = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + 600000000))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static Claims verifyToken(String token) {
        if (token == null || token.isEmpty()) return null;

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)  // For HMAC keys (like HS256)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e ) {
            return null;
        }
    }

    public static int getUserIDByToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if ( authHeader == null || !authHeader.startsWith("Bearer ")) {
            return -1;
        }
        String token = authHeader.substring("Bearer ".length());
        //System.out.println("owner token is : " + token);
        Claims claims = JWTHandler.verifyToken(token);
        if (claims == null) {
            return -1;
        }
        int ownerID = Integer.parseInt(claims.getSubject());
        return ownerID;
    }
    public static boolean doesUserOwnRestaurant(HttpExchange exchange, int restaurantID) {
        int userID = getUserIDByToken(exchange);
        int restaurantOwnerID = RestaurantController.getOwnerIDFromRestaurantID(restaurantID);
        System.out.println("ownerID : " + restaurantOwnerID);
        if(restaurantOwnerID == userID) {
            return true;
        }
        else{
            return false;
        }
    }
    public static int getRestaurantIDByOwnerID(HttpExchange exchange) {
        int userID = getUserIDByToken(exchange);
        return RestaurantController.getRestaurantIDByOwnerID(userID);
    }
    public static  User getUserByToken(HttpExchange exchange) throws SQLException {
        return UserController.getUserByID(getUserIDByToken(exchange));
    }
}
