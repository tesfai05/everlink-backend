package com.tesfai.everlink.service;

public interface IEmailService {
    void sendEmail(String toEmail, String subject, String body);
    void sendEmailToMembers(String subject, String body);
    void sendEmailOnUpdateInfo(String subject, String body, String memberId);
}
