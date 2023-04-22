package com.akram.prioritymatrix.ui.tasks;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.akram.prioritymatrix.MainActivity;
import com.akram.prioritymatrix.R;

public class NotificationBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("AHS", "Sending notification");

        //Get the task title
        String taskTitle = intent.getStringExtra("taskTitle");
        long intentId = intent.getLongExtra("intentId",0);

        //New intent to open the app when user clicks the notification
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra("intentId", intentId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) intentId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        //Set notification properties
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task_reminder")
                .setSmallIcon(R.drawable.matrix_icon)
                .setContentTitle("Task Reminder")
                .setContentText(taskTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        //Display notification, set ID to 0 as we cancel the alarm manager not the notification when updating tasks
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify((int) intentId, builder.build());

    }
}
