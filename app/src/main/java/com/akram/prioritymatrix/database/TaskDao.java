package com.akram.prioritymatrix.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    void insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("SELECT * FROM task_table")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM task_table WHERE ownerName = :userName")
    LiveData<List<Task>> getUserTasks(String userName);

    @Query("SELECT * FROM task_table WHERE ownerName = :userName ORDER BY rating DESC")
    LiveData<List<Task>> getOrderedUserTasks(String userName);

    @Query("SELECT * FROM task_table WHERE ownerName = :userName AND complete = 0 ORDER BY rating DESC")
    LiveData<List<Task>> getOutstandingUserTasks(String userName);
}
