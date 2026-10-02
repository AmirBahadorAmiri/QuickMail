package com.amirbahadoramiri.quickmail;

public class QuickMail {

    private String email;
    private String pass;
    private static QuickMail instance;
    private String title;
    private String body;
    private String emailAddress;
    private String sender;
    private QuickMailListener listener;

    private QuickMail(String email, String pass) {
        this.email = email;
        this.pass = pass;
    }

    public static QuickMail withAccount(String email, String pass) {
        instance = new QuickMail(email, pass);
        return instance;
    }

    public static QuickMail withAccount(QuickMailConfig config) {
        instance = new QuickMail(config.getEmail(), config.getPassword());
        return instance;
    }

    public QuickMail withTitle(String title) {
        instance.title = title;
        return instance;
    }

    public QuickMail withBody(String body) {
        instance.body = body;
        return instance;
    }

    public QuickMail toEmailAddress(String emailAddress) {
        instance.emailAddress = emailAddress;
        return instance;
    }

    public QuickMail withSender(String sender) {
        instance.sender = sender;
        return instance;
    }

    public QuickMail withListenner(QuickMailListener listener) {
        instance.listener = listener;
        return instance;
    }

    public void send() {
        try {
            QuickMailAuthenticator mailSender = new QuickMailAuthenticator(instance.email, instance.pass, instance.listener);
            mailSender.sendMail(instance.title, instance.body, instance.sender, instance.emailAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
