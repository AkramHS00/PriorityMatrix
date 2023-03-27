package com.akram.prioritymatrix.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ProjectRepository {

    private ProjectDao projectDao;

    private LiveData<List<Project>> allProjects;
    private LiveData<List<Project>> userProjects;
    private LiveData<List<ProjectWithTasks>> userProjectsWithTasks;

    public ProjectRepository(Application application){
        PriorityDatabase database = PriorityDatabase.getInstance(application);
        projectDao = database.projectDao();

        allProjects = projectDao.getAllProjects();
    }

    public void insertProject(Project project) { new InsertProjectAsyncTask(projectDao).execute(project);}

    public void updateProject(Project project){
        new UpdateProjectAsyncTask(projectDao).execute(project);
    }

    public void deleteProject(Project project){
        new DeleteProjectAsyncTask(projectDao).execute(project);
    }

    public LiveData<List<Project>> getAllProjects() {
        return allProjects;
    }

    public LiveData<List<Project>> getUserProjects(String userName) {
        userProjects = projectDao.getUserProjects(userName);
        return userProjects;
    }

    public LiveData<List<ProjectWithTasks>> getUserProjectsWithTasks(String userName) {
        userProjectsWithTasks = projectDao.getUserProjectsWithTasks(userName);
        return userProjectsWithTasks;
    }




    private static class InsertProjectAsyncTask extends AsyncTask<Project, Void, Void> {
        private ProjectDao projectDao;

        private InsertProjectAsyncTask(ProjectDao projectDao){
            this.projectDao = projectDao;
        }
        @Override
        protected Void doInBackground(Project... projects) {
            projectDao.insertProject(projects[0]);
            return null;
        }
    }

    private static class UpdateProjectAsyncTask extends AsyncTask<Project, Void, Void> {
        private ProjectDao projectDao;

        private UpdateProjectAsyncTask(ProjectDao projectDao){
            this.projectDao = projectDao;
        }
        @Override
        protected Void doInBackground(Project... projects) {
            projectDao.updateProject(projects[0]);
            return null;
        }
    }

    private static class DeleteProjectAsyncTask extends AsyncTask<Project, Void, Void> {
        private ProjectDao projectDao;

        private DeleteProjectAsyncTask(ProjectDao projectDao){
            this.projectDao = projectDao;
        }
        @Override
        protected Void doInBackground(Project... projects) {
            projectDao.deleteProject(projects[0]);
            return null;
        }
    }


}
