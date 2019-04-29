package com.shadow.lapitclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button accountsBtn = findViewById(R.id.go_to_login_btn);
        accountsBtn.setOnClickListener(btn -> startActivity(new Intent(this, LoginActivity.class)));
    }
}
