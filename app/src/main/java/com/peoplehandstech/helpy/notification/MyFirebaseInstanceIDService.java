package com.peoplehandstech.helpy.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.activities.ChatActivity;
import com.peoplehandstech.helpy.activities.FriendsListActivity;
import com.peoplehandstech.helpy.adapters.FriendsAdapter;
import com.peoplehandstech.helpy.models.Message;
import com.peoplehandstech.helpy.utilites.DATABASE;
import com.peoplehandstech.helpy.utilites.UserHandler;
import com.peoplehandstech.helpy.activities.GetHelpActivity;
import com.peoplehandstech.helpy.activities.MainActivity;
import com.peoplehandstech.helpy.utilites.FastSignIn;

import java.util.Random;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService {
    private final String ADMIN_CHANNEL_ID = "admin_channel";
    Intent  intent;
    Bitmap largeIcon;
    boolean photoReady=false;
  Handler handler;
    private String senderID;
     static Uri userUri;
     public static Bitmap bitmap;
     private String notificationType;
    public static String TAG="firebaseService";



    public static void setBitmap(Bitmap bitmap) {
        MyFirebaseInstanceIDService.bitmap = bitmap;
    }



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        Log.d(TAG,"onMessageReceived method >>> let's start");


        //handler=new Handler(Looper.getMainLooper());
        //if user is already signed in go directley to the getHelp activity
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){


            senderID=remoteMessage.getData().get("userID");
            notificationType =remoteMessage.getData().get("type");
            Log.d(TAG,"notification type "+notificationType);
            // if user is not logged in
            if(UserHandler.getCurrentUser()==null)
            {
                Log.d(TAG,"onMessageReceived method >>> UserHandler.getCurrentUser()==null");
                intent=new Intent(this, MainActivity.class);

            }
            else
            {
                Log.d(TAG,"onMessageReceived method >>> UserHandler.getCurrentUser()!=null");
                intent = new Intent(this, GetHelpActivity.class);
            }



        }else{
            Log.d(TAG,"onMessageReceived method >>> FirebaseAuth.getInstance().getCurrentUser()==null");
           intent = new Intent(this, MainActivity.class);
        }

        Log.d(TAG,"onMessageReceived method >>> Building notification");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID = new Random().nextInt(3000);
   /*
    Apps targeting SDK 26 or above (Android O) must
    implement notification channels and add their notifications to at least one of them. Therefore, confirm if the version is Oreo or higher, then setup notification channel
   */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels(notificationManager);
            Log.d(TAG,"onMessageReceived method >>> SDK_INT >= android.os.Build.VERSION_CODES.O");
        }



        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);




        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.empty_request)
                .setContentText(remoteMessage.getData().get("message"))
                .setContentTitle(remoteMessage.getData().get("senderName"))
                .setAutoCancel(true)
                .setLargeIcon(bitmap)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent);

        notificationBuilder.setColor(getResources().getColor(R.color.colorPrimaryDark));

        Log.d(TAG,"Notification type is "+notificationType+" message tag is "+Message.MESSAGE_TAG);
        if(notificationType.equals(Message.MESSAGE_TAG ))
        {
            Log.d(TAG,"notification type is message");



            if(UserHandler.getCurrentUser()!=null && ChatActivity.getCurrentChatRoom()!=null && ChatActivity.userIsHere
                    && ChatActivity.getCurrentChatRoom().getReceiverID().equals(senderID)){
                Log.d(TAG,"checking if user is on the chat room activity user is here");
            }


            else
            {
                notificationManager.notify(notificationID, notificationBuilder.build());
                Log.d(TAG,"checking if user is on the chat room activity user is not here");
                if(UserHandler.getCurrentUser()!=null)
                     {
                         DATABASE.updateLastMessageInFreindsAdapter(UserHandler.getCurrentUser());
                     }

                //FriendsAdapter.updateLastMessageHandler();

                GetHelpActivity.instance().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        GetHelpActivity.showUnreadMessage(true);


                    }
                });

            }


        }else
        {
            Log.d(TAG,"notified");
            notificationManager.notify(notificationID, notificationBuilder.build());
        }
        //check whether user is on the same chatActivity with the user who sent the message or not

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager) {
        CharSequence adminChannelName = "New notification";
        String adminChannelDescription = "Device to device notification ";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(getColor(R.color.mainOrange));
        adminChannel.enableVibration(true);

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    public static Uri getUserUri() {
        return userUri;
    }

    public static void setUserUri(Uri userUri) {
        MyFirebaseInstanceIDService.userUri = userUri;
    }

    public static Bitmap getBitmap() {
        return bitmap;
    }
}
