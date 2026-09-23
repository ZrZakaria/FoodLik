package ma.ensa.foodlik;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.lang.ref.WeakReference;

import ma.ensa.foodlik.data.PreferencesManager;

public class LoginActivity extends AppCompatActivity {

    private PreferencesManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = new PreferencesManager(this);
        TextInputEditText email = findViewById(R.id.emailEditText);
        TextInputEditText password = findViewById(R.id.passwordEditText);
        Button loginBtn = findViewById(R.id.loginButton);
        TextView guestBtn = findViewById(R.id.guestButton);
        TextView signupBtn = findViewById(R.id.signupRedirect);

        loginBtn.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            String passText = password.getText().toString().trim();

            if (emailText.isEmpty() || passText.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                // Using AsyncTask as requested for the login process
                new LoginTask(this, emailText).execute(passText);
            }
        });

        guestBtn.setOnClickListener(v -> {
            prefs.setGuestSession();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });

        signupBtn.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    /**
     * AsyncTask to handle authentication in the background.
     */
    private static class LoginTask extends AsyncTask<String, Void, Boolean> {
        private final WeakReference<LoginActivity> activityReference;
        private final String email;

        LoginTask(LoginActivity context, String email) {
            activityReference = new WeakReference<>(context);
            this.email = email;
        }

        @Override
        protected void onPreExecute() {
            LoginActivity activity = activityReference.get();
            if (activity != null) {
                Toast.makeText(activity, "Logging in...", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String password = params[0];
            try {
                // Simulate a background operation delay (e.g. network call)
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            LoginActivity activity = activityReference.get();
            if (activity != null && activity.prefs != null) {
                return activity.prefs.authenticate(email, password);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            LoginActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (success) {
                activity.prefs.setUserSession(email, true);
                activity.startActivity(new Intent(activity, MainActivity.class));
                activity.finish();
            } else {
                Toast.makeText(activity, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
