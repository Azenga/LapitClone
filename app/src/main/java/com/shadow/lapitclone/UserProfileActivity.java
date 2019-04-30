package com.shadow.lapitclone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    private ImageButton editProfileIB;
    private CircleImageView profileCIV;
    private TextView usernameTV, statusTV;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private StorageReference mRef;

    //private DatabaseReference mDbRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("User Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mRef = FirebaseStorage.getInstance().getReference().child("avatars");

        initComponents();

        editProfileIB.setOnClickListener(view -> startActivity(new Intent(this, SetupAccountActivity.class)));
    }

    private void initComponents() {
        editProfileIB = findViewById(R.id.edit_profile_ib);

        profileCIV = findViewById(R.id.profile_civ);

        usernameTV = findViewById(R.id.username_tv);
        statusTV = findViewById(R.id.status_tv);
    }

    private void updateProfile(User user) {
        usernameTV.setText(user.getUsername());
        statusTV.setText(user.getStatus());

        if (user.getImageName() != null) {

            StorageReference profileRef = mRef.child(user.getImageName());
            final long MB = 1024 * 1024;
            profileRef.getBytes(MB)
                    .addOnSuccessListener(
                            bytes -> {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                profileCIV.setImageBitmap(bitmap);
                            }
                    )
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to get the Image", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "updateProfile: Getting Image Failed", e);
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, StartActivity.class));
            finish();
        } else {
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
                                updateProfile(user);
                            }
                        } else {
                            Toast.makeText(this, "You have not updated your profile", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
