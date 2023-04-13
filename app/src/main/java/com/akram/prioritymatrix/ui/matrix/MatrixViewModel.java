package com.akram.prioritymatrix.ui.matrix;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.akram.prioritymatrix.database.Project;
import com.akram.prioritymatrix.database.ProjectRepository;
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.TaskRepository;

import java.util.List;

public class MatrixViewModel extends AndroidViewModel {

    private TaskRepository repository;
    private ProjectRepository projectRepository;

    private LiveData<List<Project>> userProjects;

    private LiveData<List<Task>> userTasks;

    public MatrixViewModel(@NonNull Application application){
        super(application);

        repository = new TaskRepository(application);
        projectRepository = new ProjectRepository(application);

    }

    public void updateTask(Task task){ repository.updateTask(task);}

    public LiveData<List<Project>> getUserProjects(String userName) {
        userProjects = projectRepository.getUserProjects(userName);
        return userProjects;
    }

    public LiveData<List<Task>> getUserTasks(String userName) {
        userTasks = repository.getUserTasks(userName);
        return userTasks;
    }

}