package com.akram.prioritymatrix.ui.lists;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.TaskRepository;

import java.util.List;

public class ProjectTaskViewModel extends AndroidViewModel {

    private TaskRepository repository;
    private LiveData<List<Task>> projectTasks;

    public ProjectTaskViewModel(@NonNull Application application) {
        super(application);

        repository = new TaskRepository(application);

    }

    public void updateTask(Task task){ repository.updateTask(task);}

    public LiveData<List<Task>> getProjectTasks(int projectId) {
        projectTasks = repository.getProjectTasks(projectId);
        return projectTasks;
    }

}