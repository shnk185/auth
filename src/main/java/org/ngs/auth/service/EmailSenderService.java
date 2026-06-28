package org.ngs.auth.service;

import org.ngs.auth.config.properties.EmailContentConfig;
import org.ngs.auth.enums.EmailType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
public class EmailSenderService {

    @Autowired
    @Qualifier("emailExecutorService")
    private ExecutorService emailExecutorService;

    @Autowired
    private EmailContentConfig emailContentConfig;

    @Autowired
    private EmailService emailService;

    public void sendSignUpOtpEmail(String to, String otp) {
        String body = String.format(emailContentConfig.getBodyTemplates().get(EmailType.SIGNUP_OTP), otp);
        String subject = String.format(emailContentConfig.getSubjectTemplates().get(EmailType.SIGNUP_OTP), otp);
        emailExecutorService.execute(() -> emailService.sendSimpleEmail(to, subject, body));
    }

}
