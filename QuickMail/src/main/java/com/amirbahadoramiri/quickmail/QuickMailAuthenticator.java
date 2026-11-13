package com.amirbahadoramiri.quickmail;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public class QuickMailAuthenticator extends Authenticator {

    private String user;
    private String password;
    private Session session;
    private QuickMailListener listener;

    QuickMailAuthenticator(String username, String password, QuickMailListener listener) {
        this(username, password, listener, "smtp.gmail.com", 465, false, 0);
    }

    QuickMailAuthenticator(String username, String password, QuickMailListener listener,
                           String host, int port, boolean startTls, int timeoutMillis) {
        this.user = username;
        this.password = password;
        this.listener = listener;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", host);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.ssl.enable", String.valueOf(!startTls));
        props.put("mail.smtp.starttls.enable", String.valueOf(startTls));
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.setProperty("mail.smtp.quitwait", "false");
        if (timeoutMillis > 0) {
            props.put("mail.smtp.connectiontimeout", String.valueOf(timeoutMillis));
            props.put("mail.smtp.timeout", String.valueOf(timeoutMillis));
        }

        session = Session.getInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    synchronized void sendMail(String subject, String body, String sender, String senderName, String recipients,
                               String cc, String bcc, String replyTo, boolean forceHtml,
                               java.util.List<QuickMail.Attachment> attachments) {
        QuickMailTask quickMailTask = new QuickMailTask(subject, body, sender, senderName, recipients, cc, bcc,
                replyTo, forceHtml, attachments, listener, session);
        quickMailTask.execute();
    }
}
