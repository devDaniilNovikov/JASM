package dn.jasm.service.impl;
import dn.jasm.event.MailMessageEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class SenderService {


    private final JavaMailSender javaMailSender;
    private final ApplicationEventPublisher eventPublisher;



    public void sendMessage(String from,String to, String content) throws MailException{
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from);
                message.setTo(to);
                message.setText(content);
                message.setSubject("Notification");
                javaMailSender.send(message);
                eventPublisher.publishEvent(new MailMessageEvent(this, content, LocalDateTime.now(),to,from));
                log.info("Email sent successfully to: {}", to);
            } catch (MailException e) {
                log.error("Failed to send email to: {}. Error: {}", to, content);
                throw new RuntimeException("Failed to send email");

            }
    }

    public void sendMessage(String from,String to, MultipartFile file,String content) throws MailException, IOException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setText(content);
            mimeMessageHelper.setSubject("Notification");
            if (file!=null && !file.isEmpty()){
                mimeMessageHelper.addAttachment(Objects.requireNonNull(file.getOriginalFilename()),
                        new ByteArrayResource(file.getBytes()));
            }
            javaMailSender.send(message);
            eventPublisher.publishEvent(new MailMessageEvent(
                    this,content,LocalDateTime.now(),to, from));
        } catch (MessagingException | IOException e) {
            log.error("Failed send mail  to: {} cause: {}",to, ExceptionUtils.getMessage(e));
            throw new RuntimeException(e);
        }
    }
}
