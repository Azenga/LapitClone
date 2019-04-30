package com.shadow.lapitclone;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shadow.lapitclone.adapters.SectionsPagerAdapter;
import com.shadow.lapitclone.fragments.ChatsFragment;
import com.shadow.lapitclone.fragments.FriendsFragment;
import com.shadow.lapitclone.fragments.RequestsFragment;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private ViewPager container;
    private TabLayout tabs;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

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
        }
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

                Intent intent = new Intent(this, UserProfileActivity.class);
                startActivity(intent);

                return true;
            case R.id.nav_users:
                return true;
            case R.id.nav_logout:
                mAuth.signOut();
                sendToStartActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendToStartActivity() {
        startActivity(new Intent(this, StartActivity.class));
        finish();
    }


}
