package com.akram.prioritymatrix.ui.matrix;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.TaskRepository;

public class MatrixViewModel extends AndroidViewModel {

    private TaskRepository repository;

    public MatrixViewModel(@NonNull Application application){
        super(application);

        repository = new TaskRepository(application);

    }

    public void updateTask(Task task){ repository.updateTask(task);}

}