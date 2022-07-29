package com.example.todolist;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class ExampleService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        String taskTitle = intent.getStringExtra("taskTitle");

        /*
        Intent notificationIntent = new Intent(this, HomeScreen.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
         */

        /*
        Notification notification = new NotificationCompat.Builder(this, "Task reminder")
                .setContentTitle("Example Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

         */

        //do heavy work on a background thread
        //stopSelf();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Notification(taskTitle);
            }
        }, 300000);


        return startId;
    }

    private void Notification(String taskTitle)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel
                    ("Task reminder", "Task reminder", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Task reminder");
        builder.setContentTitle("Task reminder");
        builder.setContentText("Your task:" + taskTitle + " due date has passed");
        builder.setSmallIcon(R.drawable.ic_time);
        builder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}