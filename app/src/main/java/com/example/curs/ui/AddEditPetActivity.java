package com.example.curs.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.curs.R;
import com.example.curs.data.repository.PetRepository;
import com.example.curs.data.local.PetEntity;
import com.example.curs.data.repository.StorageRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.UUID;

public class AddEditPetActivity extends AppCompatActivity {

    private TextInputEditText etName, etSpecies, etBreed;
    private ImageView ivAddPhoto;
    private ProgressBar progressBar;
    private Uri selectedImageUri;
    private PetRepository petRepository;
    private StorageRepository storageRepository;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivAddPhoto.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);

        petRepository = new PetRepository(getApplication());
        storageRepository = new StorageRepository();

        etName = findViewById(R.id.etName);
        etSpecies = findViewById(R.id.etSpecies);
        etBreed = findViewById(R.id.etBreed);
        ivAddPhoto = findViewById(R.id.ivAddPhoto);
        progressBar = findViewById(R.id.progressBarAdd);
        MaterialButton btnPickPhoto = findViewById(R.id.btnPickPhoto);
        MaterialButton btnSave = findViewById(R.id.btnSavePet);

        btnPickPhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> savePet());
    }

    private void savePet() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String species = etSpecies.getText() != null ? etSpecies.getText().toString().trim() : "";
        String breed = etBreed.getText() != null ? etBreed.getText().toString().trim() : "";

        if (name.isEmpty() || species.isEmpty()) {
            Toast.makeText(this, "Заполните кличку и вид", Toast.LENGTH_SHORT).show();
            return;
        }

        String petId = UUID.randomUUID().toString();
        PetEntity newPet = new PetEntity();
        newPet.id = petId;
        newPet.name = name;
        newPet.species = species;
        newPet.breed = breed;

        if (selectedImageUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            storageRepository.uploadPetPhoto(petId, selectedImageUri, new StorageRepository.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    newPet.photoUrl = downloadUrl;
                    saveToLocal(newPet);
                }

                @Override
                public void onFailure(String error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddEditPetActivity.this, "Ошибка фото: " + error, Toast.LENGTH_SHORT).show();
                    // Сохраняем без фото, если загрузка не удалась
                    saveToLocal(newPet);
                }
            });
        } else {
            saveToLocal(newPet);
        }
    }

    private void saveToLocal(PetEntity pet) {
        petRepository.savePet(pet);
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(AddEditPetActivity.this, "Питомец сохранен", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
