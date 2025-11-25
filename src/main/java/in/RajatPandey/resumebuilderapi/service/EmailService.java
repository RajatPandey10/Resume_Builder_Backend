package in.RajatPandey.resumebuilderapi.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${app.mail.from}")
    private String fromEmail;

    private final JavaMailSender mailSender;


    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        log.info("Sending HTML email to: {} with subject: {}", to, subject);


        log.debug("EmailService::sendHtmlEmail - Email content length: {} characters",
                htmlContent != null ? htmlContent.length() : 0);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("HTML email successfully sent to: {}", to);

        } catch (MessagingException ex) {
            log.error("Failed to send HTML email to: {} | Error: {}", to, ex.getMessage());
            throw ex;
        }
    }


    public void sendEmailWithAttachment(String to, String subject, String body,
                                        byte[] attachment, String fileName) throws MessagingException {
        log.info("Sending email with attachment to: {} with subject: {}", to, subject);
        log.debug("Attachment details - Filename: {}, Size: {} bytes",
                fileName,
                attachment != null ? attachment.length : 0);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.addAttachment(fileName, new ByteArrayResource(attachment));

            mailSender.send(message);

            log.info("Email with attachment successfully sent to: {}", to);

        } catch (MessagingException ex) {
            log.error("Failed to send email with attachment to: {} | Error: {}", to, ex.getMessage());
            throw ex;
        }
    }
}
