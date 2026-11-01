package com.example.curs.data.repository;

import android.app.Application;
import android.util.Log;

import com.example.curs.data.local.AppDatabase;
import com.example.curs.data.local.PetDao;
import com.example.curs.data.local.PetEntity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PetRepository {
    private final PetDao petDao;
    private final FirebaseFirestore db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public PetRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        petDao = database.petDao();
        db = FirebaseFirestore.getInstance();
    }

    public void savePet(PetEntity pet) {
        executor.execute(() -> {
            pet.isSynced = false;
            petDao.insertPet(pet);
            syncPetToFirebase(pet);
        });
    }
    public androidx.lifecycle.LiveData<java.util.List<PetEntity>> getAllPets() {
        return petDao.getAllPets();
    }

    private void syncPetToFirebase(PetEntity pet) {
        db.collection("pets").document(pet.id)
                .set(pet)
                .addOnSuccessListener(aVoid -> {
                    executor.execute(() -> {
                        pet.isSynced = true;
                        petDao.updatePet(pet);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("Sync", "Ошибка синхронизации, попробуем позже");
                });
    }

    public void syncPendingData() {
        executor.execute(() -> {
            List<PetEntity> unsyncedPets = petDao.getUnsyncedPets();
            for (PetEntity pet : unsyncedPets) {
                syncPetToFirebase(pet);
            }
        });
    }

    public void fetchFromCloud() {
        db.collection("pets").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<PetEntity> cloudPets = queryDocumentSnapshots.toObjects(PetEntity.class);
            executor.execute(() -> {
                for (PetEntity pet : cloudPets) {
                    pet.isSynced = true;
                    petDao.insertPet(pet);
                }
            });
        });
    }
}
