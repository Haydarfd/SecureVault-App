package com.example.securefilestorageappdemo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securefilestorageappdemo.database.AppDatabase;
import com.example.securefilestorageappdemo.models.User;
import com.example.securefilestorageappdemo.utils.HashUtils;
import com.example.securefilestorageappdemo.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameField, passwordField;
    private Button loginButton;
    private TextView registerRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);
        registerRedirect = findViewById(R.id.registerRedirect);

        loginButton.setOnClickListener(v -> handleLogin());
        registerRedirect.setOnClickListener(v -> startActivity(new Intent(this, RegistrationActivity.class)));
    }

    private void handleLogin() {
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase db = AppDatabase.getInstance(this);
        User user = db.userDao().getUserByUsername(username);

        if (user == null) {
            Toast.makeText(this, "Invalid username", Toast.LENGTH_SHORT).show();
            return;
        }

        String inputHash = HashUtils.sha256(password);
        if (!user.passwordHash.equals(inputHash)) {
            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save user session for multi-user separation
        SessionManager.saveUser(this, username);

        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
