package com.example.curs.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.example.curs.R;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curs.data.local.PetEntity;
import com.example.curs.data.remote.AuthFacade;
import com.example.curs.data.repository.PetRepository;
import com.example.curs.ui.auth.AuthActivity;
import com.example.curs.ui.main.PetAdapter;
import com.example.curs.ui.AddEditPetActivity;
import com.example.curs.ui.settings.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private AuthFacade authFacade;
    private PetRepository petRepository;
    private PetAdapter adapter;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authFacade = new AuthFacade();
        petRepository = new PetRepository(getApplication());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewPets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PetAdapter();
        recyclerView.setAdapter(adapter);

        petRepository.getAllPets().observe(this, pets -> {
            adapter.setPets(pets);
        });

        petRepository.fetchPetsFromCloud();


        // НАСТРОЙКА НИЖНЕГО МЕНЮ
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Ничего не делаем, мы уже на главной
                return true;
            } else if (itemId == R.id.nav_add) {
                openAddPetScreen();
                return true;
            } else if (itemId == R.id.nav_profile) {
                Toast.makeText(this, "Экран профиля в разработке", Toast.LENGTH_SHORT).show();
                // В будущем: startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        adapter.setListener(new PetAdapter.OnPetClickListener() {
            @Override
            public void onPetClick(PetEntity pet) {
                Intent intent = new Intent(MainActivity.this, AddEditPetActivity.class);
                intent.putExtra("PET_ID", pet.id);
                startActivity(intent);
            }

            @Override
            public void onPetDelete(PetEntity pet) {
                petRepository.deletePet(pet);
                Toast.makeText(MainActivity.this, "Питомец удален", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openAddPetScreen() {
        Intent intent = new Intent(MainActivity.this, AddEditPetActivity.class);
        startActivity(intent);
    }

    // Верхнее меню (настройки и выход)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performLogout() {
        authFacade.logout();
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}