package com.akram.prioritymatrix.ui.tasks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.akram.prioritymatrix.database.Project;
import com.akram.prioritymatrix.database.ProjectRepository;
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.TaskRepository;

import java.util.List;

public class AddTaskViewModel extends AndroidViewModel {

    private TaskRepository repository;
    private ProjectRepository projectRepository;

    private LiveData<List<Project>> userProjects;

    public AddTaskViewModel(@NonNull Application application){
        super(application);

        repository = new TaskRepository(application);
        projectRepository = new ProjectRepository(application);
    }

    public void insertTask(Task task){ repository.insertTask(task);}

    public LiveData<List<Project>> getUserProjects(String userName) {
        userProjects = projectRepository.getUserProjects(userName);
        return userProjects;
    }

}