package com.amirbahadoramiri.quickmail;

import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * Sends the mail on a background thread and delivers the result on the main thread.
 * Uses an {@link ExecutorService} instead of the deprecated {@link android.os.AsyncTask}.
 */
class QuickMailTask {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private final String subject;
    private final String body;
    private final String sender;
    private final String senderName;
    private final String recipients;
    private final String cc;
    private final String bcc;
    private final String replyTo;
    private final boolean forceHtml;
    private final List<QuickMail.Attachment> attachments;
    private final QuickMailListener listener;
    private final Session session;
    private String dataSourceType = "text/plain";

    QuickMailTask(String subject, String body, String sender, String senderName, String recipients, String cc, String bcc,
                  String replyTo, boolean forceHtml, List<QuickMail.Attachment> attachments,
                  QuickMailListener listener, Session session) {
        this.subject = subject;
        this.body = body;
        this.sender = sender;
        this.senderName = senderName;
        this.recipients = recipients;
        this.cc = cc;
        this.bcc = bcc;
        this.replyTo = replyTo;
        this.forceHtml = forceHtml;
        this.attachments = attachments == null ? null : new java.util.ArrayList<>(attachments);
        this.listener = listener;
        this.session = session;
        if (forceHtml || isHtml(body)) {
            dataSourceType = "text/html";
        }
    }

    private boolean isHtml(String data) {
        if (data == null)
            return false;
        Pattern pattern = Pattern.compile("<(\\w+)( +.+)*>((.*))</\\1>");
        Matcher matcher = pattern.matcher(data);
        return matcher.find();
    }

    void execute() {
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                Exception result = send();
                notifyResult(result);
            }
        });
    }

    private Exception send() {
        try {
            QuickMailLogger.log("Building message");
            MimeMessage message = new MimeMessage(session);
            if (sender != null && !sender.trim().isEmpty()) {
                InternetAddress from = new InternetAddress(sender);
                if (senderName != null && !senderName.trim().isEmpty())
                    from.setPersonal(senderName, "UTF-8");
                message.setFrom(from);
            }
            message.setSubject(subject, "UTF-8");
            if (replyTo != null && !replyTo.trim().isEmpty())
                message.setReplyTo(InternetAddress.parse(replyTo));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            if (cc != null && !cc.trim().isEmpty())
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
            if (bcc != null && !bcc.trim().isEmpty())
                message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));

            DataHandler handler = new DataHandler(new ByteArrayDataSource(
                    body.getBytes("UTF-8"), dataSourceType + "; charset=UTF-8"));
            if (attachments == null || attachments.isEmpty()) {
                message.setDataHandler(handler);
            } else {
                MimeMultipart multipart = new MimeMultipart();
                MimeBodyPart bodyPart = new MimeBodyPart();
                bodyPart.setDataHandler(handler);
                multipart.addBodyPart(bodyPart);
                for (QuickMail.Attachment attachment : attachments) {
                    multipart.addBodyPart(createAttachmentPart(attachment));
                }
                message.setContent(multipart);
            }

            QuickMailLogger.log("Sending message");
            Transport.send(message);
            QuickMailLogger.log("Message sent");
            return null;
        } catch (Exception e) {
            QuickMailLogger.log("Send failed: " + e);
            return e;
        }
    }

    private MimeBodyPart createAttachmentPart(QuickMail.Attachment attachment) throws Exception {
        MimeBodyPart part = new MimeBodyPart();
        if (attachment.isFile()) {
            part.attachFile(attachment.getFile());
        } else {
            InputStream stream = attachment.getResolver().openInputStream(attachment.getUri());
            try {
                DataSource source = new UriDataSource(attachment.getUri().toString(),
                        attachment.getDisplayName(), stream);
                part.setDataHandler(new DataHandler(source));
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        return part;
    }

    private void notifyResult(final Exception result) {
        if (listener == null)
            return;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (result == null) {
                    listener.onSuccess();
                } else {
                    listener.onFailure(result);
                }
            }
        });
    }

    /** Data source backed by a content uri stream. */
    private static final class UriDataSource implements DataSource {

        private final String name;
        private final String type;
        private final byte[] content;

        UriDataSource(String name, String displayName, InputStream stream) throws IOException {
            this.name = displayName == null ? name : displayName;
            this.type = guessType(this.name);
            this.content = readAll(stream);
        }

        private static byte[] readAll(InputStream stream) throws IOException {
            if (stream == null)
                return new byte[0];
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }

        private static String guessType(String name) {
            if (name == null)
                return "application/octet-stream";
            String lower = name.toLowerCase();
            if (lower.endsWith(".pdf"))
                return "application/pdf";
            if (lower.endsWith(".png"))
                return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
                return "image/jpeg";
            if (lower.endsWith(".gif"))
                return "image/gif";
            if (lower.endsWith(".txt"))
                return "text/plain";
            if (lower.endsWith(".zip"))
                return "application/zip";
            return "application/octet-stream";
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }

        @Override
        public String getContentType() {
            return type;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
