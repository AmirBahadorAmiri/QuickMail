package com.amirbahadoramiri.quickmail;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.InternetAddress;

/**
 * Fluent builder for sending a mail.
 * <p>
 * Instances are not shared: every {@link #withAccount(String, String)} call creates a new object,
 * so several mails can be prepared and sent independently and from different threads.
 */
public class QuickMail {

    private static final String TAG = "QuickMail";

    private final String email;
    private final String pass;

    private String title;
    private String body;
    private String emailAddress;
    private String senderName;
    private String senderAddress;
    private QuickMailListener listener;

    private String ccAddresses;
    private String bccAddresses;
    private String replyTo;
    private boolean htmlBody;
    private final List<Attachment> attachments = new ArrayList<>();

    private String host = "smtp.gmail.com";
    private int port = 465;
    private boolean startTls = false;
    private int timeout = 0;
    private int retryCount = 0;

    private QuickMail(String email, String pass) {
        this.email = email;
        this.pass = pass;
    }

    public static QuickMail withAccount(String email, String pass) {
        return new QuickMail(email, pass);
    }

    public static QuickMail withAccount(QuickMailConfig config) {
        if (config == null)
            throw new IllegalArgumentException("Config must not be null");
        return new QuickMail(config.getEmail(), config.getPassword());
    }

    public QuickMail withTitle(String title) {
        this.title = title;
        return this;
    }

    public QuickMail withBody(String body) {
        this.body = body;
        this.htmlBody = false;
        return this;
    }

    /** Sets the body and forces it to be sent as HTML instead of auto-detecting. */
    public QuickMail withHtmlBody(String body) {
        this.body = body;
        this.htmlBody = true;
        return this;
    }

