package hcmute.edu.vn.note_taking.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import hcmute.edu.vn.note_taking.R;
import hcmute.edu.vn.note_taking.sqlite.NoteTakingOpenHelper;
import hcmute.edu.vn.note_taking.ui.login.LoginFragment;
import hcmute.edu.vn.note_taking.utils.Constants;
import hcmute.edu.vn.note_taking.utils.NetworkUtils;

public class LoginActivity extends AppCompatActivity {

    LinearLayout rl_ic_about_us;
    LinearLayout rl_ic_contact_us;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        rl_ic_about_us = findViewById(R.id.rl_ic_about_us);
        rl_ic_contact_us = findViewById(R.id.rl_ic_contact_us);

        rl_ic_about_us.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, AboutUsActivity.class);
            startActivity(intent);
        });

        rl_ic_contact_us.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ContactUsActivity.class);
            startActivity(intent);
        });

        Fragment loginFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_login, loginFragment).commit();
    }
}
