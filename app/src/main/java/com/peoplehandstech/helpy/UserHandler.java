package com.peoplehandstech.helpy;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;


public class UserHandler {

    private static User MARKER_USER;
     private static User CURRENT_USER;
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
     * @param uri is a Uri object represents the new profile picture of the user
     * @param user is a User object represents the user who wants to update his photo
     * @param context is the Context of the Activity where did this method called
    **/
    public static void updatePhoto(StorageReference mStorageRef, Uri uri, final User user, final Context context)
    {
        deleteOldPhoto(user);
        StorageReference filePath=mStorageRef.child("images").child(user.getId());
        filePath.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content

                        if(!taskSnapshot.getMetadata().getReference().getDownloadUrl().toString().isEmpty())
                        {
                            user.setPhoto(taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
                        }
                        else
                        {
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

    /**
     * use this method to delete the current user profile picture
     * this method is called in @updatePhoto
     * @param user represents the User who will delete his photo
     */
    private static void deleteOldPhoto (User user)
    {
        StorageReference mStorageRef= FirebaseStorage.getInstance().getReference().child("images").child(user.getId());
        if(mStorageRef!=null)
        {
            mStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });
        }
    }

    public static void updateUserInfo (String infoName, Object value,User wantedUser)
    {
        HashMap<String,Object> updates =new HashMap<>();
        updates.put(infoName,value);
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("User").child(wantedUser.getId());
        reference.updateChildren(updates);
    }
    public static void updateUserInfo (HashMap<String,Object> updates,User wantedUser)
    {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("User").child(wantedUser.getId());
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

    public static User getMarkerUser() {
        return MARKER_USER;
    }

    public static void setMarkerUser(User markerUser) {
        MARKER_USER = markerUser;
    }


}
