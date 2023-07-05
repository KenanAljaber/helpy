package com.peoplehandstech.helpy.fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.peoplehandstech.helpy.notification.MyNotifications;
import com.peoplehandstech.helpy.models.NotificationFCM;
import com.peoplehandstech.helpy.activities.OtherUserProfileActivity;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.utilites.RequestHandler;
import com.peoplehandstech.helpy.utilites.UserHandler;
import com.peoplehandstech.helpy.models.Rating;
import com.peoplehandstech.helpy.models.User;
import com.peoplehandstech.helpy.notification.FirebaseNotificationHandler;

import org.json.JSONObject;

import java.io.FileNotFoundException;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentRate extends Fragment implements View.OnClickListener {


    private RelativeLayout thumbsUp,thumbsDown;
    private User markerUser,currUser;
    private String TAG = "FRAGMENT_RATE";


    public FragmentRate() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_fragment_rate, container, false);

        thumbsDown=v.findViewById(R.id.rate_fragment_thumbsDown);
        thumbsUp=v.findViewById(R.id.rate_fragment_thumbsUp);
        if(getActivity().getIntent().getSerializableExtra("marker user")!=null)
        {
            markerUser =(User)getActivity().getIntent().getSerializableExtra("marker user");
            currUser= UserHandler.getChatUser();
        }
        thumbsUp.setOnClickListener(this);
        thumbsDown.setOnClickListener(this);

        return v;
    }

    public void onClick (View view)
    {
        if(view.getId()==thumbsUp.getId() )
        {
            addRate(Rating.POSITIVE_RATING,markerUser);
            return;

        }
        if(view.getId()==thumbsDown.getId())
        {
            addRate(Rating.NEGATIVE_RATING,markerUser);
            return;
        }
    }

    private void addRate(int RATE_TAG, final User markerUser) {
       boolean canRate= RequestHandler.checkUserToRate(currUser,markerUser.getId(),getContext());
       if(canRate){
           Rating rating=new Rating(markerUser.getId(),RATE_TAG);
           markerUser.addRate(rating);

           UserHandler.setMarkerUser(markerUser);
           OtherUserProfileActivity.updateUser();
           final int newReputation=markerUser.getReputation();
           String id =markerUser.getId();


           FirebaseDatabase.getInstance().getReference().child(currUser.getCity()).child(currUser.getId()).child("my rates").child(markerUser.getName()+markerUser.getId())
                   .setValue(rating).addOnSuccessListener(new OnSuccessListener<Void>() {
               @Override
               public void onSuccess(Void aVoid) {
                   Toast.makeText(getActivity(),"Successfully rated!",Toast.LENGTH_SHORT).show();
                   UserHandler.updateUserInfoByAttribute("reputation",newReputation,markerUser,()->{

                   NotificationFCM notificationFCM=new NotificationFCM(markerUser.getName(),"You have a new review!","Someone",markerUser.getToken());
                   try {
                      JSONObject notification= FirebaseNotificationHandler.createJSONObject(notificationFCM,currUser,markerUser,"has rated you!", MyNotifications.RATING);
                       FirebaseNotificationHandler.sendNotification(notification,getContext());
                   } catch (FileNotFoundException e) {
                       e.printStackTrace();
                   }
                   });


               }
           });

       }else{
           Toast.makeText(getActivity(),"you can not rate",Toast.LENGTH_SHORT).show();
       }
    }

}
