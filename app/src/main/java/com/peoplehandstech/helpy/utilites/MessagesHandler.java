package com.peoplehandstech.helpy.utilites;

import com.peoplehandstech.helpy.models.Message;

import java.util.HashMap;

public class MessagesHandler {

     private static HashMap<String , Message> messages=new HashMap<>();

    public static boolean addMessage(String id,Message message){
        if(!messages.containsKey(id)){
            messages.put(id,message);
            return true;
        }
        return false;
    }

    public static int getMessagesSize(){
        return messages.size();
    }

    public static HashMap<String,Message> getMessages(){
        return messages;
    }

}
