package ma.event.eventreservationsystem.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptHashGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "admin123"; // change this
        String hashedPassword = encoder.encode(rawPassword);

        System.out.println("Raw password : " + rawPassword);
        System.out.println("BCrypt hash  : " + hashedPassword);
    }
}

