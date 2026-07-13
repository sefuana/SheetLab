package com.axsaafe.sheetlab.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.axsaafe.sheetlab.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View logo = findViewById(R.id.iv_logo);
        TextView tvAppName = findViewById(R.id.tv_app_name);
        TextView tvTagline = findViewById(R.id.tv_tagline);
        TextView tvOwner = findViewById(R.id.tv_owner);
        View glowLine = findViewById(R.id.glow_line);

        // Logo bounce in
        logo.setAlpha(0f);
        logo.setScaleX(0.3f);
        logo.setScaleY(0.3f);

        ObjectAnimator alphaLogo = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0.3f, 1f);
        alphaLogo.setDuration(700);
        scaleX.setDuration(700);
        scaleY.setDuration(700);
        scaleX.setInterpolator(new BounceInterpolator());
        scaleY.setInterpolator(new BounceInterpolator());

        AnimatorSet logoSet = new AnimatorSet();
        logoSet.playTogether(alphaLogo, scaleX, scaleY);
        logoSet.start();

        // App name slide in
        tvAppName.setAlpha(0f);
        tvAppName.setTranslationY(60f);
        new Handler().postDelayed(() -> {
            ObjectAnimator nameAlpha = ObjectAnimator.ofFloat(tvAppName, "alpha", 0f, 1f);
            ObjectAnimator nameY = ObjectAnimator.ofFloat(tvAppName, "translationY", 60f, 0f);
            nameAlpha.setDuration(500);
            nameY.setDuration(500);
            nameY.setInterpolator(new AccelerateDecelerateInterpolator());
            AnimatorSet nameSet = new AnimatorSet();
            nameSet.playTogether(nameAlpha, nameY);
            nameSet.start();
        }, 600);

        // Glow line expand
        glowLine.setScaleX(0f);
        glowLine.setAlpha(0f);
        new Handler().postDelayed(() -> {
            ObjectAnimator lineScale = ObjectAnimator.ofFloat(glowLine, "scaleX", 0f, 1f);
            ObjectAnimator lineAlpha = ObjectAnimator.ofFloat(glowLine, "alpha", 0f, 1f);
            lineScale.setDuration(600);
            lineAlpha.setDuration(400);
            lineScale.setInterpolator(new AccelerateDecelerateInterpolator());
            AnimatorSet lineSet = new AnimatorSet();
            lineSet.playTogether(lineScale, lineAlpha);
            lineSet.start();
        }, 1000);

        // Tagline fade in
        tvTagline.setAlpha(0f);
        new Handler().postDelayed(() -> {
            ObjectAnimator tagAlpha = ObjectAnimator.ofFloat(tvTagline, "alpha", 0f, 1f);
            tagAlpha.setDuration(500);
            tagAlpha.start();
        }, 1400);

        // Owner fade in
        tvOwner.setAlpha(0f);
        new Handler().postDelayed(() -> {
            ObjectAnimator ownerAlpha = ObjectAnimator.ofFloat(tvOwner, "alpha", 0f, 1f);
            ownerAlpha.setDuration(500);
            ownerAlpha.start();
        }, 1800);

        // Go to Login
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 3000);
    }
}
