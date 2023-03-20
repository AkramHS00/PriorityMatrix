package com.akram.prioritymatrix.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName =  "project_table")
public class Project implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String ownerName;

    public Project(String name, String ownerName) {
        this.name = name;
        this.ownerName = ownerName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getOwnerName() {
        return ownerName;
    }
}
