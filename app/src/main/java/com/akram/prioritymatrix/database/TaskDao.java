package com.akram.prioritymatrix.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    long insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("SELECT * FROM task_table")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM task_table WHERE ownerName = :userName")
    LiveData<List<Task>> getUserTasks(String userName);

    @Query("SELECT * FROM task_table WHERE ownerName = :userName AND complete = 0")
    LiveData<List<Task>> getOutstandingUserTasks(String userName);

    @Query("SELECT * FROM task_table WHERE projectId = :projectId")
    LiveData<List<Task>> getProjectTasks(int projectId);
}
