package com.akram.prioritymatrix.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {

    @Insert
    void insertUser(User user);

    @Query("SELECT EXISTS ( SELECT * FROM user_table WHERE userName = :userName AND password = :password)")
    boolean checkLogin(String userName, String password);

    @Query("SELECT EXISTS ( SELECT * FROM user_table WHERE userName = :userName)")
    boolean isTaken(String userName);

    @Query("SELECT * FROM user_table WHERE userName = :userName AND password = :password")
    User getUser(String userName, String password);

}
