package com.peoplehandstech.helpy.utilites;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.peoplehandstech.helpy.activities.UserProfileActivity;
import com.peoplehandstech.helpy.models.Friend;
import com.peoplehandstech.helpy.models.User;

import java.util.ArrayList;
import java.util.HashMap;


public class UserHandler  {

    private static User MARKER_USER;
     private static User CURRENT_USER;
     private static  User CHAT_USER;
     private static String TAG="USER_HANDLER";
    /**
     * {@link #CURRENT_USER}, is the user who is signed in
     */

     private UserHandler()
     {

     }

    /**
     * this method gives the possibility to select profile picture
     * @param activity represents the Activity who called this method
     * @param i represents the Intent who gonna open the file chooser
     * @param PICK_IMAGE_CODE represents the code who will be given to startActivityForResult method
     */
     public static void selectPhoto(Activity activity,Intent i,int PICK_IMAGE_CODE) {
         i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(i, "Select Picture"), PICK_IMAGE_CODE);
    }

    /** updatePhoto method remove the old photo from forebase storage and adds the new one
     * @param mStorageRef  is a StorageReference gives the location of file images in fire base storage
     * @param newUri is a Uri object represents the new profile picture of the user
     * @param user is a User object represents the user who wants to update his photo
     * @param context is the Context of the Activity where did this method called
    **/
    public static void uploadPhoto(StorageReference mStorageRef, Uri newUri, final User user, final Context context)
    {
        Log.d(TAG,"Updating photo from user handler");
        StorageReference filePath=mStorageRef.child("images").child(user.getId());
        filePath.putFile(newUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Log.d(TAG,"Photo has been successfully updated");
                        if(!taskSnapshot.getMetadata().getReference().getDownloadUrl().toString().isEmpty())
                        {
                            user.setPhoto(taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
                            notifyDatabaseWithChanges(user,taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
                            ImagesTools.loadUserImage(user.getId(), UserProfileActivity.circleImageView,context);
                            Log.d(TAG,"Photo has been successfully loaded and saved");
                        }
                        else
                        {
                            Log.d(TAG,"Something wrong happend when updating photo");
                            Toast.makeText(context,"empty",Toast.LENGTH_LONG).show();
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                });
    }

    private static void notifyDatabaseWithChanges(User user,String uri) {
        FirebaseDatabase.getInstance().getReference().child("PhotoChanges").child(user.getId()).setValue(user.getId());
    }


    public static void updateUserInfo (String infoName, Object value,User wantedUser)
    {
        HashMap<String,Object> updates =new HashMap<>();
        updates.put(infoName,value);
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child(wantedUser.getCity()).child(wantedUser.getId());
        reference.updateChildren(updates);
    }
    public static void updateUserInfo (HashMap<String,Object> updates,User wantedUser)
    {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child(wantedUser.getCity()).child(wantedUser.getId());
        reference.updateChildren(updates);
    }



    public static User getCurrentUser() {
        return CURRENT_USER;
    }

    /**
     * set the static object {@link #CURRENT_USER} to the current signed in user
     * @param user the user who is already signed in
     */
    public static void setCurrentUser(User user) {
        UserHandler.CURRENT_USER = user;
    }

    public static User getChatUser() {
        return CHAT_USER;
    }

    public static void setChatUser(User chatUser) {
        CHAT_USER = chatUser;
    }

    public static User getMarkerUser() {
        return MARKER_USER;
    }

    public static void setMarkerUser(User markerUser) {
        MARKER_USER = markerUser;
    }

    public static void refreshUserFriendsList(User user){
        FirebaseDatabase.getInstance().getReference().child(user.getCity()).child(user.getId()).child("Friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Friend> friends=new ArrayList<>();
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    Friend currentFriend= snapshot1.getValue(Friend.class);
                    friends.add(currentFriend);
                }
                user.setFriendsList(friends);
                UserHandler.setCurrentUser(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static ArrayList<User> filterUser (String filter){
        filter=filter.toLowerCase();
        ArrayList<User> filteredUsers=new ArrayList<>();
        for(User user:DATABASE.getUsers()){
            if(user.getHowToHelp().toLowerCase().equals(filter) || user.getHowToHelp().toLowerCase().contains(filter) ){
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
    }


}
