package hcmute.edu.vn.note_taking.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import hcmute.edu.vn.note_taking.utils.NetworkUtils;

public class OTPConfirmationActivity extends AppCompatActivity {

    final int RESEND_OTP_DELAY = 60;
    EditText et_code;
    TextView tv_resend_code;
    TextView tv_back_to_login;
    Button btn_submit_OTP;

    SharedPreferences userSharedPreferences;
    String token = "";
    String email = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_code);

        et_code = findViewById(R.id.et_code);
        tv_resend_code = findViewById(R.id.tv_resend_code);
        tv_back_to_login = findViewById(R.id.tv_back_to_login);
        btn_submit_OTP = findViewById(R.id.btn_submit_OTP);

        userSharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        new Thread(this::request_otp).start();

        email = userSharedPreferences.getString("email_otp", null);
        if (email != null) {

            tv_back_to_login.setOnClickListener(v -> new Thread(() -> {
                Intent intent = new Intent(OTPConfirmationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }));

            tv_resend_code.setOnClickListener(v -> new Thread(() -> {
                int count = RESEND_OTP_DELAY;
                new Thread(this::request_otp).start();
                while (count > 0) {
                    try {
                        Thread.sleep(1000);
                        tv_resend_code.setText("Resend code in " + count + "(s)");
                        count--;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                tv_resend_code.setText("Resend code");
            }));

            tv_back_to_login.setOnClickListener(v -> {
                Intent intent = new Intent(OTPConfirmationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });

            btn_submit_OTP.setOnClickListener(v -> {
                String otp_code = et_code.getText().toString();
                if (otp_code.length() == 6) {
                    new Thread(() -> {
                        try {
                            String password = userSharedPreferences.getString("password", null);
                            String username = userSharedPreferences.getString("username", null);
                            String type = userSharedPreferences.getString("otp_type", null);
                            String urlString = Constants.getHOST(this) + "/auth/verify-otp";

                            Map<String, String> params = new HashMap<>();
                            params.put("otp", otp_code);
                            params.put("token", token);
                            params.put("email", email);
                            params.put("password", password);
                            params.put("username", username);
                            params.put("otp_type", type);

                            JSONObject jsonResponse = NetworkUtils.sendHttpRequest(urlString, "POST", params);

                            if (jsonResponse != null) {
                                String success = jsonResponse.getString("status");
                                if (!success.equals("failed")) {
                                    Intent intent = new Intent(OTPConfirmationActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "OTP code is incorrect", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }
                        } catch (JSONException e) {
                            Log.e("Error JSONException: ", e.getMessage());
                        }
                    }).start();
                }
            });
        }
    }

    private void request_otp() {
        String type = userSharedPreferences.getString("otp_type", null);
        if (email == null) {
            Toast.makeText(this, "Email is not found", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String urlString = Constants.getHOST(this) + "/auth/create-otp";
            Map<String, String> params = new HashMap<>();
            params.put("email_otp", email);
            params.put("otp_type", type);

            JSONObject jsonResponse = NetworkUtils.sendHttpRequest(urlString, "POST", params);
            runOnUiThread(() -> {
                Toast.makeText(this, "Request OTP success with JSON :" + jsonResponse, Toast.LENGTH_SHORT).show();
            });
            if (jsonResponse != null) {
                String success = jsonResponse.getString("status");
                if (success.equals("success")) {
                    token = jsonResponse.getString("token");
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Got Token: " + token, Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Request OTP failed", Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Request OTP failed", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (JSONException e) {
            Log.i("Error JSONException: ", e.getMessage());
        }
    }

}
