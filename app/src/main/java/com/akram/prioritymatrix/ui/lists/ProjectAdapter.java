package com.akram.prioritymatrix.ui.lists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectHolder> {

    private List<Project> projects = new ArrayList<>();
    private OnItemClickListener listener;


    @NonNull
    @Override
    public ProjectAdapter.ProjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);

        return new ProjectAdapter.ProjectHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectAdapter.ProjectHolder holder, int position) {
        Project currentProject = projects.get(position);

        holder.projectName.setText(currentProject.getName());

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

    class ProjectHolder extends RecyclerView.ViewHolder{
        private TextView projectName;

        //Return the task the user clicked on
        public ProjectHolder(@NonNull View itemView) {
            super(itemView);

            projectName = itemView.findViewById(R.id.projectName);

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

