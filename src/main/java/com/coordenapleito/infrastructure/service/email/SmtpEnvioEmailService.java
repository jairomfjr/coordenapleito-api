package com.coordenapleito.infrastructure.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.MimeMessageHelper;
import com.coordenapleito.core.email.EmailProperties;
import com.coordenapleito.domain.service.EnvioEmailService;

import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.core.io.ClassPathResource;

import java.io.File;

public class SmtpEnvioEmailService implements EnvioEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    protected EmailProperties emailProperties;

    @Autowired
    private ProcessadorEmailTemplate processadorEmailTemplate;

    @Override
    public void enviar(Mensagem mensagem) {
        try {
            mailSender.send(criarMimeMessage(mensagem));
        } catch (Exception e) {
            throw new EmailException("Não foi possível enviar e-mail", e);
        }
    }

    protected MimeMessage criarMimeMessage(Mensagem mensagem) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom(emailProperties.getRemetente());
        helper.setTo(mensagem.getDestinatarios().toArray(new String[0]));
        helper.setSubject(mensagem.getAssunto());
        helper.setText(processadorEmailTemplate.processar(mensagem), true);

        ClassPathResource logo = new ClassPathResource("images/logo-coordenapleito.png");
        if (logo.exists()) {
            helper.addInline("logoCoordenapleito", logo);
        }

        if (mensagem.getAnexos() != null && mensagem.getAnexos().size() > 0)
            for (File a : mensagem.getAnexos()) {
                helper.addAttachment(a.getName(), a);
            }

        return mimeMessage;
    }
}