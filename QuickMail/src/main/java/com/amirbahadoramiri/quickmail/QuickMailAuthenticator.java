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
        this.user = username;
        this.password = password;
        this.listener = listener;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    synchronized void sendMail(String subject, String body, String sender, String recipients) {
        QuickMailTask quickMailTask = new QuickMailTask(subject, body, sender, recipients, listener, session);
        quickMailTask.execute();
    }
}