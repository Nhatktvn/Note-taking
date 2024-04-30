package hcmute.edu.vn.note_taking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import hcmute.edu.vn.note_taking.activities.MainActivity;
import hcmute.edu.vn.note_taking.activities.LoginActivity;
import hcmute.edu.vn.note_taking.utils.Constants;

public class EntryActivity extends AppCompatActivity {

    SharedPreferences userSharedPreferences;
    SharedPreferences settingsSharedPreferences;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userSharedPreferences = getApplicationContext().getSharedPreferences(Constants.USER_SHARED_PREFERENCES, MODE_PRIVATE);
        settingsSharedPreferences = getApplicationContext().getSharedPreferences(Constants.SETTINGS_SHARED_PREFERENCES, MODE_PRIVATE);

        // Kiểm tra xem thiết bị có hỗ trợ vân tay hay không
        if (settingsSharedPreferences.getBoolean("biometric", true)) {
            if (userSharedPreferences.getString("email", null) != null) {
                if (Build.VERSION_CODES.P <= Build.VERSION.SDK_INT) {
                    createAndAuth();
                }
            } else {
                // Nếu không có người dùng nào đã đăng nhập, chuyển hướng đến màn hình đăng nhập
                Intent intent = new Intent(EntryActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            // Nếu thiết bị không hỗ trợ vân tay, chuyển hướng đến màn hình đăng nhập
            Intent intent = new Intent(EntryActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void createAndAuth() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Cancel")
                .build();
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Intent intent = new Intent(EntryActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Intent intent = new Intent(EntryActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        biometricPrompt.authenticate(promptInfo);
    }
}
