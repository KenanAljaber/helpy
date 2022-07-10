package com.peoplehandstech.helpy.models;

public class NotificationFCM {

    private String to,message,from;
    private String receiverToken;

    public NotificationFCM(String to, String message, String from,String receiverToken) {
        this.to = to;
        this.message = message;
        this.from = from;
        this.receiverToken =receiverToken;
    }

    public String getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }

    public String getFrom() {
        return from;
    }

    public String getReceiverToken() {
        return receiverToken;
    }
}
