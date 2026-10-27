package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.util.NotificationHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

// Connexion via SQLite locale + SharedPreferences pour la session
public class LoginActivity extends AppCompatActivity {
    private TextInputEditText edtEmail, edtPassword;
    private TextInputLayout emailLayout, passwordLayout;
    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        dbHelper = new DatabaseHelper(this);
        prefs = getSharedPreferences("UserSession", MODE_PRIVATE);

        // Redirection si déjà connecté
        if (prefs.getBoolean("isLoggedIn", false)) { goToMain(); return; }

        setContentView(R.layout.activity_login);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> login());
    }

    private void login() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        emailLayout.setError(null); passwordLayout.setError(null);

        if (email.isEmpty() || password.isEmpty()) {
            if (email.isEmpty()) emailLayout.setError(getString(R.string.error_empty_fields));
            if (password.isEmpty()) passwordLayout.setError(getString(R.string.error_empty_fields));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.error_invalid_email)); return;
        }

        Cursor c = dbHelper.loginUser(email, password);
        if (c.moveToFirst()) {
            String id = String.valueOf(c.getLong(c.getColumnIndexOrThrow("id")));
            String name = c.getString(c.getColumnIndexOrThrow("username"));
            String phone = c.getString(c.getColumnIndexOrThrow("phone"));
            c.close();

            prefs.edit()
                    .putBoolean("isLoggedIn", true)
                    .putString("userId", id)
                    .putString("username", name)
                    .putString("userEmail", email)
                    .putString("phone", phone)
                    .apply();

            Toast.makeText(this, R.string.success_login, Toast.LENGTH_SHORT).show();

            // Notification de bienvenue
            NotificationHelper.notifyWelcome(this, name);

            goToMain();
        } else {
            c.close();
            passwordLayout.setError(getString(R.string.error_login));
        }
    }

    private void goToMain() {
        Intent i = new Intent(this, LostItemsActivity.class);
        i.putExtra("USER_ID", prefs.getString("userId", ""));
        i.putExtra("USER_NAME", prefs.getString("username", ""));
        i.putExtra("USER_EMAIL", prefs.getString("userEmail", ""));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i); finish();
    }
}
