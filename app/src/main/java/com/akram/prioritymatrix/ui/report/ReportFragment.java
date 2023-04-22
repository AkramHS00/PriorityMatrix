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

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.akram.prioritymatrix.MainActivity;
import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.User;
import com.akram.prioritymatrix.ui.tasks.TaskViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    TextView text1,text2,text3,text4,text5;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        LinearLayout textLinearLayout;
        textLinearLayout = getView().findViewById(R.id.textLinearLayout);
        int current = 0;
        text1 = getView().findViewById(R.id.text1);
        text2 = getView().findViewById(R.id.text2);
        text3 = getView().findViewById(R.id.text3);
        text4 = getView().findViewById(R.id.text4);
        text5 = getView().findViewById(R.id.text5);


        //AppOpsManager code taken from
        //https://stackoverflow.com/questions/28921136/how-to-check-if-android-permission-package-usage-stats-permission-is-given
        AppOpsManager appOps = (AppOpsManager) getActivity()
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), getActivity().getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;

        if (!granted){
            Intent usageRequest = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(usageRequest);
        } else {
            PackageManager packageManager = getActivity().getPackageManager();
            UsageStatsManager usageStatsManager = (UsageStatsManager) getActivity().getSystemService(Context.USAGE_STATS_SERVICE);
            Calendar date = Calendar.getInstance();
            date.add(Calendar.MONTH, -1);
            //Get app usage times from the past month
            List<UsageStats> appUsages = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, date.getTimeInMillis(), System.currentTimeMillis());

            HashMap<String, Long> unsortedAppTimes = new HashMap<>();
            for (UsageStats s: appUsages){
                //Try to get app names from usage stats

                try {
                    String appName = s.getPackageName();
                    //Get app info
                    ApplicationInfo appInfo = packageManager.getApplicationInfo(appName, PackageManager.GET_META_DATA);
                    //Get components of app info
                    String appTitle = (String) packageManager.getApplicationLabel(appInfo);
                    Long appTime = s.getTotalTimeInForeground() / 1000;
                    Drawable appIcon = packageManager.getApplicationIcon(appInfo);
                    unsortedAppTimes.put(appTitle, appTime);
                    //Log.i("AHS", "App: " + appTitle);
                }  catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

            if (unsortedAppTimes != null){
                //Create list of hashmap entries for sorting
                List<Map.Entry<String, Long>> listedMap = new ArrayList<>(unsortedAppTimes.entrySet());

                List<Map.Entry<String, Long>> sortedMap = listedMap.stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .limit(5).collect(Collectors.toList());

                for (Map.Entry<String, Long> e: sortedMap){
                    Log.i("AHS", "App: " + e.getKey() + "Time: " + e.getValue());

                    TextView temp = (TextView) textLinearLayout.getChildAt(current);
                    current++;
                    if (temp != null){
                        temp.setText("App: " + e.getKey() + " Time: " + e.getValue());
                    }

                }
            }
        }



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