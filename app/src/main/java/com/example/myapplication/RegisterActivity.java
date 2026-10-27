package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.data.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;

// Inscription avec SQLite locale
public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText edtUser, edtEmail, edtPhone, edtPassword;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_register);
        dbHelper = new DatabaseHelper(this);
        edtUser = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        findViewById(R.id.btnRegister).setOnClickListener(v -> register());
    }

    private void register() {
        String user = edtUser.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show(); return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show(); return;
        }
        if (pass.length() < 6) {
            Toast.makeText(this, R.string.error_short_password, Toast.LENGTH_SHORT).show(); return;
        }
        if (dbHelper.emailExists(email)) {
            Toast.makeText(this, R.string.error_email_exists, Toast.LENGTH_SHORT).show(); return;
        }

        if (dbHelper.registerUser(user, email, pass, phone)) {
            Toast.makeText(this, R.string.success_register, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        } else { Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show(); }
    }
}
