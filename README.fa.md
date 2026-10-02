# QuickMail

> 🌍 [English version](./README.md)

یک wrapper نازک اندرویدی روی [JavaMail](https://javaee.github.io/javamail/) برای ارسال ایمیل روی SMTP. یک API زنجیره‌ای در یک خط، بدون کلاس تنظیمات، بدون UI.

[![JitPack](https://img.shields.io/jitpack/v/com.github.AmirBahadorAmiri/QuickMail?style=flat-square)](https://jitpack.io/#AmirBahadorAmiri/QuickMail)
[![Platform](https://img.shields.io/badge/platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/studio)
[![Language](https://img.shields.io/badge/language-Java%20%7C%20Kotlin-orange?style=flat-square)](https://developer.android.com/studio)
[![minSdk](https://img.shields.io/badge/minSdk-16-blueviolet?style=flat-square)](https://developer.android.com/about/versions)

---

## 📥 نصب

اول JitPack را به `build.gradle` ریشه پروژه اضافه کن:

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

بعد وابستگی را اضافه کن:

```groovy
dependencies {
    implementation("com.github.AmirBahadorAmiri:QuickMail:1.0.0")
}
```

مجوز `INTERNET` را هم به `AndroidManifest.xml` اضافه کن:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## ✨ نحوه استفاده

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

در کاتلین هم دقیقاً به همین شکل است:

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
<summary>این wrapper چه کاری برایت انجام می‌دهد</summary>

- **تنظیمات آماده SMTP** — `smtp.gmail.com`، پورت `465`، SSL و `TLSv1.2`.
- **خارج از نخ اصلی** — با `AsyncTask` اجرا می‌شود؛ نتیجه از طریق `QuickMailListener` برمی‌گردد.
- **تشخیص خودکار HTML** — اگر متن بدنه شبیه HTML باشد، نوع محتوا به `text/html` تغییر می‌کند.
- **چند گیرنده** — کافی است یک رشته با کاما پاس بدهی.
- **`QuickMailConfig`** — کلاس اختیاری `data class(email, password)` اگر ترجیح بدهی دو رشته پاس ندهی.

</details>

> ⚠️ جیمیل به‌جای رمز عبور حساب، [App Password](https://myaccount.google.com/apppasswords) می‌خواهد.

---

## ⚠️ تنظیم اجباری گریدل

این بلوک را داخل `android { }` ماژول اپ خودت (`build.gradle`) اضافه کن:

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

در DSL کاتلین (`build.gradle.kts`):

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

**بدون این بلوک، بیلد شکست می‌خورد.** کتابخانه‌های `android-mail`، `android-activation` و `jakarta.mail` هر کدام فایلی با مسیر یکسان دارند — `META-INF/NOTICE.md` و `META-INF/LICENSE.md` — و AGP روی تکراری بودنشان خطا می‌دهد. از `+=` استفاده کن نه `=`، تا excludeهای پیش‌فرض خود AGP پاک نشوند.

---

## 🤝 مشارکت

خوش‌آمدید به issue و PR. اگر مشکلی پیش آمد، لطفاً فراخوانی `send()` که استفاده کردید (با حذف اطلاعات محرمانه) و استک تریس `onFailure(error)` را بفرستید.

---

ساخته‌شده با ❤️ توسط [Amir Bahador Amiri](https://github.com/AmirBahadorAmiri)