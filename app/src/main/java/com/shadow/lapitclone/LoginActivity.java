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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;

    //Widgets
    private TextInputEditText emailTIET, pwdTIET;
    private Button loginBtn;
    private TextView gotoRegisterTV;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Sign In");
        mAuth = FirebaseAuth.getInstance();

        initComponents();

        gotoRegisterTV.setOnClickListener(view -> startActivity(new Intent(this, RegisterActivity.class)));
        loginBtn.setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        if (hasMadeErrors()) return;

        String email = emailTIET.getText().toString().trim();
        String password = pwdTIET.getText().toString().trim();

        progressDialog.setTitle("Signing In");
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(this, "Sorry! login failed try again later", Toast.LENGTH_SHORT).show();
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
        }

        return false;
    }

    private void initComponents() {
        emailTIET = findViewById(R.id.email_txt);
        pwdTIET = findViewById(R.id.pwd_txt);
        loginBtn = findViewById(R.id.login_btn);
        gotoRegisterTV = findViewById(R.id.goto_register_tv);

        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
