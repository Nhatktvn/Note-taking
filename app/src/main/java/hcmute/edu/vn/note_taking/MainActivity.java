package hcmute.edu.vn.note_taking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import hcmute.edu.vn.note_taking.controller.LoginActivity;
import hcmute.edu.vn.note_taking.controller.RegisterActivity;

public class MainActivity extends AppCompatActivity {

    SharedPreferences userSharedPreferences;
    SharedPreferences settingsSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userSharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        settingsSharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);

        if (userSharedPreferences.getString("username", null) != null) {
            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric login for my app")
                    .setSubtitle("Log in using your biometric credential")
                    .setNegativeButtonText("Cancel")
                    .build();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P
                    && settingsSharedPreferences.getBoolean("biometric", false)) {
                BiometricPrompt biometricPrompt = new BiometricPrompt(this, getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Log.i("Biometric", "BIOMETRIC Authentication Succeeded" + result);
                        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                });
                biometricPrompt.authenticate(promptInfo);
            }
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }
}