package com.akram.prioritymatrix.ui.report;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.akram.prioritymatrix.MainActivity;
import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.User;
import com.akram.prioritymatrix.ui.tasks.TaskAdapter;
import com.akram.prioritymatrix.ui.tasks.TaskViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportFragment extends Fragment {


    private ReportViewModel reportViewModel;

    private User currentUser;

    private float completedTaskValue;
    private float overdueTaskValue;
    private float currentTaskValue;

    private List<Task> userTasks = new ArrayList<>();

    ProgressBar completedTasksProgress, overdueTasksProgress, currentTasksProgress;
    TextView completedTasksProgressText, overdueTasksProgressText, currentTasksProgressText;

    List<Map.Entry<String, Long>> sortedMap;

    RecyclerView appUsageRecyclerView;
    private AppUsageAdapter appUsageAdapter;

    private static final int REQUEST_USAGE_ACCESS_SETTINGS_PERMISSION = 1;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reportViewModel =
                new ViewModelProvider(this).get(ReportViewModel.class);

        if (!checkPermissionsGranted()){
            Intent usageRequest = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivityForResult(usageRequest, REQUEST_USAGE_ACCESS_SETTINGS_PERMISSION);
        } else {
            setupUsageRecyclerView();
        }

        completedTasksProgress = getView().findViewById(R.id.completedTasksProgress);
        completedTasksProgressText = getView().findViewById(R.id.completedTasksProgressText);
        overdueTasksProgress = getView().findViewById(R.id.overdueTasksProgress);
        overdueTasksProgressText = getView().findViewById(R.id.overdueTasksProgressText);
        currentTasksProgress = getView().findViewById(R.id.currentTasksProgress);
        currentTasksProgressText = getView().findViewById(R.id.currentTasksProgressText);

        currentUser = ((MainActivity) getActivity()).getCurrentUser();
        if (currentUser !=null){
            reportViewModel.getUserTasks(currentUser.getUserName()).observe(getActivity(), new Observer<List<Task>>() {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == REQUEST_USAGE_ACCESS_SETTINGS_PERMISSION){
            if (checkPermissionsGranted()){
                //Toast.makeText(getActivity(),"Permissions granted", Toast.LENGTH_SHORT).show();
                setupUsageRecyclerView();
            } else {
                Toast.makeText(getActivity(),"Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }

    }

    //Check if the user has permitted the app access to usage stats
    //AppOpsManager code taken from
    //https://stackoverflow.com/questions/28921136/how-to-check-if-android-permission-package-usage-stats-permission-is-given
    public boolean checkPermissionsGranted(){
        AppOpsManager appOps = (AppOpsManager) getActivity()
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), getActivity().getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        return granted;
    }

    //Gets usage stats from viewmodel if it exists, if not calls it from system on background thread
    //Sets up recycler view and adapter
    private void setupUsageRecyclerView(){
        if (!reportViewModel.isUsagesRetrieved()){
            Log.i("AHS", "AppUsages is null, retrieving from system.");
            reportViewModel.retrieveUsageStats(getActivity());
        } else {
            Log.i("AHS", "AppUsages is not null");
        }

        appUsageRecyclerView = getView().findViewById(R.id.appUsageRecyclerView);
        appUsageRecyclerView.setVisibility(View.VISIBLE);

        appUsageRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        appUsageRecyclerView.setHasFixedSize(true);

        appUsageAdapter = new AppUsageAdapter();
        appUsageRecyclerView.setAdapter(appUsageAdapter);

        reportViewModel.getSortedAppUsageStatsObjects().observe(getViewLifecycleOwner(), new Observer<List<AppUsage>>() {
            @Override
            public void onChanged(List<AppUsage> appUsages) {
                //displayAppUsage(appUsages);
                appUsageAdapter.setAppUsages(appUsages);
            }
        });
    }

}