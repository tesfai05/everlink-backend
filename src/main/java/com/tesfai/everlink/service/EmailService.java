package com.tesfai.everlink.service;

import com.tesfai.everlink.constant.MembershipEnum;
import com.tesfai.everlink.repository.IEverLinkRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailService implements IEmailService{
    private final IEverLinkRepository everLinkRepository;
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String everLinkEmail;

    public EmailService(IEverLinkRepository everLinkRepository, JavaMailSender mailSender) {
        this.everLinkRepository = everLinkRepository;
        this.mailSender = mailSender;
    }

    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(everLinkEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendEmailToMembers(String subject, String body) {
        List<String> emails = everLinkRepository.findAll()
                .stream()
                .filter(m -> m.getMembershipStatus().equalsIgnoreCase(MembershipEnum.Active.name()))
                .map(m -> m.getEmail())
                .filter(e-> StringUtils.isNotBlank(e))
                .collect(Collectors.toList());
        for (String email : emails) {
            sendEmail(email, subject, body);
        }
    }

}

