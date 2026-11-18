package com.example.curs.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.example.curs.ui.BaseActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.example.curs.R;
import com.example.curs.data.remote.AuthFacade;
import com.example.curs.ui.AddEditPetActivity;
import com.example.curs.ui.auth.AuthActivity;
import com.example.curs.ui.pets.PetsFragment;
import com.example.curs.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity {

    private AuthFacade authFacade;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authFacade = new AuthFacade();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchTo(new PetsFragment(), "Мои питомцы");
                return true;
            } else if (id == R.id.nav_add) {
                openAddPetScreen();
                return true;
            } else if (id == R.id.nav_profile) {
                switchTo(new ProfileFragment(), "Профиль");
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void switchTo(Fragment fragment, String title) {
        toolbar.setTitle(title);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private void openAddPetScreen() {
        Intent intent = new Intent(this, AddEditPetActivity.class);
        startActivity(intent);
    }
}