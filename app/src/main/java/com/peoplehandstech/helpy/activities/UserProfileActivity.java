package com.peoplehandstech.helpy.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.peoplehandstech.helpy.utilites.DATABASE;
import com.peoplehandstech.helpy.models.PopupWindow;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.utilites.UserHandler;
import com.peoplehandstech.helpy.fragments.SettingsFragment;
import com.peoplehandstech.helpy.models.User;
import com.peoplehandstech.helpy.utilites.ImagesTools;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {
    private static User currUser;
    private static TextView userName,helpText,phoneNumberText,reputation,helped,askedForHelp;
    private static CircleImageView circleImage;
    public static CircleImageView circleImageView;
    private static StorageReference mStorageRef;
    private  static ImageView iView;
    private ImageView logoutImage;
    private static FirebaseUser fUser;
    private static Activity USER_PROFILE_CONTEXT;
    private static int UPDATE_IMAGE_CODE=123;
    private static String TAG="USER_PROFILE";
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_user_profile);
        fUser= FirebaseAuth.getInstance().getCurrentUser();
        USER_PROFILE_CONTEXT=this;
         iView=findViewById(R.id.user_profile_Reload);
        mStorageRef= FirebaseStorage.getInstance().getReference();
        circleImage= findViewById(R.id.user_profile_updatePhoto);
        circleImageView=findViewById(R.id.user_profile_circleImage);
        userName=findViewById(R.id.user_profile_proName);
        helpText=findViewById(R.id.user_profile_helpTextView);
        phoneNumberText=findViewById(R.id.user_profile_phoneNumber_TextView);
        reputation=findViewById(R.id.user_profile_reputation_text);
        helped=findViewById(R.id.user_profile_helped_text);
        askedForHelp=findViewById(R.id.user_profile_asked_for_help_text);
        logoutImage =findViewById(R.id.logOut_button);


        loadMainFragment();
        String currUserId=fUser.getUid();
        ImagesTools.loadUserImage(currUserId,circleImageView,this);




        currUser =(User)getIntent().getSerializableExtra("current User");
        if(currUser !=null) {
            fUser = FirebaseAuth.getInstance().getCurrentUser();
            mStorageRef = FirebaseStorage.getInstance().getReference();
            String name = currUser.getName();
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            userName.setText(name);
            phoneNumberText.setText(currUser.getPhoneNumber());
            helpText.setText(currUser.getHowToHelp());
            setCardViewInfo(currUser.getReputation(),reputation);
            setCardViewInfo(currUser.getHelped(),helped);
            setCardViewInfo(currUser.getAskedForHelp(),askedForHelp);

            }


        logoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });



        }

    private void setCardViewInfo(int reputationInt, TextView textView) {
        if(reputationInt== 0)
        {
            textView.setText(String.valueOf(reputationInt));
            textView.setTextColor(getColor(R.color.almostBlack));
        }
        if(reputationInt<0)
        {
            textView.setText(String.valueOf(reputationInt));
            textView.setTextColor(getColor(R.color.red));
        }
        else{
            textView.setText(String.valueOf(reputationInt));
        }
    }

        public void changePhoto (View view)
        {
            Intent i=new Intent();
            UserHandler.selectPhoto(this,i,UPDATE_IMAGE_CODE);

        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(USER_PROFILE_CONTEXT,"here out",Toast.LENGTH_SHORT).show();
        if(requestCode==UPDATE_IMAGE_CODE && resultCode==RESULT_OK && data != null)
        {


                uri=data.getData();
                UserHandler.uploadPhoto(mStorageRef,uri, currUser,USER_PROFILE_CONTEXT);
            HashMap<String,Uri> photos= DATABASE.getPhotos();
            if(photos!=null )
            {
                if(photos.containsKey(currUser.getId()))
                {
                    photos.remove(currUser.getId());
                    photos.put(currUser.getId(),uri);

                }

            }


        }
    }

    public static void updateUser ()
        {
            currUser =UserHandler.getCurrentUser();
            if(currUser !=null)
            {
                //Toast.makeText(USER_PROFILE_CONTEXT,currUser.getName(),Toast.LENGTH_SHORT).show();
                userName.setText(currUser.getName());
                helpText.setText(currUser.getHowToHelp());
                phoneNumberText.setText(currUser.getPhoneNumber());
                USER_PROFILE_CONTEXT.getIntent().putExtra("current User", currUser);

            }
        }


    private void logOut ()
    {
        PopupWindow.setModify(true);
        PopupWindow.setText("Do you really want to logout?");
        startActivity(new Intent(UserProfileActivity.this,PopupWindow.class));

        final Handler handler=new Handler();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                if(PopupWindow.isReady()){
                    if(PopupWindow.ACCEPT>0)
                    {
                        if(MainActivity.pBar!=null){
                            MainActivity.pBar.setVisibility(View.GONE);
                            MainActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }
                        FirebaseAuth.getInstance().signOut();
                        UserProfileActivity.this.finish();
                    }
                    else{


                        System.out.println(TAG + " before "+PopupWindow.ACCEPT );
                    }
                }else{
                    handler.postDelayed(this,1000);
                }
            }
        };

        handler.postDelayed(runnable,1000);



//        Intent i=new Intent(this,MainActivity.class);
//        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(i);
        GetHelpActivity.GET_HELP.finish();
        //this.finish();
    }


    public void loadMainFragment ()
        {
            SettingsFragment sFragment=new SettingsFragment();

            if(sFragment.isAdded())
            {
                return;
            }
            else
            {
                FragmentManager manager=getSupportFragmentManager();
                FragmentTransaction transaction =manager.beginTransaction();
                transaction.add(R.id.frameContainer,sFragment);
                transaction.commit();
            }

        }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
       this.finish();
    }

    public void loadImage (View view)
        {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images").child(currUser.getId());
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(UserProfileActivity.this)
                            .load(uri)
                            .into(circleImageView);
                    iView.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    iView.setVisibility(View.VISIBLE);
                }
            });
        }



    public void backToMap (View view)
    {
        this.finish();
    }


private void restartApp(int time ){
    Intent mStartActivity = new Intent(getApplicationContext(), MainActivity.class);
    PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), time,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
    AlarmManager mgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
    System.exit(0);
}



}
