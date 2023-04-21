package com.akram.prioritymatrix;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.akram.prioritymatrix.database.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.akram.prioritymatrix.databinding.ActivityMainBinding;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private User currentUser;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        //Create shared preferences
        sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        //Get shared preferences
        String userJSON = sharedPreferences.getString("user", null  );
        if(userJSON == null){
            Log.i("AHS", "No shared preferences found, user needs to sign in.");
        } else {
            //If user is stored in shared preferences, convert from JSON to user object
            Log.i("AHS", "User is logged in.");
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJSON, User.class);
            Log.i("AHS", currentUser.getUserName().toString());
        }

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_list, R.id.navigation_matrix, R.id.navigation_calendar, R.id.navigation_report)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);


        //Set top bar to black
        getWindow().setStatusBarColor(Color.BLACK);


        //Code should only be executed on devices running Android Oreo (API 26) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            String channelId = "task_reminder";
            CharSequence channelName = "Task Reminder";
            String channelDescription = "Channel for sending reminders for tasks";

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel reminderChannel = notificationManager.getNotificationChannel(channelId);
            //Check if channel exists before creating, do not want duplicate channels
            if (reminderChannel == null){
                reminderChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                reminderChannel.setDescription(channelDescription);

                notificationManager.createNotificationChannel(reminderChannel);
            }


        }


    }





    //Called from fragments and returns the currently logged in user
    public User getCurrentUser() {
        return currentUser;
    }

    //Called from fragments and sets the user and saves to shared preferences
    public void setCurrentUser(User currentUser) {

        this.currentUser = currentUser;
        sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String jsonUser = gson.toJson(currentUser, User.class);
        Log.i("AHS", "Saved user to shared preferences with username: " + currentUser.getUserName().toString());
        editor.putString("user", jsonUser);

        editor.apply();

    }

    //Called from fragments, logs out the user and deletes saved login from shared preferences
    public void logout(){
        sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();

        currentUser = null;
    }

    public void setActionBarTitle(String title){
        getSupportActionBar().setTitle(title);
    }


    //Enables back arrow support in action bar
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp()
                || super.onSupportNavigateUp();
    }

}