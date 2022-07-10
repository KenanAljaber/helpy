package com.peoplehandstech.helpy.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
//import android.support.v7.widget.RecyclerView;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.peoplehandstech.helpy.models.ChatRoom;
import com.peoplehandstech.helpy.models.Friend;
import com.peoplehandstech.helpy.notification.MyNotifications;
import com.peoplehandstech.helpy.utilites.DATABASE;
import com.peoplehandstech.helpy.models.PopupWindow;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.utilites.RequestHandler;
import com.peoplehandstech.helpy.utilites.UserHandler;
import com.peoplehandstech.helpy.adapters.MyAdapterListener;
import com.peoplehandstech.helpy.adapters.MyRecyclerViewAdapter;
import com.peoplehandstech.helpy.models.NotificationFCM;
import com.peoplehandstech.helpy.models.Request;
import com.peoplehandstech.helpy.models.User;
import com.peoplehandstech.helpy.notification.FirebaseNotificationHandler;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class NotificationsListActivity extends AppCompatActivity implements MyAdapterListener {
    private  androidx.recyclerview.widget.RecyclerView recyclerView;
    private MyRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Request> myRequests =new ArrayList<>();
    private User currUser;
    private boolean accepted;
    private static boolean USER_NOTIFICATIONS_UPDATED=false;
    public static final String TAG="NOTIFICATION_LIST";
    private static boolean userAccepts;
    private RelativeLayout emptyRequestRL;
    Bundle t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests_list_holder);

        t=savedInstanceState;
        emptyRequestRL= findViewById(R.id.empty_requestsList);
       recyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);
        if(getIntent().getSerializableExtra("current user")!=null)
        {
            currUser=(User) getIntent().getSerializableExtra("current user");
            myRequests.clear();
            myRequests =currUser.getPending();
            setNotificationSeen(myRequests);
            if(myRequests.size()==0){
                emptyRequestRL.setVisibility(View.VISIBLE);
            }else{
                emptyRequestRL.setVisibility(View.GONE);
            }
            Log.d("REQUEST_LIST","recyclerView size is "+String.valueOf(myRequests.size()));
            if(recyclerView==null)
            {
                Log.d("REQUEST_LIST","recyclerView is null");
            }
            mAdapter = new MyRecyclerViewAdapter(myRequests, this);

            layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
        }

    }



    private void acceptRequest (Request request)
    {
        Toast.makeText(getApplicationContext(),getString(R.string.request_accepted),Toast.LENGTH_SHORT).show();

        //modify the accepted request and upload it to the data base
        request.setSeen(true);
        deleteRequestFromDatabase(request);
        FirebaseDatabase.getInstance().getReference().child(currUser.getCity()).child(currUser.getId()).child(Request.TAG)
                .child(Request.YOU_ACCEPTED+request.getRequestId()).setValue(request);

        // create a new notification to notify the notification user that its accepted
        Request acceptedRequest = new Request(currUser.getId(),
                currUser.getName()+" "+getString(R.string.has_accepted_your_request_you_can_contact_him_directly_via_whatsapp)
                ,currUser.getName(),getString(R.string.request_accepted));
        // get the notification user by the notification id
        User notificationUser=DATABASE.getUser(request.getRequestId());
        // upload the accepted request notification to the user
        FirebaseDatabase.getInstance().getReference().child(notificationUser.getCity()).child(notificationUser.getId()).child(Request.TAG)
                .child(Request.ACCEPTED_REQUEST+currUser.getId()).setValue(acceptedRequest);
        // and here we set the notification into the user array
       RequestHandler.getUsersRequest().get(notificationUser.getId()).add(acceptedRequest);
        ArrayList<Request> notificationsUserArray =RequestHandler.getUsersRequest().get(notificationUser.getId());
        notificationUser.setPending(notificationsUserArray);
        int userHelped=currUser.getHelped()+1;
        currUser.setHelped(userHelped);
        addToFriendsList(request);

        UserHandler.updateUserInfo("helped",userHelped,currUser);
        UserHandler.refreshUserFriendsList(currUser);


        //notify the owner of the notification that current user has accepted his request
        NotificationFCM acceptedRequestNotification=new NotificationFCM(notificationUser.getName(),acceptedRequest.getMessage(),currUser.getName(),notificationUser.getToken());
        try {
            JSONObject notification=FirebaseNotificationHandler.createJSONObject(acceptedRequestNotification,currUser,notificationUser,"accepted your request!", MyNotifications.ACCEPT_REQUEST);
            FirebaseNotificationHandler.sendNotification(notification,getApplicationContext());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }
    private void deleteRequestFromDatabase(Request request)
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child(currUser.getCity())
                .child(currUser.getId()).child(Request.TAG) .child((Request.NEW_REQUEST)+request.getRequestId());
        databaseReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

            }
        });
    }

    @Override
    public void onIconClicked(View v, final int position) {

        // if user accepts the request
        if(v.getId()==R.id.request_list_content_yesButton_RL)
        {
            PopupWindow.setModify(true);
            PopupWindow.setText("By accepting this request "+myRequests.get(position).getName()+" will be added to your friend list.");
            startActivity(new Intent(NotificationsListActivity.this,PopupWindow.class));

            final Handler handler=new Handler();
            Runnable runnable=new Runnable() {
                @Override
                public void run() {
                    if(PopupWindow.isReady()){
                        if(userAccepts)
                        {
                            userAccepts=false;
                            System.out.println(TAG + " after YES");
                            Request selectedRequest = myRequests.get(position);
                            selectedRequest.setTitle(getString(R.string.accepted));
                            selectedRequest.setMessage(getString(R.string.you_are_a_friend_now_with)+" "+selectedRequest.getName()+getString(R.string.so_you_can_get_in_touch));
                            selectedRequest.setAccepted(true);
                            selectedRequest.setSeen(true);
                            myRequests.remove(position);
                            myRequests.add(position,selectedRequest);
                            mAdapter.notifyItemChanged(position);
                            mAdapter.notifyItemRangeChanged(position, myRequests.size());
                            mAdapter.notifyDataSetChanged();
                            acceptRequest(selectedRequest);

                            setUserNotificationsUpdated(true);
                        }
                        else{


                            System.out.println(TAG + " before "+userAccepts );
                        }
                    }else{
                        handler.postDelayed(this,1000);
                    }
                }
            };

            handler.postDelayed(runnable,1000);

            return;
        }
        // if user does not accept the request
        if(v.getId()==R.id.request_list_content_noButton_RL)
        {
            System.out.println("no");
            Request selectedRequest = myRequests.get(position);
            selectedRequest.setAccepted(false);
            selectedRequest.setSeen(true);
            myRequests.remove(position);
            recyclerView.removeViewAt(position);
            mAdapter.notifyItemRemoved(position);
            mAdapter.notifyItemRangeChanged(position, myRequests.size());
            mAdapter.notifyDataSetChanged();
            deleteRequestFromDatabase(selectedRequest);
            currUser.setPending(myRequests);
            setUserNotificationsUpdated(true);
            Toast.makeText(getApplicationContext(),getString(R.string.request_denied),Toast.LENGTH_SHORT).show();
            return;

        }

    }
    @Override
    public void onBackPressed() {

       if(isUserNotificationsUpdated())
       {

           Intent returnIntent=new Intent();
           User result=(User) getIntent().getSerializableExtra("current user");
           result.setPending(myRequests);
           returnIntent.putExtra("updated notifications user",result);
           setResult(Activity.RESULT_OK,returnIntent);
           finish();

       }
       else
       {
           User result=(User) getIntent().getSerializableExtra("current user");
           result.setPending(setNotificationSeen(myRequests));
           Intent returnIntent = new Intent();
           returnIntent.putExtra("updated notifications user",result);
           setResult(Activity.RESULT_CANCELED, returnIntent);
           finish();
       }


    }





    private void addToFriendsList(Request request){


            User notificationUser= DATABASE.getUser(request.getRequestId());

            //create friend for current user and add it to his friends list
            ArrayList<Friend>currUserFriends=currUser.getFriendsList();
            ChatRoom chatRoom=createChatRoom(currUser,notificationUser);
            Friend friend=new Friend(request.getRequestId(),chatRoom.getRoomID());
            currUserFriends.add(friend);
            currUser.setFriendsList(currUserFriends);

        //create friend for notification user and add it to his friends list
        if(notificationUser.getFriendsList()!=null){
            Log.d(TAG,"addToFriendList method >> notification user is not null");


            ArrayList<Friend> notificationUserFriends=notificationUser.getFriendsList();
            Friend notificationFriend=new Friend(currUser.getId(),chatRoom.getRoomID());
            notificationUserFriends.add(notificationFriend);
            notificationUser.setFriendsList(notificationUserFriends);


            addFriendsToDatabase( currUser,friend);
            addFriendsToDatabase(notificationUser,notificationFriend);
        }else{
            Log.d(TAG,"addToFriendList method >> notification user is null");
        }

    }

    private void addFriendsToDatabase(User user,Friend friend){
        FirebaseDatabase.getInstance().getReference().child(user.getCity()).child(user.getId()).child("Friends")
                .child(friend.getId()).setValue(friend);
    }






    private ChatRoom createChatRoom (User currUser,User notificationUser){

        String chatRoomID=currUser.getId()+notificationUser.getId();
       // currUser.addChatRoomID(chatRoomID);
        //notificationUser.addChatRoomID(chatRoomID);

        ChatRoom chatRoom=new ChatRoom(chatRoomID);
        chatRoom.addUserToChatRoom(currUser);
        chatRoom.addUserToChatRoom(notificationUser);

        FirebaseDatabase.getInstance().getReference().child("chatRooms").child(chatRoomID).setValue(chatRoom);

        return chatRoom;

    }

    public static boolean isUserNotificationsUpdated() {
        return USER_NOTIFICATIONS_UPDATED;
    }

    public static void setUserNotificationsUpdated(boolean userNotificationsUpdated) {
        USER_NOTIFICATIONS_UPDATED = userNotificationsUpdated;
    }
    private ArrayList<Request> setNotificationSeen(ArrayList<Request> myRequests)
    {
        ArrayList<Request> returnArray= myRequests;
        for(Request currNotify: returnArray)
        {
            if(currNotify.getTitle().equals(getString(R.string.request_accepted)) && !currNotify.isSeen() ||
                    currNotify.getTitle().equals("Solicitud acceptada") && !currNotify.isSeen() )
            {
                currNotify.setSeen(true);
                FirebaseDatabase.getInstance().getReference().child(currUser.getCity()).child(currUser.getId()).child(Request.TAG)
                        .child(Request.ACCEPTED_REQUEST+currNotify.getRequestId()).setValue(currNotify);
                System.out.println("NOTIFICATIONS_LIST ss: " + currNotify.isSeen());
            }
            else
            {
                System.out.println("NOTIFICATIONS_LIST ww: " +currNotify.getMessage()+" seen is :"+currNotify.isSeen());
            }
        }
        return  returnArray;
    }
    public boolean buildAlertMessageNoGps (Context context){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("By accepting any request your phone number will be sent to the request sender.")
                .setCancelable(false)
                .setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        accepted = true;

                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        accepted = false;
                        dialog.cancel();


                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
        return accepted;
    }

    public static boolean isUserAccepts() {
        return userAccepts;
    }

    public static void setUserAccepts(boolean userAccepts) {
        NotificationsListActivity.userAccepts = userAccepts;
    }
}
