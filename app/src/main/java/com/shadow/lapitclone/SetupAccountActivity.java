package com.shadow.lapitclone;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shadow.lapitclone.models.User;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupAccountActivity extends AppCompatActivity {

    private static final String TAG = "SetupAccountActivity";

    private static final int RC_CHOOSE_IMAGE = 999;
    public static final int RC_STORAGE_PERMISSION = 998;
    public static final int RC_CROP_IMAGE = 997;
    //Widgets
    private CircleImageView profileIV;
    private TextInputEditText usernameTIET, statusTIET;
    private Button submitBtn;

    private ProgressDialog progressDialog;

    private boolean imageSelected = false;


    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private StorageReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_account);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Setup Account");
        }

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mRef = FirebaseStorage.getInstance().getReference().child("avatars");

        initComponents();

        profileIV.setOnClickListener(view -> checkPermissions());
        submitBtn.setOnClickListener(view -> updateProfile());
    }

    private void updateProfile() {
        if (emptyFields()) return;

        if (!imageSelected) {
            Toast.makeText(this, "Select a Profile Image", Toast.LENGTH_SHORT).show();
            return;
        }

        //Add Image
        Bitmap bitmap = ((BitmapDrawable) profileIV.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();

        StorageReference profileRef = mRef.child(mAuth.getCurrentUser().getUid() + ".jpg");

        progressDialog.setTitle("Uploading User Avatar");
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        UploadTask profileImageUploaddTask = profileRef.putBytes(bytes);

        profileImageUploaddTask.
                addOnSuccessListener(
                        taskSnapshot -> {
                            String imageName = taskSnapshot.getMetadata().getName();
                            String username = usernameTIET.getText().toString().trim();
                            String status = statusTIET.getText().toString().trim();

                            User user = new User(username, status, imageName);

                            uploadDetails(user);
                        }
                )
                .addOnFailureListener(
                        e -> {
                            Toast.makeText(this, "Image upload failed, try again later", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "updateUI: Upload Image Failed", e);
                            progressDialog.dismiss();
                        }
                );
    }

    private void uploadDetails(User user) {
        progressDialog.setTitle("Updating user details");

        mDb.document("users/" + mAuth.getCurrentUser().getUid())
                .set(user)
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "User profile updated", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "uploadDetails: ", task.getException());
                            }

                            progressDialog.dismiss();
                        }
                );
    }

    private boolean emptyFields() {

        if (usernameTIET.getText().toString().trim().isEmpty()) {
            usernameTIET.requestFocus();
            usernameTIET.setError("Username is required");
            return true;
        } else if (statusTIET.getText().toString().trim().isEmpty()) {
            statusTIET.requestFocus();
            statusTIET.setError("Username is required");
            return true;
        } else return false;
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openChooseImageIntent();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RC_STORAGE_PERMISSION);
            }
        } else {
            openChooseImageIntent();
        }
    }

    private void openChooseImageIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_CHOOSE_IMAGE);
    }

    private void initComponents() {

        profileIV = findViewById(R.id.profile_civ);

        usernameTIET = findViewById(R.id.username_txt);
        statusTIET = findViewById(R.id.status_txt);

        submitBtn = findViewById(R.id.submit_btn);

        progressDialog = new ProgressDialog(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            switch (requestCode) {
                case RC_STORAGE_PERMISSION:
                    openChooseImageIntent();
                    break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            }
        } else {
            Toast.makeText(this, "Sorry! requested permissions denied", Toast.LENGTH_SHORT).show();
        }
    }

    public static String random() {
        Random rand = new Random();

        StringBuilder sb = new StringBuilder();

        int randomLength = rand.nextInt(20);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (rand.nextInt(96) + 32);
            sb.append(tempChar);
        }

        return sb.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if ((resultCode == RESULT_OK) && (data != null)) {
            switch (requestCode) {
                case RC_CHOOSE_IMAGE:
                    imageSelected = true;
                    profileIV.setImageURI(data.getData());
                    Snackbar.make(findViewById(android.R.id.content), "Crop Image", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Tap Here", view -> {
                                imageSelected = false;
                                openCroppingActivity(data.getData());
                            })
                            .show();

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void openCroppingActivity(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");

        intent.putExtra("crop", true);
        intent.putExtra("outputX", 160);
        intent.putExtra("outputY", 160);
        intent.putExtra("aspectX", 160);
        intent.putExtra("aspectY", 160);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);

        PackageManager packageManager = getPackageManager();

        List<ResolveInfo> apps = packageManager.queryIntentActivities(intent, 0);

        if (apps.size() > 0) {
            profileIV.setImageDrawable(getDrawable(R.drawable.ic_account_circle_white_160dp));
            startActivityForResult(intent, RC_CROP_IMAGE);
        } else {
            Toast.makeText(this, "Please installing applications", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }else{
            getUserDetails(user.getUid());
        }
    }

    private void getUserDetails(String uid) {
        mDb.document("users/" + uid)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "getUserDetails: Failed", e);
                        return;
                    }
                    if (documentSnapshot != null) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);

                            if (user != null) {
                                updateUI(user);
                            }
                        } else {
                            Toast.makeText(this, "You have not updated your profile", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void updateUI(User user) {
        usernameTIET.setText(user.getUsername());
        statusTIET.setText(user.getStatus());

        if (user.getImageName() != null) {

            StorageReference profileRef = mRef.child(user.getImageName());
            final long MB = 1024 * 1024;
            profileRef.getBytes(MB)
                    .addOnSuccessListener(
                            bytes -> {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                profileIV.setImageBitmap(bitmap);
                                imageSelected = true;
                            }
                    )
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to get the Image", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "updateUI: Getting Image Failed", e);
                    });
        }
    }
}
