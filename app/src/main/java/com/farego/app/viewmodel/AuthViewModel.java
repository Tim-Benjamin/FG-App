package com.farego.app.viewmodel;
// ============================================================
// FILE: app/src/main/java/com/farego/app/viewmodel/AuthViewModel.java
// PURPOSE: Drives LoginFragment and RegisterFragment.
// ============================================================

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.farego.app.db.entity.User;
import com.farego.app.repository.AuthRepository;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository repo;

    public final MutableLiveData<User>    loggedInUser = new MutableLiveData<>();
    public final MutableLiveData<String>  errorMsg     = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading    = new MutableLiveData<>(false);

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repo = new AuthRepository(application);
    }

    public void login(String username, String password) {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            errorMsg.setValue("Please enter username and password");
            return;
        }
        isLoading.setValue(true);
        repo.login(username, password, new AuthRepository.AuthCallback() {
            @Override public void onSuccess(User user) {
                isLoading.postValue(false);
                loggedInUser.postValue(user);
            }
            @Override public void onError(String message) {
                isLoading.postValue(false);
                errorMsg.postValue(message);
            }
        });
    }

    public void register(String username, String password,
                         String confirmPassword, String email) {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            errorMsg.setValue("All fields are required");
            return;
        }
        if (!password.equals(confirmPassword)) {
            errorMsg.setValue("Passwords do not match");
            return;
        }
        if (password.length() < 6) {
            errorMsg.setValue("Password must be at least 6 characters");
            return;
        }
        isLoading.setValue(true);
        repo.register(username, password, email, new AuthRepository.AuthCallback() {
            @Override public void onSuccess(User user) {
                isLoading.postValue(false);
                loggedInUser.postValue(user);
            }
            @Override public void onError(String message) {
                isLoading.postValue(false);
                errorMsg.postValue(message);
            }
        });
    }

    public void validateSession(int userId) {
        repo.validateSession(userId, new AuthRepository.AuthCallback() {
            @Override public void onSuccess(User user) { loggedInUser.postValue(user); }
            @Override public void onError(String message) { errorMsg.postValue(message); }
        });
    }
}