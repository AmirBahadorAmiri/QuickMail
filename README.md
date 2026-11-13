# QuickMail

> 📖 [نسخه فارسی](./README.fa.md)

A thin Android wrapper around [JavaMail](https://javaee.github.io/javamail/) for sending e-mail over SMTP. Fluent one-liner API, no config classes, no UI.

[![JitPack](https://img.shields.io/jitpack/v/com.github.AmirBahadorAmiri/QuickMail?style=flat-square)](https://jitpack.io/#AmirBahadorAmiri/QuickMail)
[![Platform](https://img.shields.io/badge/platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/studio)
[![Language](https://img.shields.io/badge/language-Java%20%7C%20Kotlin-orange?style=flat-square)](https://developer.android.com/studio)
[![minSdk](https://img.shields.io/badge/minSdk-16-blueviolet?style=flat-square)](https://developer.android.com/about/versions)

---

## 📥 Install

Add JitPack to your root `build.gradle`:

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Then add the dependency:

```groovy
dependencies {
    implementation("com.github.AmirBahadorAmiri:QuickMail:1.1.0")
}
```

Add the `INTERNET` permission to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## ✨ Usage

```java
import com.amirbahadoramiri.quickmail.QuickMail;
import com.amirbahadoramiri.quickmail.QuickMailConfig;
import com.amirbahadoramiri.quickmail.QuickMailListener;

QuickMailConfig config = new QuickMailConfig("you@gmail.com", "your-app-password");

QuickMail.withAccount(config)
        .withTitle("Hello from QuickMail")
        .withBody("<h1>Hi 👋</h1><p>Sent from Android.</p>")
        .withSenderName("QuickMail App")
        .toEmailAddress("friend@gmail.com")
        .withListener(new QuickMailListener() {
            @Override
            public void onSuccess() {
                Log.d("MY_APP", "onSuccess");
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("MY_APP", "onFailure: " + t.getMessage());
            }
        })
        .send();
```

Kotlin works the same way:

```kotlin
QuickMail.withAccount(QuickMailConfig("you@gmail.com", "your-app-password"))
    .withTitle("Hello from QuickMail")
    .withBody("Sent from Android.")
    .withSenderName("QuickMail App")
    .toEmailAddress("friend@gmail.com")
    .withListener(object : QuickMailListener {
        override fun onSuccess() = Log.d("MY_APP", "onSuccess")
        override fun onFailure(t: Throwable) = Log.d("MY_APP", "onFailure: ${t.message}")
    })
    .send()
```

> ⚠️ Gmail needs an [App Password](https://myaccount.google.com/apppasswords), not your account password.

---

## 📚 API

### Account

| Method | Description |
| --- | --- |
| `withAccount(String email, String password)` | Creates a new builder for an account. |
| `withAccount(QuickMailConfig config)` | Same, using a config object. |

Every call returns a **new, independent instance**, so several mails can be prepared and sent concurrently without interfering with each other.

### Message

| Method | Description |
| --- | --- |
| `withTitle(String title)` | Subject line. |
| `withBody(String body)` | Plain-text body (HTML is auto-detected). |
| `withHtmlBody(String body)` | Forces the body to be sent as `text/html`. |
| `toEmailAddress(String address)` | Single recipient, or a comma-separated list. |
| `toEmailAddresses(String... addresses)` | Any number of recipients. |
| `withCc(String... addresses)` | Carbon-copy recipients. |
| `withBcc(String... addresses)` | Blind carbon-copy recipients. |
| `withReplyTo(String address)` | Reply-to address. |
| `withSenderName(String name)` | Display name in the From header. Must **not** be an email address. |
| `withSenderAddress(String address)` | Explicit From address (Gmail only allows the authenticated account). |

> 💡 `withSenderName` throws an `IllegalArgumentException` if you accidentally pass an email address, so a typo can never silently produce a broken `From` header. Use `withSenderAddress` when you really need an explicit address.

### Attachments

```java
QuickMail.withAccount(config)
        .withTitle("Report")
        .withBody("See the attached file.")
        .toEmailAddress("friend@gmail.com")
        .withAttachment("/storage/emulated/0/Download/report.pdf")
        .send();
```

```java
// content:// uri, e.g. from the Android file picker
QuickMail.withAccount(config)
        .withTitle("Report")
        .withBody("See the attached file.")
        .toEmailAddress("friend@gmail.com")
        .withAttachment(this, pickedUri)
        .send();
```

| Method | Description |
| --- | --- |
| `withAttachment(String filePath)` | Attaches a file by path. |
| `withAttachment(File file)` | Attaches a `File`. |
| `withAttachment(Context context, Uri uri)` | Attaches a `content://` uri (file picker, DocumentsProvider, …). The application context is used internally, so no activity leak. |
| `withAttachment(ContentResolver resolver, Uri uri)` | Same, with an explicit resolver. |

`withAttachment` can be called multiple times to attach several files. The display name is read from `OpenableColumns.DISPLAY_NAME` and the MIME type is derived from the file extension.

### SMTP & reliability

| Method | Description |
| --- | --- |
| `withHost(String host)` | SMTP host. Default `smtp.gmail.com`. |
| `withPort(int port)` | SMTP port. Default `465` (implicit SSL). Use `587` with `withStartTls(true)`. |
| `withStartTls(boolean)` | Uses STARTTLS instead of implicit SSL. |
| `withTimeout(int millis)` | Connection/read timeout. `0` (default) means no timeout. |
| `withRetry(int count)` | Number of extra attempts after a failed send. |
| `withLogs(boolean)` | Debug logging under the `QuickMail` tag in Logcat. |

### Sending

| Method | Description |
| --- | --- |
| `withListener(QuickMailListener listener)` | Receives `onSuccess()` / `onFailure(Throwable)`. |
| `validate()` | Checks the account, addresses and attachments. Throws with a descriptive message. |
| `send()` | Validates and sends the mail. |

```java
QuickMail.withAccount(config)
        .withTitle("Hello")
        .withBody("Hi!")
        .toEmailAddress("friend@gmail.com")
        .withRetry(2)
        .withTimeout(10000)
        .withLogs(true)
        .send();
```

---

## 🔍 What the wrapper does for you

- **SMTP preset** — `smtp.gmail.com`, port `465`, SSL, `TLSv1.2`, with optional custom host/port/STARTTLS.
- **Off the main thread** — an `ExecutorService` runs the SMTP work; callbacks are delivered on the main thread through a `Handler`.
- **Concurrent sends** — cached thread pool, so parallel mails don't queue behind each other.
- **Auto HTML** — if the body looks like HTML, the content type switches to `text/html`; `withHtmlBody` forces it.
- **UTF-8 everywhere** — subject and body are explicitly encoded, so Persian and other non-ASCII text stays intact.
- **Validation** — missing credentials, invalid addresses and unreadable attachments are caught before sending; validation failures are always logged (even with debug logs off) and reported through `onFailure`.
- **Safe sender header** — display name and address are set separately, so names with non-ASCII characters render correctly.

---

## ⚠️ Required Gradle config

Add this inside the `android { }` block of **your app module** (`build.gradle`):

```groovy
android {
    packagingOptions {
        resources {
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
        }
    }
}
```

Kotlin DSL (`build.gradle.kts`):

```kotlin
android {
    packaging {
        resources {
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
        }
    }
}
```

**Without this block the build fails.** `android-mail`, `android-activation` and `jakarta.mail` each bundle a file at the same path — `META-INF/NOTICE.md` and `META-INF/LICENSE.md` — and AGP aborts on the duplicate. Use `+=`, not `=`, so AGP's own excludes aren't wiped.

---

## 🤝 Contributing

Issues and PRs welcome. If something fails, please include the `send()` call you used (credentials redacted) and the stack trace from `onFailure(t)`.

---

Made with ❤️ by [Amir Bahador Amiri](https://github.com/AmirBahadorAmiri)
