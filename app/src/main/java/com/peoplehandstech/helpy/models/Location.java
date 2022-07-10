package com.peoplehandstech.helpy.models;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class Location implements Serializable {

    private double latitude,longitude;
    static String TAG="LOCATION_HELPY";


    public Location ()
    {

    }
    public Location(double latitude, double longitude)
    {
        this.latitude=latitude;
        this.longitude=longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public static String getCityName(double longitude, double latitude, Context ctx) throws IOException,NullPointerException {
        Geocoder gcd = new Geocoder(ctx, Locale.getDefault());
        List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
        if (addresses.size() > 0) {

            if(addresses.get(0).getCountryName()!=null){
                Log.d(TAG,addresses.get(0).getCountryName().toString());
                return addresses.get(0).getCountryName();
            }
            if(addresses.get(0).getLocality()!=null){
                Log.d(TAG,addresses.get(0).getLocality().toString());

                return addresses.get(0).getLocality();
            }
            if(addresses.get(0).getSubLocality()!=null){
                Log.d(TAG,addresses.get(0).getSubLocality().toString());
                return addresses.get(0).getSubLocality();
            }


        else {
            Log.d(TAG,"empty location");
            // do your stuff
            Toast.makeText(ctx, "Error ",Toast.LENGTH_LONG).show();
            return null;
        }
        }
        return null;
    }
}
