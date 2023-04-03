package com.akram.prioritymatrix.ui.tasks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.TaskRepository;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private TaskRepository repository;
    private LiveData<List<Task>> allTasks;
    private LiveData<List<Task>> userTasks;
    private LiveData<List<Task>> userOrderedTasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);

        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();

    }

    public void updateTask(Task task){ repository.updateTask(task);}

    public LiveData<List<Task>> getAllListings() {
        return allTasks;
    }

    public LiveData<List<Task>> getUserTasks(String userName) {
        userTasks = repository.getUserTasks(userName);
        return userTasks;
    }

    public LiveData<List<Task>> getOutstandingUserTasks(String userName) {
        userTasks = repository.getOutstandingUserTasks(userName);
        return userTasks;
    }




}