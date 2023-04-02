package com.akram.prioritymatrix.ui.calendar;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.TaskRepository;

import java.util.List;

public class CalendarViewModel extends AndroidViewModel {

    private TaskRepository repository;
    private LiveData<List<Task>> userTasks;

    public CalendarViewModel(@NonNull Application application) {
        super(application);

        repository = new TaskRepository(application);

    }

    public LiveData<List<Task>> getUserTasks(String userName) {
        userTasks = repository.getUserTasks(userName);
        return userTasks;
    }

}