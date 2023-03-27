package com.akram.prioritymatrix.database;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class ProjectWithTasks {
    @Embedded public Project project;
    @Relation(
            parentColumn = "id",
            entityColumn = "projectId",
            entity = Task.class
    )
    public List<Task> tasks;

    public Project getProject() {
        return project;
    }

    public List<Task> getTasks() {
        return tasks;
    }
}
