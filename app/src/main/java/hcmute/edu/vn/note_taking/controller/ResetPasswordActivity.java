package hcmute.edu.vn.note_taking.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import hcmute.edu.vn.note_taking.R;
import hcmute.edu.vn.note_taking.utils.Constants;
import hcmute.edu.vn.note_taking.utils.NetworkUtils;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText et_email;
    EditText et_new_password;
    TextView tv_back_to_login;
    Button btn_submit_reset;
    SharedPreferences userSharedPreferences;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);
        et_email = findViewById(R.id.et_email);
        et_new_password = findViewById(R.id.et_new_password);
        tv_back_to_login = findViewById(R.id.tv_back_to_login);
        btn_submit_reset = findViewById(R.id.btn_submit_reset);

        userSharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        tv_back_to_login.setOnClickListener(v -> {
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btn_submit_reset.setOnClickListener(v -> {
            String email = et_email.getText().toString();
            String new_password = et_new_password.getText().toString();
            if (email.isEmpty()) {
                et_email.setError("Email is required");
                et_email.requestFocus();
                return;
            }
            if (new_password.isEmpty()) {
                et_new_password.setError("Password is required");
                et_new_password.requestFocus();
                return;
            }

            SharedPreferences.Editor editor = userSharedPreferences.edit();
            editor.remove("otp_type");
            editor.remove("email_otp");
            editor.remove("password");
            editor.apply();
            editor.putString("email_otp", email);
            editor.putString("password", new_password);
            editor.putString("otp_type", "reset_password");
            editor.apply();

            Intent intent = new Intent(ResetPasswordActivity.this, OTPConfirmationActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
