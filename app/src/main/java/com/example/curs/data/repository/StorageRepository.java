package com.example.curs.data.repository;

import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class StorageRepository {
    private final FirebaseStorage storage;

    public StorageRepository() {
        this.storage = FirebaseStorage.getInstance();
    }

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(String error);
    }

    public void uploadPetPhoto(String petId, Uri imageUri, UploadCallback callback) {
        if (imageUri == null) {
            callback.onFailure("Фото не выбрано");
            return;
        }

        // Путь в хранилище: pets/{petId}/avatar.jpg
        StorageReference fileRef = storage.getReference()
                .child("pets")
                .child(petId)
                .child("avatar.jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Если загрузка успешна, получаем прямую ссылку на файл
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        callback.onSuccess(uri.toString());
                    }).addOnFailureListener(e -> {
                        callback.onFailure("Ошибка получения ссылки: " + e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Ошибка загрузки: " + e.getMessage());
                });
    }
}
