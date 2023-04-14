package com.akram.prioritymatrix.ui.report;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.akram.prioritymatrix.MainActivity;
import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.User;
import com.akram.prioritymatrix.ui.tasks.TaskViewModel;

import java.util.ArrayList;
import java.util.List;

public class ReportFragment extends Fragment {

    private ReportViewModel mViewModel;
    private TaskViewModel tempViewModel;

    private User currentUser;

    private float completedTaskValue;
    private float overdueTaskValue;
    private float currentTaskValue;

    private List<Task> userTasks = new ArrayList<>();

    ProgressBar completedTasksProgress, overdueTasksProgress, currentTasksProgress;
    TextView completedTasksProgressText, overdueTasksProgressText, currentTasksProgressText;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tempViewModel =
                new ViewModelProvider(this).get(TaskViewModel.class);

        completedTasksProgress = getView().findViewById(R.id.completedTasksProgress);
        completedTasksProgressText = getView().findViewById(R.id.completedTasksProgressText);
        overdueTasksProgress = getView().findViewById(R.id.overdueTasksProgress);
        overdueTasksProgressText = getView().findViewById(R.id.overdueTasksProgressText);
        currentTasksProgress = getView().findViewById(R.id.currentTasksProgress);
        currentTasksProgressText = getView().findViewById(R.id.currentTasksProgressText);

        currentUser = ((MainActivity) getActivity()).getCurrentUser();
        tempViewModel.getUserTasks(currentUser.getUserName()).observe(getActivity(), new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                userTasks = tasks;

                int completedTasks = 0;
                int overdueTasks = 0;
                int currentTasks = 0;
                for (Task t: tasks){
                    if (t.getComplete()){
                        completedTasks++;
                    }
                    if (t.isOverDue()){
                        overdueTasks++;
                    }
                    if (!t.isOverDue() && !t.getComplete()){
                        currentTasks++;
                    }
                }
                completedTaskValue = (int)(((float) completedTasks/ (float) tasks.size()) * 100);
                completedTasksProgress.setProgress((int) completedTaskValue);
                completedTasksProgressText.setText(String.valueOf(completedTasks) + "/" + String.valueOf(tasks.size()));

                overdueTaskValue = (int)(((float) overdueTasks/ (float) tasks.size()) * 100);
                overdueTasksProgress.setProgress((int) overdueTaskValue);
                overdueTasksProgressText.setText(String.valueOf(overdueTasks) + "/" + String.valueOf(tasks.size()));

                currentTaskValue = (int)(((float) currentTasks/ (float) tasks.size()) * 100);
                currentTasksProgress.setProgress((int) currentTaskValue);
                currentTasksProgressText.setText(String.valueOf(currentTasks) + "/" + String.valueOf(tasks.size()));
            }
        });




    }
}