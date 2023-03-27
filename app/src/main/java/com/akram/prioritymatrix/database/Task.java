package com.akram.prioritymatrix.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName =  "task_table")
public class Task implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String ownerName;

    private String title;
    private String description;
    private int rating;

    private boolean addDeadline;
    private String deadlineDate;
    private String deadlineTime;

    private boolean addReminder;
    private String reminderDate;
    private String reminderTime;

    private boolean complete;
    private String category;

    private int projectId;

    private float posX;
    private float posY;

    //private SubTask[] subtask;
    //private List list;


    public Task(String ownerName, String title, String description, int rating, boolean addDeadline,
                String deadlineDate, String deadlineTime, boolean addReminder,
                String reminderDate, String reminderTime, boolean complete, String category, int projectId,
                float posX, float posY) {
        this.ownerName = ownerName;
        this.title = title;
        this.description = description;
        this.rating = rating;
        this.addDeadline = addDeadline;
        this.deadlineDate = deadlineDate;
        this.deadlineTime = deadlineTime;
        this.addReminder = addReminder;
        this.reminderDate = reminderDate;
        this.reminderTime = reminderTime;
        this.complete = complete;
        this.category = category;
        this.projectId = projectId;
        this.posX = posX;
        this.posY = posY;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getRating() {
        return rating;
    }

    public boolean isAddDeadline() {
        return addDeadline;
    }

    public String getDeadlineDate() {
        return deadlineDate;
    }

    public String getDeadlineTime() {
        return deadlineTime;
    }

    public boolean isAddReminder() {
        return addReminder;
    }

    public String getReminderDate() {
        return reminderDate;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean getComplete() { return complete; }

    public void setComplete(boolean complete) { this.complete = complete; }

    public String getCategory() {
        return category;
    }

    public int getProjectId() {
        return projectId;
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