    /** Accepts a single address or a comma separated list of addresses. */
    public QuickMail toEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
        return this;
    }

    /** Accepts any number of recipient addresses. */
    public QuickMail toEmailAddresses(String... emailAddresses) {
        this.emailAddress = join(emailAddresses);
        return this;
    }

    public QuickMail withCc(String... ccAddresses) {
        this.ccAddresses = join(ccAddresses);
        return this;
    }

    public QuickMail withBcc(String... bccAddresses) {
        this.bccAddresses = join(bccAddresses);
        return this;
    }

    public QuickMail withReplyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    /**
     * Sets the display name shown in the From header, for example "QuickMail App".
     * <p>
     * This is a name only, never an email address: passing an address throws an
     * {@link IllegalArgumentException}. Use {@link #withSenderAddress(String)} if you really
     * need to send from a different address than the account one.
     */
    public QuickMail withSenderName(String senderName) {
        if (senderName != null && senderName.indexOf('@') >= 0)
            throw new IllegalArgumentException("withSenderName() expects a display name, not an email address."
                    + " Use withSenderAddress(\"" + senderName + "\") to set an address explicitly.");
        this.senderName = senderName;
        return this;
    }

    /** Sets an explicit From address. Gmail only allows addresses belonging to the authenticated account. */
    public QuickMail withSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
        return this;
    }

    /**
     * @deprecated ambiguous by design, it accepted both a name and an address.
     * Use {@link #withSenderName(String)} or {@link #withSenderAddress(String)} instead.
     */
    @Deprecated
    public QuickMail withSender(String sender) {
        if (sender != null && sender.indexOf('@') >= 0)
            this.senderAddress = sender;
        else
            this.senderName = sender;
        return this;
    }

    /** Attaches a file to the mail. Can be called multiple times. The file must be readable when send() is called. */
    public QuickMail withAttachment(String filePath) {
        attachments.add(Attachment.fromFile(new File(filePath)));
        return this;
    }

    /** Attaches a file to the mail. Can be called multiple times. */
    public QuickMail withAttachment(File file) {
        attachments.add(Attachment.fromFile(file));
        return this;
    }

    /**
     * Attaches a content uri, for example the result of a file picker.
     * The uri is read through the given context's ContentResolver when the mail is sent.
     */
    public QuickMail withAttachment(Context context, Uri uri) {
        if (context == null)
            throw new IllegalArgumentException("Context must not be null");
        return withAttachment(context.getApplicationContext().getContentResolver(), uri);
    }

    /** Attaches a content uri using the given resolver. */
    public QuickMail withAttachment(ContentResolver resolver, Uri uri) {
        if (resolver == null)
            throw new IllegalArgumentException("ContentResolver must not be null");
        if (uri == null)
            throw new IllegalArgumentException("Uri must not be null");
        attachments.add(Attachment.fromUri(resolver, uri));
        return this;
    }

    /** SMTP server host. Defaults to smtp.gmail.com. */
    public QuickMail withHost(String host) {
        this.host = host;
        return this;
    }

    /** SMTP server port. Defaults to 465 (implicit SSL). Use 587 together with withStartTls(true). */
    public QuickMail withPort(int port) {
        this.port = port;
        return this;
    }

    /** Uses STARTTLS instead of implicit SSL. Required for port 587. */
    public QuickMail withStartTls(boolean startTls) {
        this.startTls = startTls;
        return this;
    }

    /** Socket timeout in milliseconds for connecting and reading. 0 (default) means no timeout. */
    public QuickMail withTimeout(int timeoutMillis) {
        this.timeout = timeoutMillis;
        return this;
    }

    /** Number of extra attempts after a failed send. */
    public QuickMail withRetry(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    /** Enables debug logging with the "QuickMail" tag in Logcat. */
    public QuickMail withLogs(boolean logs) {
        QuickMailLogger.setEnabled(logs);
        return this;
    }

    public QuickMail withListener(QuickMailListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * @deprecated typo in the original API. Use {@link #withListener(QuickMailListener)} instead.
     */
    @Deprecated
    public QuickMail withListenner(QuickMailListener listener) {
        return withListener(listener);
    }

    /** Checks that everything needed to send the mail is set and valid. Throws when something is missing. */
    public void validate() {
        if (email == null || email.trim().isEmpty())
            throw new IllegalStateException("No account email set, call withAccount() first");
        if (pass == null || pass.isEmpty())
            throw new IllegalStateException("No account password set, call withAccount() first");
        validateAddress(email, "account email");
        if (emailAddress == null || emailAddress.trim().isEmpty())
            throw new IllegalStateException("No recipient set, call toEmailAddress() first");
        validateAddress(emailAddress, "recipient address");
        if (ccAddresses != null)
            validateAddress(ccAddresses, "cc address");
        if (bccAddresses != null)
            validateAddress(bccAddresses, "bcc address");
        if (replyTo != null)
            validateAddress(replyTo, "reply-to address");
        if (senderAddress != null)
            validateAddress(senderAddress, "sender address");
        if (body == null)
            throw new IllegalStateException("No body set, call withBody() first");
        for (Attachment attachment : attachments) {
            if (!attachment.isReadable())
                throw new IllegalStateException("Attachment is not readable: " + attachment.describe());
        }
    }

    public void send() {
        send(this.retryCount);
    }

    private void send(int attemptsLeft) {
        try {
            validate();
        } catch (Exception e) {
            // Always logged, even when debug logs are disabled, so failures are never silent.
            Log.e(TAG, "Mail not sent: " + e.getMessage(), e);
            QuickMailLogger.log("Mail not sent: " + e.getMessage());
            if (listener != null) listener.onFailure(e);
            return;
        }

        final QuickMailListener userListener = listener;
        final int attemptsRemaining = attemptsLeft;

        QuickMailListener retryingListener = new QuickMailListener() {
            @Override
            public void onSuccess() {
                if (userListener != null) userListener.onSuccess();
            }

            @Override
            public void onFailure(Throwable t) {
                if (attemptsRemaining > 0) {
                    QuickMailLogger.log("Send failed (" + t.getMessage() + "), retrying, "
                            + attemptsRemaining + " attempt(s) left");
                    send(attemptsRemaining - 1);
                } else if (userListener != null) {
                    userListener.onFailure(t);
                }
            }
        };

        QuickMailLogger.log("Sending mail from " + email + " to " + emailAddress);

        QuickMailAuthenticator mailSender = new QuickMailAuthenticator(
                email, pass, retryingListener, host, port, startTls, timeout);
        mailSender.sendMail(title == null ? "" : title, body, resolveSenderAddress(),
                senderName, emailAddress, ccAddresses, bccAddresses, replyTo, htmlBody, attachments);
    }

    /**
     * Resolves the From address. When only a display name is set, the account email is used
     * as the address.
     */
    private String resolveSenderAddress() {
        if (senderAddress != null && !senderAddress.trim().isEmpty())
            return senderAddress;
        return email;
    }

    private static void validateAddress(String addresses, String what) {
        try {
            InternetAddress[] parsed = InternetAddress.parse(addresses);
            if (parsed == null || parsed.length == 0)
                throw new IllegalArgumentException("no address parsed");
            for (InternetAddress address : parsed)
                address.validate();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid " + what + ": " + addresses, e);
        }
    }

    private static String join(String[] addresses) {
        if (addresses == null || addresses.length == 0)
            return null;
        StringBuilder result = new StringBuilder();
        for (String address : addresses) {
            if (address == null || address.trim().isEmpty())
                continue;
            if (result.length() > 0)
                result.append(",");
            result.append(address.trim());
        }
        return result.length() == 0 ? null : result.toString();
    }

    /** A file or a content uri that will be attached to the mail. */
    static final class Attachment {

        private final File file;
        private final ContentResolver resolver;
        private final Uri uri;
        private final String displayName;

        private Attachment(File file, ContentResolver resolver, Uri uri, String displayName) {
            this.file = file;
            this.resolver = resolver;
            this.uri = uri;
            this.displayName = displayName;
        }

        static Attachment fromFile(File file) {
            return new Attachment(file, null, null, file == null ? null : file.getName());
        }

        static Attachment fromUri(ContentResolver resolver, Uri uri) {
            return new Attachment(null, resolver, uri, queryDisplayName(resolver, uri));
        }

        boolean isFile() {
            return file != null;
        }

        File getFile() {
            return file;
        }

        ContentResolver getResolver() {
            return resolver;
        }

        Uri getUri() {
            return uri;
        }

        String getDisplayName() {
            return displayName;
        }

        String describe() {
            if (file != null)
                return file.getAbsolutePath();
            return String.valueOf(uri);
        }

        boolean isReadable() {
            if (file != null)
                return file.exists() && file.canRead();
            if (resolver == null || uri == null)
                return false;
            InputStream stream = null;
            try {
                stream = resolver.openInputStream(uri);
                return stream != null;
            } catch (Exception e) {
                return false;
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        private static String queryDisplayName(ContentResolver resolver, Uri uri) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        String name = cursor.getString(index);
                        if (name != null && !name.trim().isEmpty())
                            return name;
                    }
                }
            } catch (Exception ignored) {
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            String segment = uri.getLastPathSegment();
            return segment == null ? "attachment" : segment;
        }
    }
}
