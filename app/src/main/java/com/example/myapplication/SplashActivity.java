package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

// Splash screen animé au lancement
public class SplashActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_splash);
        ImageView logo = findViewById(R.id.splashLogo);
        TextView title = findViewById(R.id.splashTitle);
        TextView sub = findViewById(R.id.splashSub);
        View progress = findViewById(R.id.splashProgress);

        logo.setScaleX(0f); logo.setScaleY(0f); logo.setAlpha(0f);
        logo.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(600).setInterpolator(new OvershootInterpolator(1.5f)).start();
        title.setTranslationY(30f);
        title.animate().alpha(1f).translationY(0f).setStartDelay(400).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        sub.animate().alpha(1f).setStartDelay(650).setDuration(400).start();
        progress.animate().alpha(1f).setStartDelay(800).setDuration(300).start();

        new Handler().postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2000);
    }
}
