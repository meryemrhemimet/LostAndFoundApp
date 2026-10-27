package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.util.LocaleHelper;

// Écran d'accueil avec boutons Connexion / Inscription
public class MainActivity extends AppCompatActivity {
    @Override protected void attachBaseContext(Context c) { super.attachBaseContext(LocaleHelper.setLocale(c)); }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnGoToLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        findViewById(R.id.btnGoToRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }
}
