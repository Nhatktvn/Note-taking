package hcmute.edu.vn.note_taking.controller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import hcmute.edu.vn.note_taking.R;
import hcmute.edu.vn.note_taking.sqlite.NoteTakingOpenHelper;
import hcmute.edu.vn.note_taking.utils.Constants;

public class LoginActivity extends AppCompatActivity {

    EditText et_password;
    EditText et_username;
    Button btn_login;
    Button btn_register;
    TextView tv_forgot_password;

    NoteTakingOpenHelper openHelper;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        tv_forgot_password = findViewById(R.id.tv_forgot_password);

        openHelper = new NoteTakingOpenHelper(this);
        et_username.setHint(Constants.getHOST(this));

        btn_login.setOnClickListener(v -> {
            String username = et_username.getText().toString();
            String password = et_password.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                new Thread(() -> {
                    try {
                        URL url = new URL(Constants.getHOST(this) + "/auth/login");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        String jsonInputString = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setDoInput(true);
                        conn.setDoOutput(true);

                        try (DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream())) {
                            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                            outputStream.write(input, 0, input.length);
                        }


                        Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                        Log.i("MSG", conn.getResponseMessage());

                        if (conn.getResponseCode() == 200) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line).append("\n");
                            }
                            br.close();

                            JSONObject jsonObject = new JSONObject(sb.toString());
                            String success = jsonObject.getString("status");

                            Log.i("JSONResponse", sb.toString());

                            if (!success.equals("failed")) {
                                runOnUiThread(() -> {
                                    sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                                    SharedPreferences.Editor editor;
                                    editor = sharedPreferences.edit();
                                    editor.putString("username", username);
                                    editor.apply();

                                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show());
                            }
                        } else {
                            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Server error", Toast.LENGTH_SHORT).show());
                        }

                        conn.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }
}
