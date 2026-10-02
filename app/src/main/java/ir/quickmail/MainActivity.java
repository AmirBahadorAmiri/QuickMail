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

    String email = "email";
    String password = "password";
    QuickMailConfig config = new QuickMailConfig(email, password);

    private static final String TAG = "MY_APP";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QuickMail.withAccount(config)
                .withTitle("Title For Test")
                .withBody("Body For Test")
                .withSender(getString(R.string.app_name))
                .toEmailAddress("example@gmail.com , sample@gmail.com")
                .withListenner(new QuickMailListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess: ");
                    }

                    @Override
                    public void onFailure(@NotNull Exception error) {
                        Log.d(TAG, "onFailure: " + error.getMessage());
                    }
                })
                .send();

    }
}
