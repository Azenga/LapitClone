package com.shadow.lapitclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private TextInputEditText emailTIET, pwdTIET, cpwdTIET;
    private Button registerBtn;
    private TextView gotoLoginTV;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create Account");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initComponents();
        mAuth = FirebaseAuth.getInstance();

        gotoLoginTV.setOnClickListener(view -> startActivity(new Intent(this, LoginActivity.class)));
        registerBtn.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        if (hasMadeErrors()) return;

        String email = emailTIET.getText().toString().trim();
        String password = pwdTIET.getText().toString().trim();

        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(this, "Sorry! registration failed try again later", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "registerUser: ", task.getException());
                            }

                            progressDialog.dismiss();
                        }
                );


    }

    private boolean hasMadeErrors() {

        if (emailTIET.getText().toString().trim().isEmpty()) {
            emailTIET.requestFocus();
            emailTIET.setError("Email is required");
            return true;
        } else if (pwdTIET.getText().toString().trim().isEmpty()) {
            pwdTIET.requestFocus();
            pwdTIET.setError("Password is required");
            return true;
        } else if (cpwdTIET.getText().toString().trim().isEmpty()) {
            cpwdTIET.requestFocus();
            cpwdTIET.setError("Confirm password is required");
            return true;
        } else if (!cpwdTIET.getText().toString().trim().equalsIgnoreCase(pwdTIET.getText().toString().trim())) {
            cpwdTIET.requestFocus();
            cpwdTIET.setError("Passwords do not match");
            return true;
        }

        return false;
    }

    private void initComponents() {
        //TextInputEditTexts
        emailTIET = findViewById(R.id.email_txt);
        pwdTIET = findViewById(R.id.pwd_txt);
        cpwdTIET = findViewById(R.id.cpwd_txt);

        //Button
        registerBtn = findViewById(R.id.register_btn);

        //TextView
        gotoLoginTV = findViewById(R.id.goto_login_tv);

        //ProgressDialog
        progressDialog = new ProgressDialog(this);
    }
}
