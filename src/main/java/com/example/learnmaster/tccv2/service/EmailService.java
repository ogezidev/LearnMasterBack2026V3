package com.example.learnmaster.tccv2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String host;
    private final String remetente;

    public EmailService(ObjectProvider<JavaMailSender> mailSender,
                        @Value("${spring.mail.host:}") String host,
                        @Value("${app.mail.from:}") String remetente) {
        this.mailSender = mailSender;
        this.host = host;
        this.remetente = remetente;
    }

    // Roda em segundo plano: o tempo de resposta de /auth/recuperar nao revela se o e-mail existe
    @Async
    public void enviarRecuperacao(String para, String nome, String link) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (host.isBlank() || sender == null) {
            // Sem SMTP configurado (desenvolvimento): o link aparece so no log do backend
            log.warn("SMTP nao configurado (MAIL_HOST vazio). Link de redefinicao para {}: {}", para, link);
            return;
        }

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(para);
        mensagem.setSubject("LearnMaster - Redefinição de senha");
        mensagem.setText("""
                Olá, %s!

                Recebemos um pedido para redefinir a senha da sua conta no LearnMaster.
                Para criar uma nova senha, acesse o link abaixo (válido por 30 minutos e por um único uso):

                %s

                Se você não fez esse pedido, ignore este e-mail. Sua senha continua a mesma.

                Equipe LearnMaster
                """.formatted(nome, link));

        try {
            sender.send(mensagem);
        } catch (MailException e) {
            log.error("Falha ao enviar e-mail de recuperacao para {}", para, e);
        }
    }
}
