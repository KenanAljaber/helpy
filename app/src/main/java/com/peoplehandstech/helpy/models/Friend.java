package com.peoplehandstech.helpy.models;

import java.io.Serializable;

public class Friend implements Serializable {

    private String id;
    private String chatRoomID;



    public Friend(String id, String chatRoomID) {
        this.id = id;
        this.chatRoomID = chatRoomID;
    }
    public Friend (){

    }

    public String getId() {
        return id;
    }

    public String getChatRoomID() {
        return chatRoomID;
    }


}
