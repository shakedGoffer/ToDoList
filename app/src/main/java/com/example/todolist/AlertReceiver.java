package com.example.todolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


import androidx.core.app.NotificationCompat;


public class AlertReceiver extends BroadcastReceiver
{
    /*
    Task task;

    public AlertReceiver(Task task)
    {
        this.task = task;
    }

    public Task getTask() {
        return this.task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

     */
    public AlertReceiver(){}

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Toast.makeText(context,"2",Toast.LENGTH_LONG).show();

        Intent intent1 = new Intent(context, AlertService.class);
        context.startService(intent1);
    }
}
