package hcmute.edu.vn.note_taking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }
        public void onClick(View v) {
            Intent intentRegister = new Intent(Login.this, Register.class);
            startActivity(intentRegister);
        }
}
