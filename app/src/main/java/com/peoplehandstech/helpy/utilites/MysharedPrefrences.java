package com.peoplehandstech.helpy.utilites;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class MysharedPrefrences {

    public static void writeToSharedPrefrence(String cityName, Activity activity){
        SharedPreferences sharedPref =activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("cityName",cityName);
        editor.apply();
    }

    public static String readFromSharedPrefrences(Activity activity){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        String cityName = sharedPref.getString("cityName","");
        return cityName;
    }


}
