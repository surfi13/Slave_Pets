package com.example.curs.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.curs.R;
import com.example.curs.data.local.PetEntity;
import com.example.curs.data.repository.PetRepository;
import com.example.curs.data.repository.StorageRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.UUID;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class AddEditPetActivity extends AppCompatActivity {

    private TextInputEditText etName, etBreed;
    private AutoCompleteTextView etSpecies;
    private ImageView ivAddPhoto;
    private ProgressBar progressBar;
    private Uri selectedImageUri;

    private PetRepository petRepository;
    private StorageRepository storageRepository;

    private String petId = null;
    private PetEntity existingPet = null;
    private String existingPhotoUrl = null;

    private final String[] speciesList = {"Кошка", "Собака", "Попугай", "Грызун", "Рептилия", "Другое"};

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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, speciesList);
        etSpecies.setAdapter(adapter);

        petId = getIntent().getStringExtra("PET_ID");
        if (petId != null) {
            loadPetData();
        }

        btnPickPhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> savePet());
    }

    private void loadPetData() {
        new Thread(() -> {
            existingPet = petRepository.getPetById(petId);
            runOnUiThread(() -> {
                if (existingPet != null) {
                    etName.setText(existingPet.name);
                    etSpecies.setText(existingPet.species, false);
                    etBreed.setText(existingPet.breed);
                    existingPhotoUrl = existingPet.photoUrl;

                    if (existingPhotoUrl != null && !existingPhotoUrl.isEmpty()) {
                        Glide.with(this)
                                .load(existingPhotoUrl)
                                .transform(new CropCircleTransformation()) // Круглое фото
                                .into(ivAddPhoto);
                    }
                }
            });
        }).start();
    }

    private void savePet() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String species = etSpecies.getText() != null ? etSpecies.getText().toString().trim() : "";
        String breed = etBreed.getText() != null ? etBreed.getText().toString().trim() : "";

        if (name.isEmpty() || species.isEmpty()) {
            Toast.makeText(this, "Заполните кличку и вид", Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerId = FirebaseAuth.getInstance().getUid();
        if (ownerId == null) {
            ownerId = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).getString("last_user_id", null);
        }
        if (ownerId == null) {
            Toast.makeText(this, "Ошибка авторизации. Выйдите и войдите заново.", Toast.LENGTH_LONG).show();
            return;
        }

        String currentPetId = (petId != null) ? petId : UUID.randomUUID().toString();

        PetEntity newPet = new PetEntity();
        newPet.id = currentPetId;
        newPet.ownerId = ownerId;
        newPet.name = name;
        newPet.species = species;
        newPet.breed = breed;

        if (selectedImageUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            storageRepository.uploadPetPhoto(this, selectedImageUri, new StorageRepository.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    newPet.photoUrl = downloadUrl;
                    saveToLocal(newPet);
                }

                @Override
                public void onFailure(String error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddEditPetActivity.this, "Ошибка фото: " + error, Toast.LENGTH_SHORT).show();
                    newPet.photoUrl = existingPhotoUrl;
                    saveToLocal(newPet);
                }
            });
        } else {
            newPet.photoUrl = existingPhotoUrl;
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