package com.peoplehandstech.helpy.models;


import android.util.Log;

import com.peoplehandstech.helpy.utilites.DATABASE;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {



    private String name;
    private String phoneNumber;
    private String eMail;
    private String howToHelp;
    private String password;
    private Location location;
    private double latitude,longitude;
    private boolean verified;
    private String id;
    private String photo;
    private String gender;
    private String token;
    private String city;
    private int reputation,helped,askedForHelp;
    private  ArrayList<Request> pending =new ArrayList<>();
    private ArrayList<Rating> peopleRated =new ArrayList<>();
    private ArrayList<Friend> friendsList=new ArrayList<>();




    public User()
    {

    }
    public User(String gender,String photo)
    {
        this.gender=gender;
        this.photo=photo;
    }

    public User (String gender,String photo,String name, String phoneNumber, String eMail, String howToHelp, String password, double latitude,double longitude)
    {

        reputation=0;
        helped=0;
        askedForHelp=0;
        verified =false;
        this.gender=gender;
        this.photo=photo;
        this.name=name;
        this.phoneNumber=phoneNumber;
        this.eMail=eMail;
        this.howToHelp=howToHelp;
        this.password=password;
        this.latitude=latitude;
        this.longitude=longitude;

    }



    public boolean searchFriends(String id)
    {


        if(friendsList.size()==0)
        {
            Log.d("USER_CLASS","searchFriends method >> friends list is empty");
            return false;
        }

        for(Friend friend : friendsList)
        {
            if(id.equals(friend.getId()))
            {
                return true;
            }

        }
        return false;
    }

    public boolean searchRequest(String id)
    {
        if(pending.size()==0)
        {
            Log.d("USER_CLASS","myRequest arrayList is empty");
            return false;
        }

        for(Request request : pending)
        {
            if(request.getRequestId().equals(id) && request.getTitle().equals("Accepted"))
            {
                return true;
            }
            else if(request.getRequestId().equals(id) && request.getTitle().equals("New Request"))
            {
                return true;
            }
        }
        return false;
    }

    public String getChatRoomID(String chatUserID){
        String ID1=chatUserID+this.getId();
        String ID2=this.getId()+chatUserID;

        if(friendsList.size()>0){
            for(Friend currentFriend:friendsList)
            {
                String chatRoomID=currentFriend.getChatRoomID();
                if(ID1.equals(chatRoomID) || ID2.equals(chatRoomID))
                {
                    return currentFriend.getChatRoomID();
                }
            }
        }
        return null;
    }



    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String geteMail() {
        return eMail;
    }

    public String getHowToHelp() {
        return howToHelp;
    }

    public String getPassword() {
        return password;
    }

    public Location getLoc() {
        return location;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setId(String id) {
            this.id = id;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public String getId ()
    {
        return this.id;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getReputation() {

        return reputation;
    }
    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public ArrayList<Request> getPending() {
        return pending;
    }

    public void setPending(ArrayList<Request> pending) {
        this.pending = pending;
    }

    public ArrayList<Rating> getPeopleRated() {
        return peopleRated;
    }

    public void setPeopleRated(ArrayList<Rating> peopleRated) {
        this.peopleRated = peopleRated;
    }

    public void addRate (Rating rating)
    {
        this.reputation= reputation+rating.getRate();
        this.setReputation(reputation);

    }

    public int getHelped() {
        return helped;
    }

    public void setHelped(int helped) {
        this.helped = helped;
    }

    public int getAskedForHelp() {
        return askedForHelp;
    }

    public void setAskedForHelp(int askedForHelp) {
        this.askedForHelp = askedForHelp;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public ArrayList<Friend> getFriendsList() {
        return friendsList;
    }

    public void setFriendsList(ArrayList<Friend> friendsList) {
        this.friendsList = friendsList;
    }

    public void setHowToHelp(String howToHelp) {
        this.howToHelp = howToHelp;
    }
    public void setName(String name) {
        this.name = name;
    }
}
