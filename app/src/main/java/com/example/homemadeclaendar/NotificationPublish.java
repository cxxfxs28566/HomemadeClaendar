package com.example.homemadeclaendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;



public class NotificationPublish extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent){
        String name = intent.getStringExtra("Name");
        String location = intent.getStringExtra("Location");
        String start = intent.getStringExtra("Start");
        String end = intent.getStringExtra("End");
        int id = intent.getIntExtra("Id",0);
        NotificationManager manager2 = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_iconmonstr_info_11)
                .setContentTitle("New event: "+ name)
                .setContentText("Next event at "+ start + "-" + end + " in "+location)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setAutoCancel(true);
        manager2.notify(id,builder.build());

    }
}
