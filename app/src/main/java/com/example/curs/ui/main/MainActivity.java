package com.example.curs.ui.main;

import com.example.curs.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.curs.data.remote.AuthFacade;
import com.example.curs.data.repository.PetRepository;
import com.example.curs.ui.auth.AuthActivity;
import com.example.curs.data.PetAdapter;
import com.example.curs.ui.AddEditPetActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private AuthFacade authFacade;
    private PetRepository petRepository;
    private PetAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Теперь R будет находиться без проблем
        setContentView(R.layout.activity_main);

        authFacade = new AuthFacade();
        petRepository = new PetRepository(getApplication());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewPets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PetAdapter();
        recyclerView.setAdapter(adapter);

        // Слушаем изменения из Room
        petRepository.getAllPets().observe(this, pets -> {
            adapter.setPets(pets);
        });

        FloatingActionButton fab = findViewById(R.id.fabAddPet);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditPetActivity.class);
            startActivity(intent);
        });

        adapter.setListener(pet -> {
            Toast.makeText(this, "Выбран: " + pet.name, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
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