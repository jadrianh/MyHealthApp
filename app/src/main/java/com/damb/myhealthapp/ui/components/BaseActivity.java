package com.damb.myhealthapp.ui.components;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.damb.myhealthapp.R;
import com.damb.myhealthapp.ui.views.MainActivity;
import com.damb.myhealthapp.ui.views.SleepLogActivity;
import com.damb.myhealthapp.ui.views.UserProfileActivity;
import com.damb.myhealthapp.ui.views.WaterLogActivity;
import com.damb.myhealthapp.ui.views.WorkoutLogActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected android.widget.ImageView drawerProfileImageView;
    protected android.widget.TextView textViewDrawerUsername;
    protected android.widget.TextView textViewDrawerUserEmail;
    protected android.widget.TextView Username;
    protected SharedPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Método para establecer el contenido específico de la subclase
    protected void setContentViewWithDrawer(@LayoutRes int layoutResID) {
        super.setContentView(R.layout.drawer_layout);
        getLayoutInflater().inflate(layoutResID, findViewById(R.id.content_frame), true);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("MyHealthAppPrefs", MODE_PRIVATE);


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            textViewDrawerUsername = headerView.findViewById(R.id.textView_drawer_username);
            textViewDrawerUserEmail = headerView.findViewById(R.id.textView_drawer_user_email);
            drawerProfileImageView = headerView.findViewById(R.id.imageView_drawer_user_profile);

            // Cargar nombre desde Firestore
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();
                loadDisplayNameFromFirestore(uid);
                textViewDrawerUserEmail.setText(currentUser.getEmail());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileImage();
        // Asegurarse de que el nombre de usuario también se refresque si es necesario
        if (mAuth.getCurrentUser() != null) {
            loadDisplayNameFromFirestore(mAuth.getCurrentUser().getUid());
        }
    }

    private void loadProfileImage() {
        if (drawerProfileImageView != null && prefs != null) {
            int savedResId = prefs.getInt("profile_image_res", R.drawable.ic_account_circle);
            drawerProfileImageView.setImageResource(savedResId);
        }
    }

    protected void loadDisplayNameFromFirestore(String userId) {
        DocumentReference userDocRef = db.collection("users").document(userId);

        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                String name = document != null ? document.getString("displayName") : null;

                String safeName = (name != null && !name.trim().isEmpty()) ? name : "Usuario";
                if (textViewDrawerUsername != null) {
                    textViewDrawerUsername.setText(safeName);
                }
                if (Username != null) {
                    Username.setText(safeName); // Solo si existe esa vista en esta activity
                }
            } else {
                if (textViewDrawerUsername != null) textViewDrawerUsername.setText("Usuario");
                if (Username != null) Username.setText("Usuario");
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Class<?> targetActivity = null;

        if (id == R.id.nav_home) {
            targetActivity = MainActivity.class;
        } else if(id == R.id.nav_workout_log) {
            targetActivity = WorkoutLogActivity.class;
        } else if (id == R.id.nav_profile) {
            targetActivity = UserProfileActivity.class;
        } else if (id == R.id.nav_water_log) {
            targetActivity = WaterLogActivity.class;
        } else if (id == R.id.nav_sleep_log) {
            targetActivity = SleepLogActivity.class;
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            Intent intent = new Intent(this, com.damb.myhealthapp.ui.views.LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        if (targetActivity != null && !this.getClass().equals(targetActivity)) {
            Intent intent = new Intent(this, targetActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
