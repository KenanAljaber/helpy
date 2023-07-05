package com.peoplehandstech.helpy.utilites;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.peoplehandstech.helpy.RefreshCallback;
import com.peoplehandstech.helpy.UserDataChangesCallback;
import com.peoplehandstech.helpy.UsersFetchingCallback;
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

    public static boolean isLocationChanged() {
        return locationChanged;
    }

    public static void setLocationChanged(boolean locationChanged) {
        UserHandler.locationChanged = locationChanged;
    }

    private static boolean locationChanged=false;
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


    public static void updateUserInfoByAttribute(String attributeName, Object value, User wantedUser, UserDataChangesCallback onUserDataChanged)
    {
        Log.d(TAG,"Updating user " + attributeName + " attribute");
        HashMap<String,Object> updates =new HashMap<>();
        updates.put(attributeName,value);
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child(wantedUser.getCity()).child(wantedUser.getId());
        reference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG,"Updated successfully");
                onUserDataChanged.onChangesCompleted();
            }
        });
    }
    public static void updateUserInfo(HashMap<String,Object> updates, User wantedUser, UserDataChangesCallback onUserDataChanged)
    {
        Log.d(TAG,"Updating user "+ updates.size() +" attributes");
        DatabaseReference userRef= FirebaseDatabase.getInstance().getReference().child(wantedUser.getCity()).child(wantedUser.getId());
        userRef.updateChildren(updates);
        Log.d(TAG,"children updated");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                User currentUser=snapshot.getValue(User.class);
                setCurrentUser(currentUser);
            if(updates.get("city")!=null && !wantedUser.getCity().equals(updates.get("city"))){
                    Log.d(TAG,"Location updated, moving user to a new node");
                  moveUserToNewLocatoin(wantedUser.getCity(),(String)updates.get("city"),wantedUser.getId(),onUserDataChanged);
             }else{
                Log.d(TAG,"Location was not updated, and process has finished!");
                onUserDataChanged.onChangesCompleted();
            }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private static void moveUserToNewLocatoin(String oldCountry, String newCountry, String userId,UserDataChangesCallback onUserDataChanged) {
    Log.d(TAG,"Moving user to a new location "+newCountry);
        // Change the location of the user in the user locatoins node (the dictionary)
       FirebaseDatabase.getInstance().getReference().child("Users Locations").child(userId).setValue(newCountry);
        // Get references to the old and new locations
        DatabaseReference oldRef = FirebaseDatabase.getInstance().getReference().child(oldCountry).child(userId);
        DatabaseReference newRef = FirebaseDatabase.getInstance().getReference().child(newCountry).child(userId);

        // Read the data from the old location

        oldRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the data from the old location
                    Object data = dataSnapshot.getValue();

                    // Remove the data from the old location
                    oldRef.removeValue();

                    // Write the data to the new location
                    newRef.setValue(data);

                    DATABASE.setUsers(newCountry, new UsersFetchingCallback() {
                        @Override
                        public void onLocationReady(String country) {

                        }

                        @Override
                        public void onLocationNull() {

                        }

                        @Override
                        public void onLocationError() {

                        }

                        @Override
                        public void onUsersSet() {
                            setLocationChanged(true);
                            onUserDataChanged.onChangesCompleted();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that may occur
            }
        });
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

    public static void refreshUserFriendsList(User user, RefreshCallback refreshCallback){
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
                refreshCallback.onFirendsListRefreshComlete();
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

    private static UsersFetchingCallback usersCallback=new UsersFetchingCallback() {
        @Override
        public void onLocationReady(String country) {

        }

        @Override
        public void onLocationNull() {

        }

        @Override
        public void onLocationError() {

        }

        @Override
        public void onUsersSet() {
//            setLocationChanged(true);
        }
    };


}
