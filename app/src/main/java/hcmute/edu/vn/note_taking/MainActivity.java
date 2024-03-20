package hcmute.edu.vn.note_taking;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
//        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView forgotPassTextView = findViewById(R.id.forgotPasswordTextView);
//        forgotPassTextView.setPaintFlags(forgotPassTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
    }
}