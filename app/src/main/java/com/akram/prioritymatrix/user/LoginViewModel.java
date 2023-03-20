package com.akram.prioritymatrix.user;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import com.akram.prioritymatrix.database.User;
import com.akram.prioritymatrix.database.UserRepository;

public class LoginViewModel extends AndroidViewModel {

    private UserRepository repository;
    private boolean checkLogin;


    public LoginViewModel(@NonNull Application application) {
        super(application);

        repository = new UserRepository(application);
    }

    public void insert(User user){
        repository.insert(user);
    }

    public boolean checkLogin(String userName, String password){
        return repository.checkLogin(userName, password);
    }

    public User getUser(String userName, String password){
        return repository.getUser(userName, password);
    }
}