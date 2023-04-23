package com.akram.prioritymatrix.ui.report;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.TaskRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportViewModel extends AndroidViewModel {

    private TaskRepository repository;
    private LiveData<List<Task>> userTasks;


    private MutableLiveData<List<Map.Entry<String, Long>>> sortedMap;
    private MutableLiveData<List<AppUsage>> sortedAppUsages;
    //boolean for fragment to check if data has already been retrieved
    private boolean usagesRetrieved = false;

    public ReportViewModel(@NonNull Application application) {
        super(application);

        repository = new TaskRepository(application);
        sortedMap = new MutableLiveData<>();
        sortedAppUsages = new MutableLiveData<>();
    }

    public LiveData<List<Task>> getUserTasks(String userName) {
        userTasks = repository.getUserTasks(userName);
        return userTasks;
    }

    /*public List<Map.Entry<String, Long>> getSortedMap() {
        return sortedMap;
    }

    public void setSortedMap(List<Map.Entry<String, Long>> sortedMap) {
        this.sortedMap = sortedMap;
    }*/

    //Class to get usage stats for the past month asynchronously - now deprecated, could consider updating for future-proofing
    public void retrieveUsageStats(Context context){
        new AsyncTask<Void, Void, List<UsageStats>>(){

            @Override
            protected List<UsageStats> doInBackground(Void... voids) {
                PackageManager packageManager = context.getPackageManager();
                UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                Calendar date = Calendar.getInstance();
                date.add(Calendar.MONTH, -1);

                List<UsageStats> appUsages = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, date.getTimeInMillis(), System.currentTimeMillis());

                Log.i("AHS", "Retrieved app usages in async task!");
                usagesRetrieved = true;
                return appUsages;
            }

            @SuppressLint("StaticFieldLeak")
            @Override
            protected void onPostExecute(List<UsageStats> appUsages) {
                super.onPostExecute(appUsages);

                Log.i("AHS", "Setting value of sorted map in onPostExecute");
                //sortedMap.setValue(sortAppUsages(appUsages, context));
                sortedAppUsages.setValue(sortAppUsages(appUsages, context));
            }
        }.execute();
    }

    public List<AppUsage> sortAppUsages(List<UsageStats> appUsages, Context context){

        PackageManager packageManager = context.getPackageManager();
        List<Map.Entry<String, Long>> newSortedMap = new ArrayList<>();
        List<AppUsage> newAppUsages = new ArrayList<>();

        HashMap<String, Long> unsortedAppTimes = new HashMap<>();

        List<AppUsage> allAppUsages = new ArrayList<>();
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
                AppUsage appUsage = new AppUsage(appTitle, appTime, appIcon);
                allAppUsages.add(appUsage);
            }  catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (unsortedAppTimes != null){
            //Create list of hashmap entries for sorting
            List<Map.Entry<String, Long>> listedMap = new ArrayList<>(unsortedAppTimes.entrySet());

            newSortedMap = listedMap.stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .limit(5).collect(Collectors.toList());

            for (Map.Entry<String, Long> e : newSortedMap){
                for (AppUsage appUsage: allAppUsages){
                    if (e.getKey().equals(appUsage.getAppTitle())){
                        newAppUsages.add(appUsage);
                    }
                }
            }

        }

        return newAppUsages;
    }

    /*public LiveData<List<Map.Entry<String, Long>>> getSortedAppUsageStats(){
        return sortedMap;
    }*/

    public LiveData<List<AppUsage>> getSortedAppUsageStatsObjects(){
        return sortedAppUsages;
    }

    public boolean isUsagesRetrieved() {
        return usagesRetrieved;
    }


}
