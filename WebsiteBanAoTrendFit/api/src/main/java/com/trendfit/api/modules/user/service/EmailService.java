// Đường dẫn: api/src/main/java/com/trendfit/api/modules/user/service/EmailService.java
package com.trendfit.api.modules.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service gửi email Gmail - NOTE: Cấu hình application.properties trước
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationCode(String toEmail, String code, String hoTen) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã xác thực TrendFit: " + code);
        message.setText("Kính chào " + hoTen + ",\n\nMã xác thực: " + code + 
                       "\n\nMã hết hạn sau 5 phút.\nTrân trọng, TrendFit");
        mailSender.send(message);
        System.out.println("✅ Gửi mã xác thực đến: " + toEmail);
    }

    public void sendWelcomeEmail(String toEmail, String hoTen) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Chào mừng đến TrendFit!");
        message.setText("Chào " + hoTen + ",\n\nTài khoản đã tạo thành công!\nTrân trọng.");
        mailSender.send(message);
    }
}