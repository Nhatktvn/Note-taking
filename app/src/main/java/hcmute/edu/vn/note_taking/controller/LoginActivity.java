package hcmute.edu.vn.note_taking.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import hcmute.edu.vn.note_taking.R;
import hcmute.edu.vn.note_taking.sqlite.NoteTakingOpenHelper;
import hcmute.edu.vn.note_taking.utils.Constants;
import hcmute.edu.vn.note_taking.utils.NetworkUtils;

public class LoginActivity extends AppCompatActivity {

    EditText et_password;
    EditText et_email;
    Button btn_login;
    Button btn_register;
    TextView tv_forgot_password;

    NoteTakingOpenHelper openHelper;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        tv_forgot_password = findViewById(R.id.tv_forget_password);

        openHelper = new NoteTakingOpenHelper(this);
        et_email.setHint(Constants.getHOST(this));

        tv_forgot_password.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });

        btn_register.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistActivity.class);
            startActivity(intent);
            finish();
        });

        btn_login.setOnClickListener(v -> {
            String email = et_email.getText().toString();
            String password = et_password.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                new Thread(() -> {
                    String url = Constants.getHOST(LoginActivity.this) + "/auth/login";
                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("password", password);
                    JSONObject response = NetworkUtils.sendHttpRequest(url, "POST", params);
                    try {
                        if (response != null) {
                            if (response.getString("status").equals("success")) {
                                sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("email", email);
                                editor.apply();
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                runOnUiThread(() -> {
                                    try {
                                        Toast.makeText(LoginActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this, "An error occurred. Please try again later", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }
}
