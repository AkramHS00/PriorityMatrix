package com.akram.prioritymatrix.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.ui.tasks.TaskAdapter;

import java.util.ArrayList;
import java.util.List;

public class CalendarTaskAdapter extends RecyclerView.Adapter<CalendarTaskAdapter.CalendarTaskHolder>{

    private List<Task> tasks = new ArrayList<>();
    private CalendarTaskAdapter.OnItemClickListener listener;


    @NonNull
    @Override
    public CalendarTaskAdapter.CalendarTaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_task_item, parent, false);

        return new CalendarTaskAdapter.CalendarTaskHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarTaskAdapter.CalendarTaskHolder holder, int position) {
        Task currentTask = tasks.get(position);

        holder.taskTitle.setText(currentTask.getTitle());
        holder.categoryText.setText(currentTask.getCategory());
        holder.deadlineTime.setText(formatTime(currentTask.getDeadlineTime()));

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

    class CalendarTaskHolder extends RecyclerView.ViewHolder{
        private TextView taskTitle;
        private TextView deadlineTime;
        private TextView categoryText;

        private ConstraintLayout taskRelativeLayout;

        //Return the task the user clicked on
        public CalendarTaskHolder(@NonNull View itemView) {
            super(itemView);

            taskTitle = itemView.findViewById(R.id.taskTitle);
            deadlineTime = itemView.findViewById(R.id.deadlineTime);
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

        }
    }

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    public void setOnItemClickListener(CalendarTaskAdapter.OnItemClickListener listener){
        this.listener = listener;
    }

    private String formatTime(String time){
        String formattedTime = time.substring(0,2) + ":" + time.substring(2,4) ;
        return formattedTime;
    }


}
