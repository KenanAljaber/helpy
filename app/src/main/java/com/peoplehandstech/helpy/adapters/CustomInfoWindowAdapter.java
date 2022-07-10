package com.peoplehandstech.helpy.adapters;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.peoplehandstech.helpy.activities.GetHelpActivity;
import com.peoplehandstech.helpy.R;


import de.hdodenhof.circleimageview.CircleImageView;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private View mWindow;

     public CustomInfoWindowAdapter(Context context)
     {
         this.context=context;
         mWindow= LayoutInflater.from(context).inflate(R.layout.custom_info_window,null);
     }
     private void getWindowElements (Marker marker , View view )
     {
         final CircleImageView circleImageView= view.findViewById(R.id.custom_info_circleImage);
        if(GetHelpActivity.photos.get(marker)!=null)
        {
                Glide.with(context.getApplicationContext()).load(GetHelpActivity.photos.get(marker)).into(circleImageView);
        }
        else
        {
            Log.i("Custom info Window","the uri is null because of the on success callback in GetHelpActivity");
        }
         TextView name=view.findViewById(R.id.custom_info_name_textView);
         String userName=marker.getTitle();
         name.setText(userName);

         TextView help=view.findViewById(R.id.custom_info_help_textView);
         String userHelp=marker.getSnippet();
         help.setText(userHelp);
     }

    @Override
    public View getInfoWindow(Marker marker) {
         getWindowElements(marker,mWindow);

        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        getWindowElements(marker,mWindow);
        return mWindow;
    }

}
