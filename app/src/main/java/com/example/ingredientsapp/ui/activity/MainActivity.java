package com.example.ingredientsapp.ui.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.ingredientsapp.R;
import com.example.ingredientsapp.ui.auth.SignInActivity;
import com.example.ingredientsapp.ui.fragments.HistoryFragment;
import com.example.ingredientsapp.ui.fragments.HomeFragment;
import com.example.ingredientsapp.ui.fragments.ListsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;
import com.google.firebase.firestore.Source;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        replaceFragment(new HomeFragment());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                        PersistentCacheSettings
                                .newBuilder()
                                .build())
                .build();
        db.setFirestoreSettings(settings);

        NavigationView navigationView = findViewById(R.id.navigationView);
        Menu menu = navigationView.getMenu();
        MenuItem signIn = menu.findItem(R.id.signIn);
        MenuItem deleteAccount = menu.findItem(R.id.deleteAccount);

        updateMenuItems(signIn, deleteAccount);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.history) {
                replaceFragment(new HistoryFragment());
                return true;
            } else if (itemId == R.id.lists) {
                replaceFragment(new ListsFragment());
                return true;
            }
            return false;
        });

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.signIn) {
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser != null && !currentUser.isAnonymous()) {
                    auth.signOut();
                    Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                    updateMenuItems(signIn, deleteAccount);
                } else {
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                }
                return true;
            } else if (itemId == R.id.clearHistory) {
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser != null) {
                    clearHistory(currentUser.getUid());
                } else {
                    auth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser newUser = auth.getCurrentUser();
                                if (newUser != null) clearHistory(newUser.getUid());
                            }
                        }
                    });
                }
            } else if (itemId == R.id.deleteAccount) {
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser != null) {
                    currentUser.delete();
                    updateMenuItems(signIn, deleteAccount);
                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                }
            }
            drawerLayout.closeDrawers();
            return false;
        });
    }

    private void updateMenuItems(MenuItem signIn, MenuItem deleteAccount) {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null && !currentUser.isAnonymous()) {
            signIn.setTitle("Sign Out");
            signIn.setIcon(R.drawable.baseline_logout_24);
            deleteAccount.setTitle("Delete account");
        } else {
            signIn.setTitle("Sign In");
            signIn.setIcon(R.drawable.baseline_login_24);
            deleteAccount.setTitle("");
        }
    }

    public void clearHistory(String userId) {
        db.collection("users")
                .document(userId)
                .collection("history")
                .get(Source.CACHE)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    if (getCurrentFragment() instanceof HistoryFragment) {
                        replaceFragment(new HistoryFragment());
                    }
                    Toast.makeText(MainActivity.this, "History cleared", Toast.LENGTH_SHORT).show();
                });
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.frameLayout);
    }
}
