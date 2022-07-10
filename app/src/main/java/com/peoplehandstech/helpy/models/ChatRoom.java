package com.peoplehandstech.helpy.models;

import com.peoplehandstech.helpy.utilites.DATABASE;
import com.peoplehandstech.helpy.utilites.UserHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatRoom implements Serializable {

    private String roomID;

   private ArrayList<String> chatRoomUsers;
    private ArrayList<Message> messages;






    public ChatRoom (String roomID){
        this.roomID=roomID;
        chatRoomUsers=new ArrayList<>();

        messages=new ArrayList<>();
    }

    public ChatRoom(){

    }


    public int getMessageSize (){
        if(messages!=null){
            return messages.size();
        }else {
            return -1;
        }
    }

    public void addUserToChatRoom (User user) {
        if(chatRoomUsers.size()<2){
            chatRoomUsers.add(user.getId());
        }

    }



    public void addMessage (Message message){
            messages.add(message);
    }

    public String getRoomID() {
        return roomID;
    }

    public void setChatRoomUsers(ArrayList<String> chatRoomUsers) {
        this.chatRoomUsers = chatRoomUsers;
    }

    public ArrayList<String> getChatRoomUsers() {
        return chatRoomUsers;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public String getReceiverID (){
        for(String userID:chatRoomUsers){
            User currentUser= UserHandler.getCurrentUser();
            if(!currentUser.getId().equals(userID)){
                return userID;
            }
        }
        return "";
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }
}
