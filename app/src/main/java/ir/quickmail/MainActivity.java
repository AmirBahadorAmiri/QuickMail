package ir.quickmail;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.amirbahadoramiri.quickmail.QuickMail;
import com.amirbahadoramiri.quickmail.QuickMailConfig;
import com.amirbahadoramiri.quickmail.QuickMailListener;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    // Email Address
    String email = "email@gmail.com";
    // Get a Gmail App Password from your Google Account.
    String password = "password";
    QuickMailConfig config = new QuickMailConfig(email, password);

    private static final String TAG = "MY_APP";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QuickMail.withAccount(config)
                .withTitle("Title For Test")
                .withBody("Body For Test")
                .withSenderName(getString(R.string.app_name))
                .toEmailAddresses("example@gmail.com", "sample@gmail.com")
                .withListener(new QuickMailListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess: ");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.d(TAG, "onFailure: " + t.getMessage());
                    }
                })
                .withRetry(1)
                .withTimeout(10000)
                .withLogs(true)
                .send();

    }
}
