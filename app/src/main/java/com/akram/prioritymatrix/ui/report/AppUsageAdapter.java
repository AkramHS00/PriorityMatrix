package com.akram.prioritymatrix.ui.report;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.ui.tasks.TaskAdapter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AppUsageAdapter extends RecyclerView.Adapter<AppUsageAdapter.AppUsageHolder> {

    private List<AppUsage> appUsages = new ArrayList<>();

    @NonNull
    @Override
    public AppUsageAdapter.AppUsageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_usage_item, parent, false);

        return new AppUsageAdapter.AppUsageHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull AppUsageAdapter.AppUsageHolder holder, int position) {
        AppUsage currentAppUsage = appUsages.get(position);

        holder.appTitle.setText(currentAppUsage.getAppTitle());
        holder.appTime.setText(formatSeconds(currentAppUsage.getAppTime()));
        holder.appIcon.setImageDrawable(currentAppUsage.getAppIcon());


    }

    @Override
    public int getItemCount() {
        return appUsages.size();
    }

    //If listings are changed, update the adapter
    public void setAppUsages(List<AppUsage> appUsages){
        this.appUsages = appUsages;
        notifyDataSetChanged(); //Let adapter know to update layout with new data, not best practice.
    }

    class AppUsageHolder extends RecyclerView.ViewHolder{
        private TextView appTitle;
        private TextView appTime;
        private ImageView appIcon;

        //Return the task the user clicked on
        public AppUsageHolder(@NonNull View itemView) {
            super(itemView);

            appTitle = itemView.findViewById(R.id.appTitle);
            appTime = itemView.findViewById(R.id.appTime);
            appIcon = itemView.findViewById(R.id.appIcon);

        }
    }

    private String formatSeconds(Long time){

        //Gets number of days from seconds
        long days = TimeUnit.SECONDS.toDays(time);

        //Gets number of hours from the remainder seconds
        long hours = TimeUnit.SECONDS.toHours(time) % 24;

        //Gets number of minutes from the remainder seconds
        long minutes = TimeUnit.SECONDS.toMinutes(time) % 60;

        return String.format("%d days, %02d hours,\n%02d minutes", days, hours, minutes);
    }

}
