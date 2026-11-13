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
    implementation("com.github.AmirBahadorAmiri:QuickMail:1.1.0")
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

در کاتلین هم دقیقاً به همین شکل است:

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

> ⚠️ جیمیل به‌جای رمز عبور حساب، [App Password](https://myaccount.google.com/apppasswords) می‌خواهد.

---

## 📚 API

### حساب

| متد | توضیح |
| --- | --- |
| `withAccount(String email, String password)` | یک builder جدید برای یک حساب می‌سازد. |
| `withAccount(QuickMailConfig config)` | همان، با استفاده از شیء config. |

هر فراخوانی یک **نمونه‌ی مستقل و جدید** برمی‌گرداند، پس می‌توانی چند ایمیل را هم‌زمان آماده و ارسال کنی بدون اینکه روی هم اثر بگذارند.

### پیام

| متد | توضیح |
| --- | --- |
| `withTitle(String title)` | موضوع ایمیل. |
| `withBody(String body)` | بدنه‌ی متنی (HTML به‌صورت خودکار تشخیص داده می‌شود). |
| `withHtmlBody(String body)` | بدنه را به‌اجبار به‌صورت `text/html` می‌فرستد. |
| `toEmailAddress(String address)` | یک گیرنده، یا رشته‌ای از آدرس‌ها با کاما. |
| `toEmailAddresses(String... addresses)` | هر تعداد گیرنده. |
| `withCc(String... addresses)` | گیرنده‌های رونوشت (CC). |
| `withBcc(String... addresses)` | گیرنده‌های رونوشت مخفی (BCC). |
| `withReplyTo(String address)` | آدرس پاسخ. |
| `withSenderName(String name)` | نام نمایشی در هدر From. **نباید** آدرس ایمیل باشد. |
| `withSenderAddress(String address)` | آدرس صریح From (جیمیل فقط آدرس حساب احراز هویت‌شده را قبول می‌کند). |

> 💡 اگر اشتباهی آدرس ایمیل بدهی، `withSenderName` یک `IllegalArgumentException` می‌دهد؛ پس هیچ‌وقت به‌خاطر یک تایپ، هدر From خراب نمی‌شود. برای آدرس صریح از `withSenderAddress` استفاده کن.

### پیوست‌ها

```java
QuickMail.withAccount(config)
        .withTitle("Report")
        .withBody("See the attached file.")
        .toEmailAddress("friend@gmail.com")
        .withAttachment("/storage/emulated/0/Download/report.pdf")
        .send();
```

```java
// آدرس content://، مثلاً از فایل‌پیکر اندروید
QuickMail.withAccount(config)
        .withTitle("Report")
        .withBody("See the attached file.")
        .toEmailAddress("friend@gmail.com")
        .withAttachment(this, pickedUri)
        .send();
```

| متد | توضیح |
| --- | --- |
| `withAttachment(String filePath)` | پیوست فایل با مسیر. |
| `withAttachment(File file)` | پیوست یک `File`. |
| `withAttachment(Context context, Uri uri)` | پیوست آدرس `content://` (فایل‌پیکر، DocumentsProvider و…). از application context استفاده می‌شود، پس Activity leak نمی‌کند. |
| `withAttachment(ContentResolver resolver, Uri uri)` | همان، با resolver مشخص. |

`withAttachment` را می‌توانی چند بار صدا بزنی تا چند فایل پیوست کنی. نام نمایشی از `OpenableColumns.DISPLAY_NAME` خوانده می‌شود و نوع MIME از پسوند فایل تشخیص داده می‌شود.

### SMTP و پایداری

| متد | توضیح |
| --- | --- |
| `withHost(String host)` | هاست SMTP. پیش‌فرض `smtp.gmail.com`. |
| `withPort(int port)` | پورت SMTP. پیش‌فرض `465` (SSL ضمنی). برای `587` از `withStartTls(true)` استفاده کن. |
| `withStartTls(boolean)` | استفاده از STARTTLS به‌جای SSL ضمنی. |
| `withTimeout(int millis)` | تایم‌اوت اتصال/خواندن. `0` (پیش‌فرض) یعنی بدون تایم‌اوت. |
| `withRetry(int count)` | تعداد تلاش مجدد اضافه بعد از شکست ارسال. |
| `withLogs(boolean)` | لاگ دیباگ با تگ `QuickMail` در Logcat. |

### ارسال

| متد | توضیح |
| --- | --- |
| `withListener(QuickMailListener listener)` | دریافت `onSuccess()` / `onFailure(Throwable)`. |
| `validate()` | حساب، آدرس‌ها و پیوست‌ها را بررسی می‌کند و در صورت مشکل خطای توصیفی می‌دهد. |
| `send()` | اعتبارسنجی می‌کند و ایمیل را می‌فرستد. |

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

## 🔍 این wrapper چه کاری برایت انجام می‌دهد

- **تنظیم آماده SMTP** — `smtp.gmail.com`، پورت `465`، SSL و `TLSv1.2`، با امکان تغییر host/port/STARTTLS.
- **خارج از نخ اصلی** — کار SMTP روی یک `ExecutorService` انجام می‌شود؛ callbackها با `Handler` روی نخ اصلی تحویل داده می‌شوند.
- **ارسال هم‌زمان** — thread pool از نوع cached، پس ایمیل‌های موازی پشت سر هم صف نمی‌شوند.
- **تشخیص خودکار HTML** — اگر متن بدنه شبیه HTML باشد، نوع محتوا به `text/html` تغییر می‌کند؛ `withHtmlBody` آن را اجباری می‌کند.
- **UTF-8 همه‌جا** — موضوع و بدنه صریحاً انکد می‌شوند، پس متن فارسی و سایر کاراکترهای غیر ASCII سالم می‌مانند.
- **اعتبارسنجی** — نبود اطلاعات حساب، آدرس نامعتبر و پیوست غیرقابل‌خواندن پیش از ارسال گرفته می‌شود؛ خطای اعتبارسنجی همیشه لاگ می‌شود (حتی با لاگ دیباگ خاموش) و از طریق `onFailure` هم گزارش می‌شود.
- **هدر From امن** — نام نمایشی و آدرس جداگانه ست می‌شوند، پس نام‌های غیر ASCII درست نمایش داده می‌شوند.

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

خوش‌آمدید به issue و PR. اگر مشکلی پیش آمد، لطفاً فراخوانی `send()` که استفاده کردید (با حذف اطلاعات محرمانه) و استک تریس `onFailure(t)` را بفرستید.

---

ساخته‌شده با ❤️ توسط [Amir Bahador Amiri](https://github.com/AmirBahadorAmiri)
