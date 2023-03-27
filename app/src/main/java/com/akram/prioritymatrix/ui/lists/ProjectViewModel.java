package com.akram.prioritymatrix.ui.lists;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.akram.prioritymatrix.database.Project;
import com.akram.prioritymatrix.database.ProjectRepository;
import com.akram.prioritymatrix.database.ProjectWithTasks;

import java.util.List;

public class ProjectViewModel extends AndroidViewModel {

    private ProjectRepository repository;
    private LiveData<List<Project>> allProjects;
    private LiveData<List<Project>> userProjects;
    private LiveData<List<ProjectWithTasks>> userProjectsWithTasks;

    public ProjectViewModel(@NonNull Application application) {
        super(application);

        repository = new ProjectRepository(application);
        allProjects = repository.getAllProjects();

    }

    public LiveData<List<Project>> getAllProjects() {
        return allProjects;
    }

    public LiveData<List<Project>> getUserProjects(String userName) {
        userProjects = repository.getUserProjects(userName);
        return userProjects;
    }

    public LiveData<List<ProjectWithTasks>> getUserProjectsWithTasks(String userName) {
        userProjectsWithTasks = repository.getUserProjectsWithTasks(userName);
        return userProjectsWithTasks;
    }

}