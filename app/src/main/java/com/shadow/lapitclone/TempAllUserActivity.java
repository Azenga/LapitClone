package com.shadow.lapitclone;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shadow.lapitclone.models.User;

import de.hdodenhof.circleimageview.CircleImageView;

public class TempAllUserActivity extends AppCompatActivity {
    private static final String TAG = "TempAllUserActivity";

    private RecyclerView usersRV;


    private FirebaseFirestore mDb;
    private FirestoreRecyclerAdapter adapter;
    private StorageReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_all_user);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("All Users");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mDb = FirebaseFirestore.getInstance();
        mRef = FirebaseStorage.getInstance().getReference().child("avatars");

        initComponents();
        getAllUsers();

    }

    private void getAllUsers() {
        Query query = mDb.collection("users").orderBy("username").limit(10);

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<User, UserHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserHolder holder, int position, @NonNull User model) {
                holder.usernameTV.setText(model.getUsername());
                holder.statusTV.setText(model.getStatus());

                //Getting the Image

                if (model.getImageName() != null) {
                    StorageReference profileRef = mRef.child(model.getImageName());
                    final long MB = 1024 * 1024;
                    profileRef.getBytes(MB)
                            .addOnSuccessListener(
                                    bytes -> {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        holder.profileCIV.setImageBitmap(bitmap);
                                    }
                            )
                            .addOnFailureListener(
                                    e -> {
                                        Toast.makeText(TempAllUserActivity.this, "Failed to get an Image", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "onBindViewHolder: Getting image failed ", e);
                                    }
                            );
                }
            }

            @NonNull
            @Override
            public UserHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(TempAllUserActivity.this).inflate(R.layout.single_user_layout, viewGroup, false);

                return new UserHolder(view);
            }
        };

        usersRV.setAdapter(adapter);
    }

    private void initComponents() {
        usersRV = findViewById(R.id.users_rv);
        usersRV.setLayoutManager(new LinearLayoutManager(this));
        usersRV.setHasFixedSize(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();

    }


    public static class UserHolder extends RecyclerView.ViewHolder {
        View mView;
        CircleImageView profileCIV;
        TextView usernameTV, statusTV;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            profileCIV = itemView.findViewById(R.id.profile_civ);
            usernameTV = itemView.findViewById(R.id.username_tv);
            statusTV = itemView.findViewById(R.id.status_tv);
        }
    }
}
