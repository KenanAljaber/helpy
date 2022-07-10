package com.peoplehandstech.helpy.utilites;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.peoplehandstech.helpy.activities.GetHelpActivity;
import com.peoplehandstech.helpy.models.Location;
import com.peoplehandstech.helpy.activities.MainActivity;
import com.peoplehandstech.helpy.models.User;

import java.io.IOException;

public class FastSignIn {
    private static User currentUser;
    private static String TAG="FAST_LOGIN";
    private static double WAIT_TIME=15012.547915;



    /**
     * this method will do all the logic and the necessary steps to log the user in.
     * @param ctx the activity context
     * @param fromMainActivity use it to indicate whether you are signing in from the main activity or from a notification true means from MainActivity,
     *                         false means from notification
     */
    public static void signIn (final Context ctx, final boolean fromMainActivity){
        DATABASE.setReadyToGo(false);
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            DATABASE.getUserLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),ctx);
            Log.d(TAG,"Auth current user is not null");
        }else{
            Log.d(TAG,"Auth current user is null");
            Toast.makeText(ctx,"Please try again..",Toast.LENGTH_SHORT).show();
            return;
        }

        final long cur = System.nanoTime();
        final Handler handler=new Handler(Looper.getMainLooper());
        // a runnable instance that contains the signing in progress

        Runnable runnable = new Runnable() {
            @Override
            public void run() {


                if(DATABASE.isReadyToGo() )
                {
                    //get the user id
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    //get the user by the id
                    currentUser = DATABASE.getUser(id);
                    if(currentUser==null){
                        Log.d(TAG,"currentUser is null");
                        FirebaseAuth.getInstance().signOut();
                        if(MainActivity.window!=null && MainActivity.pBar!=null){
                            MainActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            MainActivity.pBar.setVisibility(View.GONE);
                            MainActivity.signInClicked=false;
                        }
                        return;
                    }else{
                        Log.d(TAG,"currentUser is not null");

                    }

                    //fetch all pending requests user has
                    currentUser.setPending(RequestHandler.getUsersRequest().get(currentUser.getId()));


                    //final DatabaseReference reff= FirebaseDatabase.getInstance().getReference().child("Tokens");

                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if(!task.isSuccessful()){
                                Log.d(TAG,"field in generating new token");
                            }else{
                                String token=task.getResult();
                                Log.d(TAG,token);
                                currentUser.setToken(token);
                                FirebaseDatabase.getInstance().getReference().child(currentUser.getCity()).child(currentUser.getId()).child("token").setValue(token);
                                //DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("User").child(currentUser.getId());

                                //get the city name according to the longitude and latitude of the user
                                    Log.d(TAG,"Current user city is "+ currentUser.getCity());



//                                DatabaseReference reff=FirebaseDatabase.getInstance().getReference();
//                                reff.child(currentUser.getCity()).child(currentUser.getId()).setValue(currentUser);
//
//                                // this code should be deleted after cleaning all users in the database
//                                reff.child("Users Locations").child(currentUser.getId()).setValue(currentUser.getCity());




                                if(fromMainActivity){
                                    Intent i = new Intent(ctx, GetHelpActivity.class);
                                    i.putExtra("current user", currentUser);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    ctx.startActivity(i);
                                }
                            }
                        }
                    });
                }
                else {

                    double elapsedTime=(System.nanoTime() - cur) / 1000000.0;
                    Log.d(TAG,"fast sign in time so far : "+elapsedTime);
                    if(elapsedTime>=WAIT_TIME){
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(ctx,"Failed sign in..",Toast.LENGTH_SHORT).show();
                        Log.d(TAG,"fast sign in took so long, method will be break");
                        if(MainActivity.window!=null && MainActivity.pBar!=null){
                            MainActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            MainActivity.pBar.setVisibility(View.GONE);
                            MainActivity.signInClicked=false;
                        }
                        return;
                    }
                    // if not wait another second
                    handler.postDelayed(this, 1000);

                }
            }
        };
        // wait for PENDING_REQUESTS_DOWNLOADED to be true and database to be ready
        handler.postDelayed(runnable, 1000);
    }




}
