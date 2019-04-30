package com.shadow.lapitclone;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class TempAllUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_all_user);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("All Users");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
