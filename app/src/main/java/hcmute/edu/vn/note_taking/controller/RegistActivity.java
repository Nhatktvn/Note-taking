package hcmute.edu.vn.note_taking.controller;

import static hcmute.edu.vn.note_taking.utils.NetworkUtils.sendHttpRequest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import hcmute.edu.vn.note_taking.R;
import hcmute.edu.vn.note_taking.utils.Constants;

public class RegistActivity extends AppCompatActivity {

    EditText et_email;
    EditText et_username;
    EditText et_password;
    EditText et_confirm_password;

    Button btn_submit_regist;
    TextView tv_back_to_login;

    SharedPreferences userSharedPreferences;
    SharedPreferences settingsSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        et_email = findViewById(R.id.et_email);
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        et_confirm_password = findViewById(R.id.et_confirm_password);
        btn_submit_regist = findViewById(R.id.btn_submit_regist);
        tv_back_to_login = findViewById(R.id.tv_back_to_login);

        userSharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        settingsSharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);

        tv_back_to_login.setOnClickListener(v -> {
            Intent intent = new Intent(RegistActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btn_submit_regist.setOnClickListener(v -> {
            String email = et_email.getText().toString();
            String username = et_username.getText().toString();
            String password = et_password.getText().toString();
            String confirm_password = et_confirm_password.getText().toString();
            if (email.isEmpty()) {
                et_email.setError("Please enter email");
            }
            if (username.isEmpty()) {
                et_username.setError("Please enter username");
            }
            if (password.isEmpty()) {
                et_password.setError("Please enter password");
            }
            if (!password.equals(confirm_password)) {
                et_confirm_password.setError("Password not match");
            }
            if (!email.isEmpty() && !username.isEmpty() && !password.isEmpty() && password.equals(confirm_password))
                new Thread(() -> {
                    String urlString = Constants.getHOST(this) + "/auth/regist";
                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("username", username);
                    params.put("password", password);
                    JSONObject jsonResponse = sendHttpRequest(urlString, "POST", params);

                    try {
                        assert jsonResponse != null;
                        if (jsonResponse.getString("status").equals("success")) {
                            SharedPreferences.Editor editor = userSharedPreferences.edit();
                            editor.remove("otp_type");
                            editor.remove("email_otp");
                            editor.remove("password");
                            editor.remove("username");
                            editor.apply();
                            editor.putString("email_otp", email);
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.putString("otp_type", "regist");
                            editor.apply();

                            Intent intent = new Intent(RegistActivity.this, OTPConfirmationActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(RegistActivity.this, "Register failed", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(RegistActivity.this, "Register failed", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
        });

        et_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.charAt(s.length() - 1) == '\n') {
                    s.delete(s.length() - 1, s.length());
                    et_email.requestFocus();
                }
            }
        });

        et_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.charAt(s.length() - 1) == '\n') {
                    s.delete(s.length() - 1, s.length());
                    et_password.requestFocus();
                }
            }
        });

        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.charAt(s.length() - 1) == '\n') {
                    s.delete(s.length() - 1, s.length());
                    et_confirm_password.requestFocus();
                }
            }
        });

        et_confirm_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.charAt(s.length() - 1) == '\n') {
                    s.delete(s.length() - 1, s.length());
                    btn_submit_regist.callOnClick();
                }
            }
        });

    }
}