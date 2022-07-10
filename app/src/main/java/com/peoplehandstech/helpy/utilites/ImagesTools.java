package com.peoplehandstech.helpy.utilites;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import android.util.Base64;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.peoplehandstech.helpy.notification.MyFirebaseInstanceIDService;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;


public class ImagesTools {
        static String TAG="IMAGE_TOOLS";
        static Bitmap userBitmap;
    public static Bitmap StringToBitMap(String encodedString){
        try{
            byte [] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }
//
    public static void getUrlAsync (String userID, final Context ctx ){
        // Points to the root reference

        final Handler handler =new Handler(Looper.getMainLooper());


        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference dateRef = storageRef.child("images").child(userID);
        dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadUrl)
            {
                MyFirebaseInstanceIDService.setUserUri(downloadUrl);
                //getBitmapFromUrl(downloadUrl.toString(),ctx);
                MyFirebaseInstanceIDService.setBitmap(getBitmapfromUrlTest(downloadUrl.toString()));
                //do something with downloadurl
            }
        });
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                if(MyFirebaseInstanceIDService.getUserUri()==null || MyFirebaseInstanceIDService.getBitmap()==null){
                    handler.postDelayed(this,1000);
                }
            }
        };
        handler.postDelayed(runnable,1000);


    }

    public static Bitmap getBitmapfromUrlTest(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }


     private static void getBitmapFromUrl (String url, Context context){

    final Handler handler=new Handler(Looper.getMainLooper());


         Glide.with(context)
                 .asBitmap()
                 .load(url)
                 .into(new SimpleTarget<Bitmap>() {
                     @Override
                     public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                         userBitmap=resource;
                     }
                 });
   Runnable runnable=new Runnable() {
       @Override
       public void run() {
           if(userBitmap==null){
               handler.postDelayed(this,1000);
           }else{
               MyFirebaseInstanceIDService.setBitmap(userBitmap);
           }
       }
   };
   handler.postDelayed(runnable,1000);


    }

    public static void loadUserImage (String userID, final CircleImageView view, final Context context){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images").child(userID);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context)
                        .load(uri)
                        .into(view);

            }
        });
    }


}
