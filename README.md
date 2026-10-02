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
    implementation("com.github.AmirBahadorAmiri:QuickMail:1.0.0")
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
        .withSender("you@gmail.com")
        .toEmailAddress("friend@gmail.com")
        .withListenner(new QuickMailListener() {
            @Override
            public void onSuccess() {
                Log.d("MY_APP", "onSuccess");
            }

            @Override
            public void onFailure(@NotNull Exception error) {
                Log.d("MY_APP", "onFailure: " + error.getMessage());
            }
        })
        .send();
```

Kotlin works the same way:

```kotlin
QuickMail.withAccount(QuickMailConfig("you@gmail.com", "your-app-password"))
    .withTitle("Hello from QuickMail")
    .withBody("Sent from Android.")
    .withSender("you@gmail.com")
    .toEmailAddress("friend@gmail.com")
    .withListenner(object : QuickMailListener {
        override fun onSuccess() = Log.d("MY_APP", "onSuccess")
        override fun onFailure(error: Exception) = Log.d("MY_APP", "onFailure: ${error.message}")
    })
    .send()
```

<details>
<summary>What the wrapper does for you</summary>

- **SMTP preset** — `smtp.gmail.com`, port `465`, SSL, `TLSv1.2`.
- **Off the main thread** — `AsyncTask`; results come back through `QuickMailListener`.
- **Auto HTML** — if the body looks like HTML, the content type switches to `text/html`.
- **Multiple recipients** — pass a comma-separated string.
- **`QuickMailConfig`** — optional `data class(email, password)` if you prefer not to pass two strings.

</details>

> ⚠️ Gmail needs an [App Password](https://myaccount.google.com/apppasswords), not your account password.

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

Issues and PRs welcome. If something fails, please include the `send()` call you used (credentials redacted) and the stack trace from `onFailure(error)`.

---

Made with ❤️ by [Amir Bahador Amiri](https://github.com/AmirBahadorAmiri)