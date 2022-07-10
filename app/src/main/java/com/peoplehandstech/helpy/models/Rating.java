package com.peoplehandstech.helpy.models;

import com.peoplehandstech.helpy.notification.Notification;

import java.io.Serializable;

public class Rating implements Serializable, Notification {

    private int rate;
    public static final int POSITIVE_RATING=1;
    public static final int NEGATIVE_RATING=-1;

    private String id;

    public Rating()
    {

    }

    public Rating( String raterId,int rate)
    {
        this.rate=rate;
      this.id=raterId;
    }

    public int getRate() {
        return rate;
    }

    public String getId() {
        return id;
    }

}
