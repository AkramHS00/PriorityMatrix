package com.akram.prioritymatrix.ui.tasks;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Task;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {

    private List<Task> tasks = new ArrayList<>();
    private OnItemClickListener listener;

    DateTimeFormatter saveDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    DateTimeFormatter displayDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    DateTimeFormatter saveTimeFormat = DateTimeFormatter.ofPattern("HHmm");
    DateTimeFormatter displayTimeFormat = DateTimeFormatter.ofPattern("HH:mm");


    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);

        return new TaskHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
        Task currentTask = tasks.get(position);

        holder.taskTitle.setText(currentTask.getTitle());
        holder.taskDescription.setText(currentTask.getDescription());
        holder.categoryText.setText(currentTask.getCategory());

        holder.deadlineDate.setText(displayDateFormat.format(LocalDate.parse(currentTask.getDeadlineDate(), saveDateFormat)));
        holder.deadlineTime.setText(displayTimeFormat.format(LocalTime.parse(currentTask.getDeadlineTime(), saveTimeFormat)));


        //holder.deadlineDate.setText(String.valueOf(currentTask.getDeadlineDate()));
        //holder.deadlineDate.setText(formatDate(currentTask.getDeadlineDate()));
        //holder.deadlineTime.setText(String.valueOf(currentTask.getDeadlineTime()));
        //holder.deadlineTime.setText(formatTime(currentTask.getDeadlineTime()));

        if (currentTask.getCategory().equals("Do") ){
            holder.taskRelativeLayout.setBackgroundColor(ContextCompat.getColor(holder.taskTitle.getContext(), R.color.light_green));
        } else if (currentTask.getCategory().equals("Schedule")) {
            holder.taskRelativeLayout.setBackgroundColor(ContextCompat.getColor(holder.taskTitle.getContext(), R.color.light_yellow));
        } else if (currentTask.getCategory().equals("Delegate")) {
            holder.taskRelativeLayout.setBackgroundColor(ContextCompat.getColor(holder.taskTitle.getContext(), R.color.light_blue));
        } else {
            holder.taskRelativeLayout.setBackgroundColor(ContextCompat.getColor(holder.taskTitle.getContext(), R.color.light_red));
        }


    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    //If listings are changed, update the adapter
    public void setTasks(List<Task> tasks){
        this.tasks = tasks;
        notifyDataSetChanged(); //Let adapter know to update layout with new data, not best practice.
    }

    class TaskHolder extends RecyclerView.ViewHolder{
        private TextView taskTitle;
        private TextView taskDescription;
        private TextView deadlineDate;
        private TextView deadlineTime;
        private TextView taskRating;
        private TextView categoryText;
        private CheckBox taskCheckbox;

        private RelativeLayout taskRelativeLayout;

        //Return the task the user clicked on
        public TaskHolder(@NonNull View itemView) {
            super(itemView);

            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            deadlineDate = itemView.findViewById(R.id.deadlineDate);
            deadlineTime = itemView.findViewById(R.id.deadlineTime);
            taskRating = itemView.findViewById(R.id.taskRating);
            taskCheckbox = itemView.findViewById(R.id.taskCheckbox);
            categoryText = itemView.findViewById(R.id.categoryText);

            taskRelativeLayout = itemView.findViewById(R.id.taskRelativeLayout);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION){
                        listener.onItemClick(tasks.get(position));
                    }

                }
            });

            taskCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                    if (isChecked){
                        Log.i("AHS", "Task: " + taskTitle.getText() + " was checked.");
                        int position = getAdapterPosition();
                        listener.completeTask(tasks.get(position));
                        taskCheckbox.setChecked(false);
                    } else {
                        Log.i("AHS", "Task: " + taskTitle.getText() + " was unchecked.");
                    }

                }
            });

        }
    }

    public interface OnItemClickListener {
        void onItemClick(Task task);
        void completeTask(Task task);
    }



    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }



}
