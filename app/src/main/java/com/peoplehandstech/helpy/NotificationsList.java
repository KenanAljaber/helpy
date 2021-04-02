package com.peoplehandstech.helpy;

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
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class NotificationsList extends AppCompatActivity implements MyAdapterListener {
    private  androidx.recyclerview.widget.RecyclerView recyclerView;
    private MyRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Request> myRequests =new ArrayList<>();
    private User currUser;
    private boolean accepted;
    private static boolean USER_NOTIFICATIONS_UPDATED=false;
    public static final String TAG="NOTIFICATION_LIST";
    private static boolean userAccepts;
    Bundle t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests_list_holder);

        t=savedInstanceState;
       recyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);
        if(getIntent().getSerializableExtra("current user")!=null)
        {
            currUser=(User) getIntent().getSerializableExtra("current user");
            myRequests.clear();
            myRequests =currUser.getPending();
            setNotificationSeen(myRequests);
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
        deleteRequest(request);

        FirebaseDatabase.getInstance().getReference().child("User").child(currUser.getId()).child(Request.TAG)
                .child(Request.YOU_ACCEPTED+request.getRequestId()).setValue(request);

        // create a new notification to notify the notification user that its accepted
        Request acceptedRequest = new Request(currUser.getId(),
                currUser.getName()+" "+getString(R.string.has_accepted_your_request_you_can_contact_him_directly_via_whatsapp)+"\n"+currUser.getPhoneNumber()
                ,currUser.getName(),getString(R.string.request_accepted));
        // get the notification user by the notification id
        User notificationUser=DATABASE.getUser(request.getRequestId());
        // upload the accepted request notification to the user
        FirebaseDatabase.getInstance().getReference().child("User").child(notificationUser.getId()).child(Request.TAG)
                .child(Request.ACCEPTED_REQUEST+currUser.getId()).setValue(acceptedRequest);
        // and here we set the notification into the user array
       RequestHandler.getUsersRequest().get(notificationUser.getId()).add(acceptedRequest);
        ArrayList<Request> notificationsUserArray =RequestHandler.getUsersRequest().get(notificationUser.getId());
        notificationUser.setPending(notificationsUserArray);
        int userHelped=currUser.getHelped()+1;
        currUser.setHelped(userHelped);
        UserHandler.updateUserInfo("helped",userHelped,currUser);

    }
    private void deleteRequest (Request request)
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("User")
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
            PopupWindow.setText("By accepting any request your phone number will be sent to the request sender.");
            startActivity(new Intent(NotificationsList.this,PopupWindow.class));

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
                            selectedRequest.setMessage(getString(R.string.your_phone_number_will_be_send_to)+" "+selectedRequest.getName()+getString(R.string.so_you_can_get_in_touch));
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
            deleteRequest(selectedRequest);
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
                FirebaseDatabase.getInstance().getReference().child("User").child(currUser.getId()).child(Request.TAG)
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
        NotificationsList.userAccepts = userAccepts;
    }
}
