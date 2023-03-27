package com.akram.prioritymatrix.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface ProjectDao {

    @Insert
    void insertProject(Project project);

    @Update
    void updateProject(Project project);

    @Delete
    void deleteProject(Project project);

    @Query("SELECT * FROM project_table")
    LiveData<java.util.List<Project>> getAllProjects();

    @Query("SELECT * FROM project_table WHERE ownerName = :userName")
    LiveData<java.util.List<Project>> getUserProjects(String userName);

    @Query("SELECT * FROM project_table WHERE ownerName = :userName")
    LiveData<List<ProjectWithTasks>> getUserProjectsWithTasks(String userName);
}
