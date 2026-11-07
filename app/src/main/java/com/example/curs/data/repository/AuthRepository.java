package com.example.curs.data.repository;

import android.util.Log;
import com.example.curs.data.local.AppDatabase;
import com.example.curs.data.local.PetDao;
import com.example.curs.data.local.PetEntity;
import com.example.curs.data.local.UserDao;
import com.example.curs.data.local.UserEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepository {
    private final UserDao userDao;
    private final PetDao petDao;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AuthRepository(android.app.Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        userDao = database.userDao();
        petDao = database.petDao();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public interface AuthCallback {
        void onSuccess();
        void onFailure(String message);
    }

    public void login(String email, String password, AuthCallback callback) {
        executor.execute(() -> {
            // 1. ОФЛАЙН ВХОД
            UserEntity localUser = userDao.getUserByEmail(email);
            if (localUser != null && localUser.passwordHash.equals(password)) {

                // ВАЖНО: Пытаемся восстановить сессию Firebase в фоне, чтобы работал getUid()
                try {
                    mAuth.signInWithEmailAndPassword(email, password);
                } catch (Exception e) {
                    Log.e("AUTH", "Нет сети для входа в Firebase: " + e.getMessage());
                }

                callback.onSuccess();
                syncUserDataFromCloud(localUser.id);
                return;
            }

            // 2. ОНЛАЙН ВХОД
            try {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult().getUser() != null) {
                                String uid = task.getResult().getUser().getUid();

                                callback.onSuccess();

                                db.collection("users").document(uid).get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            UserEntity cloudUser = documentSnapshot.toObject(UserEntity.class);
                                            if (cloudUser != null) {
                                                cloudUser.passwordHash = password;
                                                cloudUser.isSynced = true;
                                                executor.execute(() -> userDao.insertUser(cloudUser));
                                                syncUserDataFromCloud(uid);
                                            }
                                        });
                            } else {
                                callback.onFailure("Неверный email или пароль");
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (e instanceof com.google.firebase.FirebaseNetworkException) {
                                callback.onFailure("Нет интернета. Войдите с аккаунтом, который уже использовался.");
                            } else {
                                callback.onFailure("Ошибка входа: " + e.getMessage());
                            }
                        });
            } catch (Exception e) {
                callback.onFailure("Критическая ошибка: " + e.getMessage());
            }
        });
    }

    public void syncUserDataFromCloud(String userId) {
        if (userId == null) return;
        db.collection("pets").whereEqualTo("ownerId", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<PetEntity> cloudPets = queryDocumentSnapshots.toObjects(PetEntity.class);
                    executor.execute(() -> {
                        for (PetEntity pet : cloudPets) {
                            pet.isSynced = true;
                            petDao.insertPet(pet);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("Sync", "Ошибка синхронизации (возможно нет сети): " + e.getMessage());
                });
    }

    public void register(String name, String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String uid = task.getResult().getUser().getUid();
                        UserEntity newUser = new UserEntity();
                        newUser.id = uid;
                        newUser.name = name;
                        newUser.email = email;
                        newUser.passwordHash = password;
                        newUser.isSynced = true;

                        executor.execute(() -> userDao.insertUser(newUser));
                        db.collection("users").document(uid).set(newUser);

                        callback.onSuccess();
                    } else {
                        callback.onFailure("Ошибка регистрации: " +
                                (task.getException() != null ? task.getException().getMessage() : "Неизвестно"));
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Ошибка сети: " + e.getMessage());
                });
    }
}