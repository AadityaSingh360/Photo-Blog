package com.example.photoblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private Toolbar toolbar;
    private Toolbar supportActionBar;
    private FloatingActionButton addPost_btn;
    private BottomNavigationView main_bottom;
    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        addPost_btn = (FloatingActionButton) findViewById(R.id.addPost_btn);

        addPost_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addPostIntent = new Intent(MainActivity.this, PostBlogActivity.class);
                startActivity(addPostIntent);
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Photo Blog");

        homeFragment = new HomeFragment();
        accountFragment = new AccountFragment();
        notificationFragment = new NotificationFragment();
        replaceFragment(homeFragment);

        main_bottom = findViewById(R.id.bottomNavigationView);

        if (mAuth.getCurrentUser() != null) {
            main_bottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.bottomHome:
                            replaceFragment(homeFragment);
                            return true;
                        case R.id.bottomNotification:
                            replaceFragment(notificationFragment);
                            return true;
                        case R.id.bottomAccount:
                            replaceFragment(accountFragment);
                            return true;
                        default:
                            return false;
                    }
                }
            });

        }
    }
    @Override
    public void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()==null)
        {
            Intent intent=new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }else
        {

            FirebaseUser currentUser = mAuth.getCurrentUser();
            String userid=currentUser.getUid();
            firestore.collection("Users").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            Intent intent=new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(intent);
                        }
                    }
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.logout:
                logout();
                return true;
            case R.id.account_settings :
                Intent intent=new Intent(MainActivity.this,SetupActivity.class);
                startActivity(intent);
                return true;
                default:
                    return false;
        }
    }

    private void logout() {
        mAuth.signOut();
        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void replaceFragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();
    }

}
