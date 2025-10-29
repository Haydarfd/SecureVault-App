package com.example.securefilestorageappdemo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securefilestorageappdemo.database.AppDatabase;
import com.example.securefilestorageappdemo.models.User;
import com.example.securefilestorageappdemo.utils.HashUtils;

public class RegistrationActivity extends AppCompatActivity {

    EditText usernameField, passwordField;
    Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        usernameField = findViewById(R.id.usernameRegisterField);
        passwordField = findViewById(R.id.passwordRegisterField);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            AppDatabase db = AppDatabase.getInstance(this);
            if (db.userDao().getUserByUsername(username) != null) {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            String hash = HashUtils.sha256(password);
            db.userDao().insertUser(new User(username, hash));

            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
