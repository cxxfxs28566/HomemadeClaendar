package com.example.homemadeclaendar;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class NotificationPublish extends BroadcastReceiver {
    private static final String channelId = "test1";

//    @Override
//    public void onReceive(Context context, Intent intent){
//        String name = intent.getStringExtra("Name");
//        String location = intent.getStringExtra("Location");
//        String start = intent.getStringExtra("Start");
//        String end = intent.getStringExtra("End");
//        int id = intent.getIntExtra("Id",0);
//
//        NotificationManager manager2 = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext())
//                .setSmallIcon(R.drawable.ic_iconmonstr_info_11)
//                .setContentTitle("New event: "+ name)
//                .setContentText("Next event at "+ start + "-" + end + " in "+location)
//                .setPriority(NotificationCompat.PRIORITY_HIGH);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//        {
//            String channelId = "Test Channel 1";
//            NotificationChannel channel = new NotificationChannel(
//                    channelId,
//                    "A Test Channel",
//                    NotificationManager.IMPORTANCE_HIGH);
//                    manager2.createNotificationChannel(channel);
//                    builder.setChannelId(channelId);
//        }
//        manager2.notify(id,builder.build());
//
//    }

    @Override
    public void onReceive(Context context, Intent intent){
        String name = intent.getStringExtra("Name");
        String location = intent.getStringExtra("Location");
        String start = intent.getStringExtra("Start");
        String end = intent.getStringExtra("End");
        int id = intent.getIntExtra("Id",0);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, channelId);
        mBuilder.setSmallIcon(R.drawable.ic_event_available);
        mBuilder.setContentTitle("New event: "+ name);
        mBuilder.setContentText("Next event at "+ start + "-" + end + " in "+location);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Test Channel 1",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
        }
        mNotificationManager.notify(id, mBuilder.build());
    }
}
