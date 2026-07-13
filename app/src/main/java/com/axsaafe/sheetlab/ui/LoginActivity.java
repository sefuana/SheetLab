package com.axsaafe.sheetlab.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.axsaafe.sheetlab.R;
import com.axsaafe.sheetlab.SheetLabApp;

public class LoginActivity extends AppCompatActivity {

    private static final String CORRECT_USERNAME = "Sheetlabpro";
    private static final String CORRECT_PASSWORD = "axSaaFe-admin";
    private static final String KEY_LOGGED_IN = "logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Skip login if already logged in
        SharedPreferences prefs = getSharedPreferences(SheetLabApp.PREF_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_LOGGED_IN, false)) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        EditText etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        View card = findViewById(R.id.login_card);

        // Card slide up animation
        card.setTranslationY(200f);
        card.setAlpha(0f);
        card.animate().translationY(0f).alpha(1f).setDuration(600).start();

        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (user.equals(CORRECT_USERNAME) && pass.equals(CORRECT_PASSWORD)) {
                prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply();
                goToMain();
            } else {
                // Shake animation
                card.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
                Toast.makeText(this, "Wrong username or password!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}
