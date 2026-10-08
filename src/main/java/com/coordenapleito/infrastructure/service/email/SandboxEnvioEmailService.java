package com.coordenapleito.infrastructure.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

public class SandboxEnvioEmailService extends SmtpEnvioEmailService{

//    public SandboxEnvioEmailService(JavaMailSender mailSender, EmailProperties emailProperties, ProcessadorEmailTemplate processadorEmailTemplate) {
//        super(mailSender, emailProperties, processadorEmailTemplate);
//    }

    @Override
    protected MimeMessage criarMimeMessage(Mensagem mensagem) throws MessagingException {
        MimeMessage mimeMessage = super.criarMimeMessage(mensagem);

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
        helper.setTo(super.emailProperties.getSandbox().getDestinatario());

        return mimeMessage;
    }
}