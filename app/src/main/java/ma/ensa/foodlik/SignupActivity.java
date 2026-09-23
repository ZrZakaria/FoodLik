package ma.ensa.foodlik;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import ma.ensa.foodlik.data.PreferencesManager;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        PreferencesManager prefs = new PreferencesManager(this);
        TextInputEditText name = findViewById(R.id.nameEditText);
        TextInputEditText email = findViewById(R.id.emailEditText);
        TextInputEditText password = findViewById(R.id.passwordEditText);
        TextInputEditText confirmPassword = findViewById(R.id.confirmPasswordEditText);
        Button signupBtn = findViewById(R.id.signupButton);
        TextView loginBtn = findViewById(R.id.loginRedirect);

        signupBtn.setOnClickListener(v -> {
            String nameText = name.getText().toString().trim();
            String emailText = email.getText().toString().trim();
            String passText = password.getText().toString().trim();
            String confirmPassText = confirmPassword.getText().toString().trim();

            if (nameText.isEmpty() || emailText.isEmpty() || passText.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (passText.length() < 6) {
                password.setError("Password must be at least 6 characters");
            } else if (!passText.equals(confirmPassText)) {
                confirmPassword.setError("Passwords do not match");
            } else {
                // Register the user in persistent storage
                if (prefs.registerUser(nameText, emailText, passText)) {
                    // Register session
                    prefs.setUserSession(emailText, true);
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "User already exists with this email", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginBtn.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }
}
