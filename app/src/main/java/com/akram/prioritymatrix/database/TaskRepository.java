package com.akram.prioritymatrix.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class TaskRepository {

    private TaskDao taskDao;
    private LiveData<List<Task>> allTasks;
    private LiveData<List<Task>> userTasks;
    private LiveData<List<Task>> orderedUserTasks;
    private LiveData<List<Task>> outstandingUserTasks;

    public TaskRepository(Application application){
        PriorityDatabase database = PriorityDatabase.getInstance(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasks();
    }

    public void insertTask(Task task){
        new InsertTaskAsyncTask(taskDao).execute(task);
    }

    public void updateTask(Task task){
        new UpdateTaskAsyncTask(taskDao).execute(task);
    }

    public void deleteTask(Task task){
        new DeleteTaskAsyncTask(taskDao).execute(task);
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<List<Task>> getUserTasks(String userName) {
        userTasks = taskDao.getUserTasks(userName);
        return userTasks;
    }

    public LiveData<List<Task>> getOrderedUserTasks(String userName) {
        orderedUserTasks = taskDao.getOrderedUserTasks(userName);
        return orderedUserTasks;
    }

    public LiveData<List<Task>> getOutstandingUserTasks(String userName) {
        outstandingUserTasks = taskDao.getOutstandingUserTasks(userName);
        return outstandingUserTasks;
    }

    private static class InsertTaskAsyncTask extends AsyncTask<Task, Void, Void> {
        private TaskDao taskDao;

        private InsertTaskAsyncTask(TaskDao taskDao){
            this.taskDao = taskDao;
        }
        @Override
        protected Void doInBackground(Task... tasks) {
            taskDao.insertTask(tasks[0]);
            return null;
        }
    }

    private static class UpdateTaskAsyncTask extends AsyncTask<Task, Void, Void> {
        private TaskDao taskDao;

        private UpdateTaskAsyncTask(TaskDao taskDao){
            this.taskDao = taskDao;
        }
        @Override
        protected Void doInBackground(Task... tasks) {
            taskDao.updateTask(tasks[0]);
            return null;
        }
    }

    private static class DeleteTaskAsyncTask extends AsyncTask<Task, Void, Void> {
        private TaskDao taskDao;

        private DeleteTaskAsyncTask(TaskDao taskDao){
            this.taskDao = taskDao;
        }
        @Override
        protected Void doInBackground(Task... tasks) {
            taskDao.deleteTask(tasks[0]);
            return null;
        }
    }
}
