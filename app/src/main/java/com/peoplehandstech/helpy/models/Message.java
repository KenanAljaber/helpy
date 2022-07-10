package com.peoplehandstech.helpy.models;

import java.io.Serializable;

public class Message implements Serializable {

    private String senderId;
    private String messageBody;
    private String date;
    private boolean seen;
   // private String id;
    public static String MESSAGE_TAG="Message";

    public Message(String senderId, String messageBody, String date) {
        this.senderId = senderId;
        this.messageBody = messageBody;
        this.date = date;
        seen=false;
    }

    public Message(){

    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getDate() {
        return date;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

//    public String getId() {
//        return id;
//    }
}
