package com.akram.prioritymatrix.ui.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.akram.prioritymatrix.R;

public class NotificationBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("AHS", "Sending notification");

        //Get the task title
        String taskTitle = intent.getStringExtra("taskTitle");
        long intentId = intent.getLongExtra("intentId",0);

        //Set notification properties
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task_reminder")
                .setSmallIcon(R.drawable.matrix_icon)
                .setContentTitle("Task Reminder")
                .setContentText(taskTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        //Display notification, set ID to 0 as we cancel the alarm manager not the notification when updating tasks
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify((int) intentId, builder.build());

    }
}
