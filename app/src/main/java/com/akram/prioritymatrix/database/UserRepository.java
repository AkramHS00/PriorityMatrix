package com.akram.prioritymatrix.database;

import android.app.Application;
import android.os.AsyncTask;

public class UserRepository {

    private UserDao userDao;
    private boolean checkLogin;

    public UserRepository(Application application){
        PriorityDatabase database = PriorityDatabase.getInstance(application);
        userDao = database.userDao(); //Can call userDao method from DB class because Room generates files.
    }

    public void insert(User user) { new InsertUserAsyncTask(userDao).execute(user);}


    public boolean checkLogin(String userName, String password){
        return userDao.checkLogin(userName, password);
    }

    public boolean isTaken(String userName){
        return userDao.isTaken(userName);
    }

    public User getUser(String userName, String password){
        return userDao.getUser(userName, password);
    }




    private static class InsertUserAsyncTask extends AsyncTask<User, Void, Void> {
        private UserDao userDao;

        private InsertUserAsyncTask(UserDao noteDao){
            this.userDao = noteDao;
        }
        @Override
        protected Void doInBackground(User... users) {
            userDao.insertUser(users[0]);
            return null;
        }
    }

}
