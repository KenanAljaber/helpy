package com.peoplehandstech.helpy.notification;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static android.app.Notification.DEFAULT_ALL;
import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

public class MyNotifications extends Application {

     public static final String CHANNEL_1_ID = "channel1";
     public static final String CHANNEL_2_ID = "channel2";
     public static final String ASK_FOR_HELP="ASK_FOR_HELP";
    public static final String ACCEPT_REQUEST="ACCEPT_REQUEST";
    public static final String RATING="RATING";


    @Override
    public void onCreate (){
        super.onCreate();
        createNotificationChannel();
    }


    public  void createNotificationChannel() {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel channel1=new NotificationChannel(
                    CHANNEL_1_ID,"channel 1",NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("this is channel 1");

            NotificationChannel channel2=new NotificationChannel(
                    CHANNEL_2_ID,"channel 2",NotificationManager.IMPORTANCE_LOW);
            channel1.setDescription("this is channel 2");
            NotificationManager manager=getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);

        }

    }

}
