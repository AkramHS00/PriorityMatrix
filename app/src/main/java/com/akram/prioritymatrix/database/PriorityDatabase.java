package com.akram.prioritymatrix.database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {User.class, Task.class, Project.class}, version = 1)
public abstract class PriorityDatabase extends RoomDatabase {

    private static PriorityDatabase instance;

    public abstract UserDao userDao();
    public abstract TaskDao taskDao();
    public abstract ProjectDao projectDao();

    public static synchronized PriorityDatabase getInstance(Context context){

        //Synchronised means only one thread can access DB at once
        //Singleton, if instance of database is equal to null, build a new database
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            PriorityDatabase.class,
                            "park_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)//Calls roomCallback method
                    .allowMainThreadQueries()
                    .build();
        }

        return instance;

    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback(){ //Calls populate database Async task
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {  //Populates database with records on creation

        private UserDao userDao;
        private TaskDao taskDao;
        private ProjectDao projectDao;
        private PopulateDbAsyncTask(PriorityDatabase db){

            userDao = db.userDao();
            taskDao = db.taskDao();
            projectDao = db.projectDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //Add admin user when database is created
            userDao.insertUser(new User("admin", "admin", "admin"));

            projectDao.insertProject(new Project("Default", "admin"));

            return null;
        }
    }
}
