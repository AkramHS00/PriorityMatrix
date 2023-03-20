package com.akram.prioritymatrix.ui.lists;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.akram.prioritymatrix.database.Project;
import com.akram.prioritymatrix.database.ProjectRepository;
import com.akram.prioritymatrix.database.Task;

import java.util.List;

public class AddProjectViewModel extends AndroidViewModel {

    private ProjectRepository repository;

    public AddProjectViewModel(@NonNull Application application) {
        super(application);

        repository = new ProjectRepository(application);

    }

    public void insertProject(Project project){ repository.insertProject(project);}

    public void updateProject(Project project){ repository.updateProject(project);}


}