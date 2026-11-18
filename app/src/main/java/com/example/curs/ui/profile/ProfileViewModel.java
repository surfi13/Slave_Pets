package com.example.curs.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.curs.data.remote.AuthFacade;
import com.example.curs.data.local.AppDatabase;
import com.example.curs.data.local.UserDao;
import com.example.curs.data.local.UserEntity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileViewModel extends AndroidViewModel {

    private final AuthFacade authFacade;
    private final FirebaseFirestore db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<UserEntity> user = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSaved = new MutableLiveData<>(false);

    private final Application app;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.app = application;
        authFacade = new AuthFacade();
        db = FirebaseFirestore.getInstance();
        loadUser();
    }

    private UserDao userDao() {
        return AppDatabase.getDatabase(app).userDao();
    }

    public LiveData<UserEntity> getUser() { return user; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsSaved() { return isSaved; }

    public void loadUser() {
        FirebaseUser fbUser = authFacade.getCurrentUser();
        if (fbUser == null) return;

        String uid = fbUser.getUid();
        String email = fbUser.getEmail();

        executor.execute(() -> {
            UserEntity local = userDao().getUserById(uid);
            if (local == null) {
                local = new UserEntity();
                local.id = uid;
                local.email = email != null ? email : "";
                local.name = "";
                local.isSynced = false;
                userDao().insertUser(local);
            }
            user.postValue(local);

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            UserEntity remote = doc.toObject(UserEntity.class);
                            if (remote != null) {
                                remote.isSynced = true;
                                executor.execute(() -> userDao().insertUser(remote));
                                user.postValue(remote);
                            }
                        }
                    });
        });
    }

    public void saveProfile(String newName) {
        UserEntity current = user.getValue();
        if (current == null) return;

        if (newName == null || newName.trim().isEmpty()) {
            errorMessage.setValue("Имя не может быть пустым");
            return;
        }

        isLoading.setValue(true);

        executor.execute(() -> {
            current.name = newName.trim();
            current.isSynced = false;
            userDao().insertUser(current);

            db.collection("users").document(current.id)
                    .set(current)
                    .addOnSuccessListener(aVoid -> {
                        current.isSynced = true;
                        executor.execute(() -> userDao().insertUser(current));
                        isLoading.postValue(false);
                        isSaved.postValue(true);
                    })
                    .addOnFailureListener(e -> {
                        isLoading.postValue(false);
                        errorMessage.postValue("Нет сети. Имя сохранено локально и синхронизируется позже.");
                    });
        });
    }

    public void logout() {
        authFacade.logout();
        // Если в AuthFacade.logout() нет AppDatabase.reset() — раскомментируйте:
        // AppDatabase.reset();
    }
}