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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    DateTimeFormatter saveDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

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

            List<AppUsage> newAppUsages = new ArrayList<>();

            @Override
            protected List<UsageStats> doInBackground(Void... voids) {
                PackageManager packageManager = context.getPackageManager();
                UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                Calendar date = Calendar.getInstance();
                date.add(Calendar.MONTH, -1);

                List<UsageStats> appUsages = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, date.getTimeInMillis(), System.currentTimeMillis());

                Log.i("AHS", "Retrieved app usages in async task!");
                usagesRetrieved = true;

                newAppUsages = sortAppUsages(appUsages, context);

                return appUsages;
            }

            @SuppressLint("StaticFieldLeak")
            @Override
            protected void onPostExecute(List<UsageStats> appUsages) {
                super.onPostExecute(appUsages);

                Log.i("AHS", "Setting value of sorted map in onPostExecute");
                //sortedMap.setValue(sortAppUsages(appUsages, context));
                sortedAppUsages.setValue(newAppUsages);
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

                if (unsortedAppTimes.containsKey(appTitle)){
                    unsortedAppTimes.put(appTitle, unsortedAppTimes.get(appTitle) + appTime);

                    //If the there is already an instance of the app, add the time to it instead of creating a new one
                    for (AppUsage a: allAppUsages){
                        if (appTitle.equals(a.getAppTitle())){
                            a.setAppTime(a.getAppTime() + appTime);
                        }
                    }

                } else {
                    unsortedAppTimes.put(appTitle, appTime);
                    AppUsage appUsage = new AppUsage(appTitle, appTime, appIcon);
                    allAppUsages.add(appUsage);
                }

                //Log.i("AHS", "App: " + appTitle);

            }  catch (PackageManager.NameNotFoundException e) {
                //Log.i("AHS", "Try failed");
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

    public List<Task> getMonthsTasks(List<Task> tasks){

        List<Task> monthlyTasks = new ArrayList<>();

        for (Task t: tasks){
            LocalDate taskDeadline = LocalDate.parse(t.getDeadlineDate(), saveDateFormat);
            if (taskDeadline.getMonthValue() == LocalDate.now().getMonthValue()){
                monthlyTasks.add(t);
            }
        }

        return monthlyTasks;

    }

    public String getMostPopularDay(List<Task> tasks){

        HashMap<String, Integer> dayCount = new HashMap<>();
        dayCount.put("SATURDAY" ,0);
        dayCount.put("SUNDAY" ,0);
        dayCount.put("MONDAY" ,0);
        dayCount.put("TUESDAY" ,0);
        dayCount.put("WEDNESDAY" ,0);
        dayCount.put("THURSDAY" ,0);
        dayCount.put("FRIDAY" ,0);
        for (Task t: tasks){
            LocalDate taskDeadline = LocalDate.parse(t.getDeadlineDate(), saveDateFormat);
            DayOfWeek day = taskDeadline.getDayOfWeek();
            dayCount.put(day.name(), dayCount.get(day.name()) + 1);
        }
        String mostPopularDay = null;
        int highestNum = 0;

        for (Map.Entry<String, Integer> entry : dayCount.entrySet()){

            if(mostPopularDay == null){
                mostPopularDay = entry.getKey();
                highestNum = entry.getValue();
            } else {
                Log.i("AHS", "Else looping");
                if (entry.getValue() >= highestNum){
                    mostPopularDay = entry.getKey();
                    highestNum = entry.getValue();
                }
            }
        }

        return mostPopularDay.substring(0, 1).toUpperCase() + mostPopularDay.substring(1).toLowerCase();

    }

    public String getLeastPopularDay(List<Task> tasks){

        HashMap<String, Integer> dayCount = new HashMap<>();
        dayCount.put("SATURDAY" ,0);
        dayCount.put("SUNDAY" ,0);
        dayCount.put("MONDAY" ,0);
        dayCount.put("TUESDAY" ,0);
        dayCount.put("WEDNESDAY" ,0);
        dayCount.put("THURSDAY" ,0);
        dayCount.put("FRIDAY" ,0);
        for (Task t: tasks){
            LocalDate taskDeadline = LocalDate.parse(t.getDeadlineDate(), saveDateFormat);
            DayOfWeek day = taskDeadline.getDayOfWeek();
            dayCount.put(day.name(), dayCount.get(day.name()) + 1);
        }
        String leastPopularDay = null;
        int lowestNum = 0;

        for (Map.Entry<String, Integer> entry : dayCount.entrySet()){

            if(leastPopularDay == null){
                Log.i("AHS", "leastpopularDay is null so setting to: " + entry.getKey());
                leastPopularDay = entry.getKey();
                lowestNum = entry.getValue();
            } else {
                Log.i("AHS", "Else looping");
                if (entry.getValue() <= lowestNum){
                    Log.i("AHS", "This else is lower than least popular day");
                    leastPopularDay = entry.getKey();
                    lowestNum = entry.getValue();
                }
            }
        }

        return leastPopularDay.substring(0, 1).toUpperCase() + leastPopularDay.substring(1).toLowerCase();
    }

    public String getMostOverdueCategory(List<Task> tasks){
        HashMap<String, Integer> categoryOverdueCount = new HashMap<>();
        categoryOverdueCount.put("Do", 0);
        categoryOverdueCount.put("Schedule", 0);
        categoryOverdueCount.put("Delegate", 0);
        categoryOverdueCount.put("Delete", 0);

        //Get number of overdue tasks per category
        for (Task t: tasks){
            if (t.isOverDue() && !t.getComplete()){
                String category = t.getCategory();
                categoryOverdueCount.put(category, categoryOverdueCount.get(category) + 1);
                //Log.i("AHS", "Overdue task " + t.getTitle() + " " + t.getCategory());
            }
        }

        String highestCategory = null;
        int categoryCount = 0;

        for (Map.Entry<String, Integer> entry : categoryOverdueCount.entrySet()){
            if (highestCategory == null){
                highestCategory = entry.getKey();
                categoryCount = entry.getValue();
                //Log.i("AHS", "Highest category is null so setting to " + entry.getKey());
            } else {
                if (entry.getValue() >= categoryCount){
                    //Log.i("AHS", "This entry value is greater than category count " + entry.getKey() + " " + entry.getValue());
                    highestCategory = entry.getKey();
                    categoryCount = entry.getValue();
                }
            }
        }

        //Will = 0 if there are no overdue tasks, want to show this differently in report
        if (categoryCount == 0){
            return "null";
        } else {
            return highestCategory;
        }


    }

    public String getMostPopularCategory(List<Task> tasks){
        HashMap<String, Integer> categoryOverdueCount = new HashMap<>();
        categoryOverdueCount.put("Do", 0);
        categoryOverdueCount.put("Schedule", 0);
        categoryOverdueCount.put("Delegate", 0);
        categoryOverdueCount.put("Delete", 0);

        //Get number of overdue tasks per category
        for (Task t: tasks){
            categoryOverdueCount.put(t.getCategory(), categoryOverdueCount.get(t.getCategory()) + 1);
        }

        String highestCategory = null;
        int categoryCount = 0;

        for (Map.Entry<String, Integer> entry : categoryOverdueCount.entrySet()){
            if (highestCategory == null){
                highestCategory = entry.getKey();
                categoryCount = entry.getValue();
            } else {
                if (entry.getValue() >= categoryCount){
                    Log.i("AHS", "This else is lower than least popular day");
                    highestCategory = entry.getKey();
                    categoryCount = entry.getValue();
                }
            }
        }

        return highestCategory;
    }
}
