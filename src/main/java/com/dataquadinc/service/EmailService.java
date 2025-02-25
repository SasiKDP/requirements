package com.dataquadinc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;


    public void sendEmail(String to, String subject, String text)  {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to); // recipient's email
        message.setSubject(subject); // email subject
        message.setText(text); // email body
        message.setFrom("datamatrrrix@gmail.com"); // your email

        javaMailSender.send(message);
    }
}
