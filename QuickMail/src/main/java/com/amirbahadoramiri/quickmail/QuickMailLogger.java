package com.amirbahadoramiri.quickmail;

import android.util.Log;

final class QuickMailLogger {

    private static final String TAG = "QuickMail";
    private static boolean enabled = false;

    private QuickMailLogger() {
    }

    static void setEnabled(boolean enabled) {
        QuickMailLogger.enabled = enabled;
    }

    static void log(String message) {
        if (enabled)
            Log.d(TAG, message);
    }
}
