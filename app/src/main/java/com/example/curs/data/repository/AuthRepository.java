package com.example.curs.data.repository;


import android.app.Application;

import com.example.curs.data.local.AppDatabase;
import com.example.curs.data.local.UserDao;
import com.example.curs.data.local.UserEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepository {
    private final UserDao userDao;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AuthRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        userDao = database.userDao();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public interface AuthCallback {
        void onSuccess();
        void onFailure(String message);
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

                        executor.execute(() -> {
                            userDao.insertUser(newUser);
                            db.collection("users").document(uid).set(newUser);
                        });
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Ошибка регистрации: " + task.getException().getMessage());
                    }
                });
    }

    public void login(String email, String password, AuthCallback callback) {
        // Сначала пытаемся найти пользователя в локальной базе данных (Room)
        executor.execute(() -> {
            UserEntity localUser = userDao.getUserByEmail(email);

            // Если пользователь найден локально и пароль совпадает
            if (localUser != null && localUser.passwordHash.equals(password)) {
                // Успешный вход офлайн (или онлайн, но через кэш)
                callback.onSuccess();

                // Опционально: если есть интернет, можно в фоне обновить токен Firebase
                // mAuth.signInWithEmailAndPassword(email, password);
            } else {
                // Если локально не нашли, пробуем через Firebase (только если есть интернет)
                // ВАЖНО: Оборачиваем в try-catch, чтобы поймать ошибку отсутствия сети
                try {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult().getUser() != null) {
                                    String uid = task.getResult().getUser().getUid();
                                    db.collection("users").document(uid).get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                UserEntity cloudUser = documentSnapshot.toObject(UserEntity.class);
                                                if (cloudUser != null) {
                                                    cloudUser.passwordHash = password;
                                                    cloudUser.isSynced = true;
                                                    executor.execute(() -> userDao.insertUser(cloudUser));
                                                    callback.onSuccess();
                                                }
                                            });
                                } else {
                                    callback.onFailure("Неверный email или пароль");
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Если произошла ошибка сети, сообщаем пользователю
                                if (e instanceof com.google.firebase.FirebaseNetworkException) {
                                    callback.onFailure("Нет подключения к интернету. Войдите с аккаунтом, который уже использовался на этом устройстве.");
                                } else {
                                    callback.onFailure("Ошибка входа: " + e.getMessage());
                                }
                            });
                } catch (Exception e) {
                    // Дополнительная страховка от краша
                    callback.onFailure("Ошибка сети. Проверьте подключение.");
                }
            }
        });
    }
}
