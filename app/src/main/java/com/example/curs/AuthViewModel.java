package com.example.curs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AuthViewModel extends ViewModel {

    private final AuthFacade authFacade;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAuthSuccessful = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isRegisterMode = new MutableLiveData<>(false);

    public AuthViewModel() {
        authFacade = new AuthFacade();
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsAuthSuccessful() { return isAuthSuccessful; }
    public LiveData<Boolean> getIsRegisterMode() { return isRegisterMode; }

    public void toggleMode() {
        Boolean currentMode = isRegisterMode.getValue();
        isRegisterMode.setValue(currentMode == null || !currentMode);
    }

    public void authenticate(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Заполните все поля");
            return;
        }
        if (password.length() < 6) {
            errorMessage.setValue("Пароль должен содержать минимум 6 символов");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        if (Boolean.TRUE.equals(isRegisterMode.getValue())) {
            authFacade.register(email, password, new AuthFacade.AuthCallback() {
                @Override
                public void onSuccess() {
                    isLoading.setValue(false);
                    isAuthSuccessful.setValue(true);
                }

                @Override
                public void onFailure(String message) {
                    isLoading.setValue(false);
                    errorMessage.setValue(message);
                }
            });
        } else {
            authFacade.login(email, password, new AuthFacade.AuthCallback() {
                @Override
                public void onSuccess() {
                    isLoading.setValue(false);
                    isAuthSuccessful.setValue(true);
                }

                @Override
                public void onFailure(String message) {
                    isLoading.setValue(false);
                    errorMessage.setValue(message);
                }
            });
        }
    }

    public boolean isUserLoggedIn() {
        return authFacade.getCurrentUser() != null;
    }
}
