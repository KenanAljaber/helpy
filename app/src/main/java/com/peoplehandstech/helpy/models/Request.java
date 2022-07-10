package com.peoplehandstech.helpy.models;

import com.peoplehandstech.helpy.notification.Notification;

import java.io.Serializable;

public class Request implements Serializable, Notification {

//    private Uri requestPhoto;

    private String requestId;
    private String message;
    private String name;
    private String title;
    private boolean accepted;
    private boolean seen;
    public static final String TAG="Notifications";
    public static final String NEW_REQUEST  = "NEW_REQUEST";
    public static final String ACCEPTED_REQUEST  = "ACCEPTED_REQUEST";
    public static final String YOU_ACCEPTED  = "YOU_ACCEPTED";
    public Request() {

    }

    public Request(String requestId, String message, String name, String title) {
//        this.requestPhoto = requestPhoto;
        this.requestId = requestId;
        this.message = message;
        this.name = name;
        this.title=title;
        accepted=false;
        seen =false;
    }


    public String getRequestId() {
        return requestId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
