package com.amirbahadoramiri.quickmail;

import android.os.AsyncTask;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

class QuickMailTask extends AsyncTask<String, Void, Exception> {
    private String subject;
    private String body;
    private String sender;
    private String recipients;
    private QuickMailListener listener;
    private Session session;
    private String dataSourceType = "text/plain";

    QuickMailTask(String subject, String body, String sender, String recipients, QuickMailListener listener, Session session) {
        this.subject = subject;
        this.body = body;
        this.sender = sender;
        this.recipients = recipients;
        this.listener = listener;
        this.session = session;
        if (isHtml(body)) {
            dataSourceType = "text/html";
        }
    }

    private boolean isHtml(String data) {
        Pattern pattern = Pattern.compile("<(\\w+)( +.+)*>((.*))</\\1>");
        Matcher matcher = pattern.matcher(data);
        return matcher.find();
    }

    protected Exception doInBackground(String... urls) {
        Exception result = null;
        try {
            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), dataSourceType));
            MimeMessage message = new MimeMessage(session);
            message.setSender(new InternetAddress(sender));
            message.setSubject(subject);
            message.setDataHandler(handler);
            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            result = e;
        }
        return result;
    }

    protected void onPostExecute(Exception result) {
        if (listener != null) {
            if (result == null) {
                listener.onSuccess();
            } else {
                listener.onFailure(result);
            }
        }
    }

    private class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}
