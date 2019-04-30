package com.shadow.lapitclone;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shadow.lapitclone.adapters.SectionsPagerAdapter;
import com.shadow.lapitclone.fragments.ChatsFragment;
import com.shadow.lapitclone.fragments.FriendsFragment;
import com.shadow.lapitclone.fragments.RequestsFragment;
import com.shadow.lapitclone.models.User;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;

    private ViewPager container;
    private TabLayout tabs;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        initComponents();

        if (getSupportActionBar() == null) setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Lapit Clone");

        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new RequestsFragment(), "Requests");
        adapter.addFragment(new ChatsFragment(), "Chats");
        adapter.addFragment(new FriendsFragment(), "Friends");

        container.setAdapter(adapter);

        tabs.setupWithViewPager(container);

    }

    private void initComponents() {
        container = findViewById(R.id.container);
        tabs = findViewById(R.id.tabs);
        toolbar = findViewById(R.id.toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            sendToStartActivity();
        } else {
            checkProfile(user.getUid());
        }
    }

    private void checkProfile(String uid) {
        mDb.document("users/" + uid)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "getUserDetails: Failed", e);
                        return;
                    }
                    if (!documentSnapshot.exists()) {
                        Snackbar.make(findViewById(android.R.id.content), "Crop Image", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Tap Here", view -> openSetupAccountActivity())
                                .show();
                    } else {
                        Toast.makeText(this, "Profile is cool", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_settings:
                openSetupAccountActivity();
                return true;
            case R.id.nav_users:
                startActivity(new Intent(this, TempAllUserActivity.class));
                return true;
            case R.id.nav_logout:
                mAuth.signOut();
                sendToStartActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSetupAccountActivity() {

        Intent intent = new Intent(this, SetupAccountActivity.class);
        startActivity(intent);
    }

    private void sendToStartActivity() {
        startActivity(new Intent(this, StartActivity.class));
        finish();
    }


}
