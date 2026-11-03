package com.example.curs.ui.auth;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.curs.data.repository.AuthRepository;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAuthSuccessful = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isRegisterMode = new MutableLiveData<>(false);

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsAuthSuccessful() { return isAuthSuccessful; }
    public LiveData<Boolean> getIsRegisterMode() { return isRegisterMode; }

    public void toggleMode() {
        Boolean currentMode = isRegisterMode.getValue();
        isRegisterMode.setValue(currentMode == null || !currentMode);
    }

    public void authenticate(String name, String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Заполните все поля");
            return;
        }

        isLoading.postValue(true);
        errorMessage.postValue(null);

        AuthRepository.AuthCallback callback = new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                // Принудительно выполняем в главном потоке (UI)
                new Handler(Looper.getMainLooper()).post(() -> {
                    isLoading.setValue(false);
                    isAuthSuccessful.setValue(true);
                });
            }

            @Override
            public void onFailure(String message) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(message);
                });
            }
        };

        if (Boolean.TRUE.equals(isRegisterMode.getValue())) {
            authRepository.register(name, email, password, callback);
        } else {
            authRepository.login(email, password, callback);
        }
    }
}