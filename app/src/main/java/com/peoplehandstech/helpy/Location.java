package com.peoplehandstech.helpy;

import java.io.Serializable;

public class Location implements Serializable {

    private double latitude,longitude;


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
}
