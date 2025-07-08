package com.tesfai.everlink.service;

import java.util.List;

public interface IEmailService {
    void sendEmail(String toEmail, String subject, String body);
    void sendEmailToMembers(String subject, String body);
}
