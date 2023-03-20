package com.akram.prioritymatrix.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName =  "user_table")
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String userName;
    private String password;


    public User(String name, String userName, String password){
        this.name = name;
        this.userName = userName;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getName() { return name; }


}
