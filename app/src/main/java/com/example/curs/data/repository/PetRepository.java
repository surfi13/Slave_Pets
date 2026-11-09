package com.example.curs.data.repository;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.curs.data.local.AppDatabase;
import com.example.curs.data.local.PetDao;
import com.example.curs.data.local.PetEntity;
import com.google.firebase.auth.FirebaseAuth;
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

    public LiveData<List<PetEntity>> getAllPets() {
        return petDao.getAllPets();
    }

    public void savePet(PetEntity pet) {
        executor.execute(() -> {
            pet.isSynced = false;
            petDao.insertPet(pet);
            syncPetToFirebase(pet);
        });
    }

    public void updatePet(PetEntity pet) {
        executor.execute(() -> {
            pet.isSynced = false;
            petDao.updatePet(pet);
            syncPetToFirebase(pet);
        });
    }

    public void deletePet(PetEntity pet) {
        executor.execute(() -> {
            petDao.deletePet(pet);
            db.collection("pets").document(pet.id).delete()
                    .addOnSuccessListener(aVoid -> Log.d("Delete", "Удалено из Firebase"))
                    .addOnFailureListener(e -> Log.e("Delete", "Ошибка удаления: " + e.getMessage()));
        });
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
                .addOnFailureListener(e -> Log.e("Sync", "Ошибка сохранения: " + e.getMessage()));
    }

    public void syncPendingData() {
        executor.execute(() -> {
            List<PetEntity> unsyncedPets = petDao.getUnsyncedPets();
            for (PetEntity pet : unsyncedPets) {
                syncPetToFirebase(pet);
            }
        });
    }

    public void fetchPetsFromCloud() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        db.collection("pets")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<PetEntity> cloudPets = queryDocumentSnapshots.toObjects(PetEntity.class);
                    executor.execute(() -> {
                        for (PetEntity pet : cloudPets) {
                            pet.isSynced = true;
                            petDao.insertPet(pet);
                        }
                    });
                })
                .addOnFailureListener(e -> Log.e("Sync", "Ошибка скачивания: " + e.getMessage()));
    }
    public PetEntity getPetById(String id) {
        return petDao.getPetById(id);
    }
}