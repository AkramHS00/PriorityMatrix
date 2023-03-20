package com.akram.prioritymatrix.user;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.akram.prioritymatrix.database.User;
import com.akram.prioritymatrix.database.UserRepository;

public class SignUpViewModel extends AndroidViewModel {

    private UserRepository repository;


    public SignUpViewModel(@NonNull Application application) {
        super(application);

        repository = new UserRepository(application);
    }

    public void insert(User user){
        repository.insert(user);
    }

    public User getUser(String userName, String password){
        return repository.getUser(userName, password);
    }

    public boolean isTaken(String userName){
        return repository.isTaken(userName);
    }

}