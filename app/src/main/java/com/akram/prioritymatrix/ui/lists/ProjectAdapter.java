package com.akram.prioritymatrix.ui.lists;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Project;
import com.akram.prioritymatrix.database.ProjectWithTasks;
import com.akram.prioritymatrix.database.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectHolder> {

    private List<Project> projects = new ArrayList<>();
    private List<ProjectWithTasks> projectWithTasks = new ArrayList<>();
    private OnItemClickListener listener;

    private HashMap<Project, List<Task>> projectWithTasksHash= new HashMap<Project, List<Task>>();


    @NonNull
    @Override
    public ProjectAdapter.ProjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);

        return new ProjectAdapter.ProjectHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectAdapter.ProjectHolder holder, int position) {
        Project currentProject = projects.get(position);

        List<Task> tasks = projectWithTasksHash.get(currentProject);
        int totalCount = projectWithTasksHash.get(currentProject).size();
        int completedCount = 0;

        for (Task t : tasks){
            if(t.getComplete() == true){
                completedCount++;
            }
        }

        holder.projectName.setText(currentProject.getName());
        holder.projectTaskCounter.setText(String.valueOf(completedCount) + "/" + String.valueOf(totalCount));
        holder.projectProgressBar.setProgress((int)((float) completedCount/ (float) totalCount * 100));

    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    //If listings are changed, update the adapter
    public void setProjects(List<Project> projects){
        this.projects = projects;
        notifyDataSetChanged(); //Let adapter know to update layout with new data, not best practice.
    }

    public void setProjectWithTasks(List<ProjectWithTasks> projectWithTasks){

        ArrayList<Project> projects = new ArrayList<>();
        for (ProjectWithTasks p : projectWithTasks){
            Project project = p.getProject();
            List<Task> tasks = p.getTasks();
            projectWithTasksHash.put(project, tasks);

            projects.add(project);
        }
        this.projects = projects;
        this.projectWithTasks = projectWithTasks;
        notifyDataSetChanged();

    }


    class ProjectHolder extends RecyclerView.ViewHolder{
        private TextView projectName;
        private TextView projectTaskCounter;
        private ProgressBar projectProgressBar;

        //Return the task the user clicked on
        public ProjectHolder(@NonNull View itemView) {
            super(itemView);

            projectName = itemView.findViewById(R.id.projectName);
            projectTaskCounter = itemView.findViewById(R.id.taskCounter);
            projectProgressBar = itemView.findViewById(R.id.progressBar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION){
                        listener.onItemClick(projects.get(position));
                    }

                }
            });


        }
    }

    public interface OnItemClickListener {
        void onItemClick(Project project);
    }



    public void setOnItemClickListener(com.akram.prioritymatrix.ui.lists.ProjectAdapter.OnItemClickListener listener){
        this.listener = listener;
    }




}

