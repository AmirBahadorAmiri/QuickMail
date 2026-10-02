group = "com.amirbahadoramiri"
version = "1.0.2"

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.amirbahadoramiri.quickmail"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 16

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    implementation("com.sun.mail:android-mail:1.6.8")
    implementation("com.sun.mail:android-activation:1.6.8")
}