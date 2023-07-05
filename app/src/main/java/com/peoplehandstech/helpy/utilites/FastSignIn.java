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
import com.peoplehandstech.helpy.UsersFetchingCallback;
import com.peoplehandstech.helpy.activities.GetHelpActivity;
import com.peoplehandstech.helpy.models.Location;
import com.peoplehandstech.helpy.activities.MainActivity;
import com.peoplehandstech.helpy.models.User;

import java.io.IOException;

public class FastSignIn {
    private static User currentUser;
    private static String TAG = "FAST_LOGIN";
    private static double WAIT_TIME = 15012.547915;

    private static boolean fromMainActivity = false;

    private static long startLoadingTime;

    private static Context providedContext;
    private static UsersFetchingCallback usersCallBack = new UsersFetchingCallback() {
        @Override
        public void onLocationReady(String country) {
            DATABASE.setUsers(country, usersCallBack);
        }

        @Override
        public void onLocationNull() {
        }

        @Override
        public void onLocationError() {

        }

        @Override
        public void onUsersSet() {
            if (DATABASE.isReadyToGo()) {
                // Get the user id
                String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                // Get the user by the id
                currentUser = DATABASE.getUser(id);
                if (currentUser == null) {
                    cancelSignIn();
                    return;
                }
                Log.d(TAG, "currentUser is not null");
                // Fetch all pending requests user has
                currentUser.setPending(RequestHandler.getUsersRequest().get(currentUser.getId()));
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            String token = task.getResult();
                            Log.d(TAG, token);
                            currentUser.setToken(token);
                            // Update user's token in the database
                            FirebaseDatabase.getInstance().getReference().child(currentUser.getCity()).child(currentUser.getId()).child("token").setValue(token);

                            // Get the city name according to the longitude and latitude of the user
                            Log.d(TAG, "Current user city is " + currentUser.getCity());

                            if (fromMainActivity) {
                                // Start the GetHelpActivity and pass the current user as an extra
                                Intent i = new Intent(providedContext, GetHelpActivity.class);
                                i.putExtra("current user", currentUser);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                providedContext.startActivity(i);
                            }
                        } else {
                            Log.d(TAG, "Error in generating new token");
                        }
                    }
                });
            } else {
                Toast.makeText(providedContext, "IS NOT READY", Toast.LENGTH_SHORT).show();
            }
        }

    };

    private static void cancelSignIn() {
        Log.d(TAG, "currentUser is null");
        FirebaseAuth.getInstance().signOut();
        if (MainActivity.window != null && MainActivity.pBar != null) {
            MainActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            MainActivity.pBar.setVisibility(View.GONE);
            MainActivity.signInClicked = false;
        }
    }


    /**
     * this method will do all the logic and the necessary steps to log the user in.
     *
     * @param ctx              the activity context
     * @param fromMainActivity use it to indicate whether you are signing in from the main activity or from a notification true means from MainActivity,
     *                         false means from notification
     */
    public static void signIn(final Context ctx, final boolean fromMainActivity) {
        DATABASE.setReadyToGo(false);
        FastSignIn.fromMainActivity = fromMainActivity;
        providedContext = ctx;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.d(TAG, "Auth current user is null");
            Toast.makeText(ctx, "Please try again..", Toast.LENGTH_SHORT).show();
            return;
        }
        DATABASE.getUserLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), ctx, usersCallBack);
        Log.d(TAG, "Auth current user is not null");
        startLoadingTime = System.nanoTime();
        timeOut();
    }


    private static void timeOut() {
        if (DATABASE.isReadyToGo()) {
            Log.d(TAG,"Is ready to go true");
            DATABASE.setReadyToGo(false);
            return;
        }

        double elapsedTime = (System.nanoTime() - startLoadingTime) / 1000000.0;
        Log.d(TAG, "fast sign in time so far : " + elapsedTime);
        if (elapsedTime >= WAIT_TIME && !DATABASE.isReadyToGo()) {
            Toast.makeText(providedContext, "Failed sign in..", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            Log.d(TAG, "fast sign in took so long, method will be break");
            if (MainActivity.window != null && MainActivity.pBar != null) {
                MainActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                MainActivity.pBar.setVisibility(View.GONE);
                MainActivity.signInClicked = false;
            }

        } else {
                new Handler().postDelayed(FastSignIn::timeOut, 1000);

        }

    }
}
