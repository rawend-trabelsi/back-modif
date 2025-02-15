package com.rawend.demo.utils;

import com.rawend.demo.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service("emailServiceUtils")
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }


    public void sendWelcomeEmail(User user, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Welcome to the system");
        message.setText("Hello " + user.getUsername() + ",\n\n" +
                        "Your account has been created successfully.\n" +
                        "Here are your login credentials:\n\n" +
                        "Email: " + user.getEmail() + "\n" +
                        "Password: " + tempPassword + "\n\n" +
                        "Please change your password upon first login for security reasons.\n\n" +
                        "Login here: https://yourapp.com/login\n\n" +
                        "Thank you for joining us!");

        message.setFrom(senderEmail);
        javaMailSender.send(message);
    }

} // celui pour les identifiants et autre pour code verification