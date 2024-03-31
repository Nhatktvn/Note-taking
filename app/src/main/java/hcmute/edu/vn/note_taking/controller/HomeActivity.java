package hcmute.edu.vn.note_taking.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import hcmute.edu.vn.note_taking.R;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_exit, null);
        builder.setView(view);

        Button btnYes = view.findViewById(R.id.btn_yes);
        Button btnNo = view.findViewById(R.id.btn_no);

        AlertDialog dialog = builder.create();

        btnYes.setOnClickListener(v -> {
            onDestroy();
            super.onBackPressed();
        });

        btnNo.setOnClickListener(v -> {
            dialog.dismiss();
            super.onBackPressed();
        });
        dialog.show();
    }
}
