package com.peoplehandstech.helpy.utilites;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.peoplehandstech.helpy.UsersFetchingCallback;
import com.peoplehandstech.helpy.activities.GetHelpActivity;
import com.peoplehandstech.helpy.adapters.FriendsAdapter;
import com.peoplehandstech.helpy.models.ChatRoom;
import com.peoplehandstech.helpy.models.Friend;
import com.peoplehandstech.helpy.models.Location;
import com.peoplehandstech.helpy.models.Message;
import com.peoplehandstech.helpy.models.Rating;
import com.peoplehandstech.helpy.models.Request;
import com.peoplehandstech.helpy.models.User;
import com.peoplehandstech.helpy.notification.Notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

public class DATABASE {

    private static final String TAG = "DATABASE";
    private static FirebaseDatabase fDatabase = FirebaseDatabase.getInstance();
    private static DatabaseReference databaseRef = fDatabase.getReference();
    private static boolean READY_TO_GO = false;
    private static ArrayList<User> users = new ArrayList<>();
    private static HashMap <String, User> usersMap=new HashMap<>();
    private static HashMap<String, Uri> photos = new HashMap<>();
    private static HashMap<String ,ArrayList<Request>>requestMap=new HashMap<>();
    private static HashMap<String ,ArrayList<Rating>> ratingMap=new HashMap<>();
    private static DatabaseReference messagesRef;
    private static boolean messageFetcherRemoved=false;

    //use this hashMap to map user id with the last message
    private static HashMap<String,Message> lastMessageMap=new HashMap<>();
    private static HashMap<String,ChatRoom> conversations=new HashMap<>();

    private static Handler handler;
    private static boolean USER_LOCATION_READY=false;
    private static String currentUserCityName;
    private static int ERROR_DATA=0;
    public static boolean NOTI_REFRESHED =false;
    public static boolean USERS_REFRESHED =false;
    public static ChildEventListener messagesFetcher;
    /**
     * {@link #TAG} represents the class name tag for debug issues
     * {@link #fDatabase} an instance of the fireBase database class
     * {@link #fDatabase} a reference to the database instance
     * {@link #READY_TO_GO} flag to trace if fetching data from database is done
     * {@link #users} the arrayList that will carry all the users
     * {@link #photos} a hashMap carry all profile photos of users
     */

    /**
     * use this method to check whether {@param email} is already exists
     * @param email the email that you need to check it
     * @param userId the user id who want to use this mail
     * @return
     */
    public static boolean checkIfEmailExists(final String email, String userId) {
        for (User currUser : users) {
            if (currUser.geteMail().equals(email) && !currUser.getId().equals(userId)) {
                return true;
            }
        }
        return false;
    }


    private static void fillUserNotifications(Iterable<DataSnapshot> children, User user, Notification type) {

        Notification notification;
        boolean isRequest=false;
        //here we check whether the argument is type of request or rate and we initialize it
        if (type instanceof Request)
        {
            notification=new Request();
            isRequest=true;
        }
        else {
            notification=new Rating();
        }
        for (DataSnapshot child : children) {
            notification = child.getValue(notification.getClass());
            if (notification != null) {
                if(isRequest){
                    Request request= (Request) notification;
                    requestMap.get(user.getId()).add(request);
                }else{
                    Rating request= (Rating) notification;
                    ratingMap.get(user.getId()).add(request);
                }
            }
        }
        setUserNotificationsData(isRequest,user);

    }
    private static void setUserNotificationsData(boolean isRequest, User user){
        if(isRequest)
        {
            user.setPending(requestMap.get(user.getId()));
            NOTI_REFRESHED =true;
        }else
        {
            user.setPeopleRated(ratingMap.get(user.getId()));
        }
    }


    public static void refreshUsers(final User currentUser){
        USERS_REFRESHED =false;
        ArrayList<User> refreshedUsers=new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child(currentUser.getCity())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    User user= snapshot.getValue(User.class);
                    refreshedUsers.add(user);
                }
                Collections.copy(users,refreshedUsers);
                USERS_REFRESHED=true;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void refreshNotifications(final User user){
        NOTI_REFRESHED =false;
        FirebaseDatabase.getInstance().getReference().child(user.getCity())
                        .child(user.getId()).child(Request.TAG).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Request> userRequests=new ArrayList<>();
                Iterable<DataSnapshot> notifications = snapshot.getChildren();
                for (DataSnapshot child : notifications) {
                    Request request = child.getValue(Request.class);
                    if (request != null) {
                        userRequests.add(request);

                    }
                }
                requestMap.replace(user.getId(),userRequests);
                setUserNotificationsData(true,user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    //this code is temporal, once the database is updated should be deleted
     static void temporallySetUser (String id,Context context, UsersFetchingCallback userCallback){
        Toast.makeText(context,"This may take few minutes for the first time\n please wait..",Toast.LENGTH_LONG).show();
       FirebaseDatabase.getInstance().getReference().child("User").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
           @Override
           public void onComplete(@NonNull Task<DataSnapshot> task) {
               if (!task.isSuccessful()) {
                   Log.e(TAG, "temporallySetUser >>> Error getting data", task.getException());
               }
               else {
                   Log.d(TAG,"temporallySetUser method >>> " +String.valueOf(task.getResult().getValue()));
                   User user=task.getResult().getValue(User.class);
                   if(user!=null){
                       try {
                           String location=Location.getCityName(user.getLongitude(),user.getLatitude(),context);
                           user.setCity(location);
                           user.setFriendsList(new ArrayList<Friend>());
                           user.setVerified(true);
                           Log.d(TAG,"temporallySetUser method >>> " +location);
                           addUserToLocationNode(user,userCallback);
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }else{
                       Log.d(TAG,"temporallySetUser method >>> user is null method will break");
                       return;
                   }


               }
           }
       });
    }

    //this code is temporal, once the database is updated should be deleted
    private static void addUserToLocationNode(User user,UsersFetchingCallback userCallback){
        FirebaseDatabase.getInstance().getReference().child(user.getCity())
                .child(user.getId())
                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseDatabase.getInstance().getReference().child("Users Locations")
                        .child(user.getId())
                        .setValue(user.getCity()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        deleteUserFromOldNode(user.getId());
                        UserHandler.setCurrentUser(user);
                        setUsers(user.getCity(),userCallback);

                    }
                });

            }
        });
    }


    private static void deleteUserFromOldNode(String id){
        FirebaseDatabase.getInstance().getReference().child("User").child(id).removeValue();
    }

    /**
     * what this method does is
     * first get all users and save them in {@link #users} arrayList so we can access it anytime
     * second get all notifications related for each user and set them in a HashMap <String userID ,ArrayList<Request>> and then add for each user his arrayList
     * third in the same way we done the notifications we also do it with ratings
     * finally call getPhotosFromDatabase to get all users photos
     */


    public static void setUsers(String cityName,UsersFetchingCallback userCallback) {

        Log.d(TAG,"setUsers method >> setUsers(cityName) called");

        DatabaseReference countryNode = FirebaseDatabase.getInstance().getReference().child(cityName);

        countryNode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                photos.clear();
                Log.d(TAG,"setUsers method >> on data change");
                long count = dataSnapshot.getChildrenCount();
                User user = new User();
                Log.d(TAG,"setUsers method >> Before Looping");
                Log.d(TAG,"setUsers method >>  children count "+count);
                HashMap<String,Object> test=new HashMap<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d(TAG,"setUsers method >> looping");
                    if (child != null) {
                        //add users to users arrayList

                        user=child.getValue(User.class);

                        //user=fillUserInfo(child);
                        if(user!=null &&user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            UserHandler.setCurrentUser(user);
                        }
                        users.add(user);
                        usersMap.put(user.getId(),user);
                        //create arrayList and hashMaps for both requests and rates
                        ArrayList<Request> requestArrayList = new ArrayList<>();
                        ArrayList<Rating> ratingArrayList = new ArrayList<>();
                        ratingMap.put(user.getId(),ratingArrayList);
                        requestMap.put(user.getId(), requestArrayList);
                    }

                        if(FirebaseAuth.getInstance().getCurrentUser()!=null && user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            //add friendsList and chatRoomsList
                            Iterable<DataSnapshot> friendsList = child.child("Friends").getChildren();
                            addFriends( friendsList, user);
                           /* Iterable<DataSnapshot> chatRooms = child.child("chatRooms").getChildren();
                            addChatRooms(chatRooms,user);*/
                        }

                    Iterable<DataSnapshot> notificationKids = child.child("Notifications").getChildren();
                    fillUserNotifications(notificationKids, user, new Request());

//                    Iterable<DataSnapshot> pendingNotifications = child.child("Pending").getChildren();
//                    fillUserNotifications(pendingNotifications, user, new Request());

                    Iterable<DataSnapshot> ratingKids = child.child("my rates").getChildren();
                    fillUserNotifications(ratingKids,user,new Rating());

                    if (requestMap.get(user.getId()) == null) {
                        System.out.println(TAG + " request map is null");
                    } else {

                        RequestHandler.setUsersRequest(requestMap);
                        RequestHandler.setUsersRates(ratingMap);
                    }
                }
                if (users.size() > 0) {
                    GetHelpActivity.setAllUsers(users);
                    getPhotosfromDatabase(count,userCallback);
                    countryNode.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG,"CANCELLED");
                throw databaseError.toException(); //Don't ignore errors
            }
        });
    }




    private  static void addFriends(Iterable<DataSnapshot> dataSnapshot,User user){
        ArrayList<Friend> friends=new ArrayList<>();
            for(DataSnapshot child: dataSnapshot){
                Friend friendId=child.getValue(Friend.class);
                friends.add(friendId);
            }
            user.setFriendsList(friends);
            getFriendsConversations(friends);


    }

    private static void getFriendsConversations(ArrayList<Friend> friends) {

        for(Friend currentFriend:friends){

            FirebaseDatabase.getInstance().getReference().child("chatRooms").child(currentFriend.getChatRoomID()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Log.d(TAG,"getFriendsConversations method >>> snapshot is correct");
                        ArrayList<Message> chatRoomsMessages=new ArrayList<>();
                        ArrayList<String> chatRoomUsers=new ArrayList<>();
                        HashMap <String,Object> chatRoomMap=new HashMap<>();


                        ChatRoom currentChatRoom;
                        chatRoomMap= (HashMap<String, Object>) snapshot.getValue();
                        if(chatRoomMap==null){
                            currentChatRoom=new ChatRoom("empty");
                            conversations.put(currentFriend.getId(),currentChatRoom);
                            Log.d(TAG,"chat room is null");

                        }else{
                            Log.d(TAG,"chat room is not null");
                            chatRoomUsers= (ArrayList<String>) chatRoomMap.get("chatRoomUsers");

                            String currentChatRoomID=(String)chatRoomMap.get("roomID");
                            currentChatRoom=new ChatRoom(currentChatRoomID);
                            currentChatRoom.setChatRoomUsers(chatRoomUsers);
                            ArrayList<String> messagesTest=new ArrayList<>();



                            //get the messages hashmap and loop through it to get messages
                            if(chatRoomMap.containsKey("messages")){

                                fetchMessages(currentChatRoom);


                            }else{
                                currentChatRoom.setMessages(new ArrayList<>());
                            }
                            conversations.put(currentFriend.getId(),currentChatRoom);

                            Log.d(TAG,"getFriendsConversations method >>> chatRoom added");




                        }




                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }



    }

    private static void fetchMessages(ChatRoom currentChatRoom) {
        ArrayList<Message> chatRoomsMessages=new ArrayList<>();
         messagesRef= FirebaseDatabase.getInstance().getReference().child("chatRooms").
                child(currentChatRoom.getRoomID()).child("messages");
        messagesFetcher=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //this flag will make sure that this code below will not be executed  when messageFetcher is removed.
                if(!messageFetcherRemoved){
                    Log.d(TAG,"From the on child added tester the id is "+snapshot.getKey());
                     Message currentMessage=snapshot.getValue(Message.class);
                     Message newMessage=new Message(currentMessage.getSenderId(),currentMessage.getMessageBody(),currentMessage.getDate());
                     MessagesHandler.addMessage(snapshot.getKey(),newMessage);
                     chatRoomsMessages.add(newMessage);
                     currentChatRoom.setMessages(chatRoomsMessages);
                }
                //messagesTest.add(newMessage.getMessageBody());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };


        messagesRef.addChildEventListener(messagesFetcher);




    }
    public static void removeMessageListener (){
        if(!messageFetcherRemoved && messagesRef!=null ){
            messagesRef.removeEventListener(messagesFetcher);
            messageFetcherRemoved=true;
            Log.d(TAG,"removeMessageListener method >> child event listener has been removed");
        }

    }

    /**
     * this method fetches all users photos from database
     * @param children the number of users in database so we make sure that number of photos has a match with number of users
     */
    private static void getPhotosfromDatabase(final long children, UsersFetchingCallback userCallback) {
        Log.d(TAG,"Getting photos from database storage");
        if (users != null) {
            for (final User user : users) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images").child(user.getId());
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        photos.put(user.getId(), uri);
                        if (photos.size() == children) {
                                GetHelpActivity.setUsersPhotos(photos);
                                System.out.println(TAG+" number of photos from database "+String.valueOf(photos.size()));
                                getLastMessage(UserHandler.getCurrentUser());
                                setReadyToGo(true);
                                userCallback.onUsersSet();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"This ID "+user.getId()+ " Has no photo in the database storage");
                        setDefaultPhoto(user,children);

                        Log.d(TAG,"Failed fetching photos ");
                        Log.d(TAG,"This is the exception message "+e.getMessage());

                    }
                });

            }
        }
    }

    private static void setDefaultPhoto(User user,final long children) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images").child("default.jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                photos.put(user.getId(), uri);
                Log.d(TAG,"setDefaultPhoto >>>> Default photo has been set successfully!");
                if (photos.size() == children) {
                    GetHelpActivity.setUsersPhotos(photos);
                    System.out.println(TAG+" number of photos from database "+String.valueOf(photos.size()));
                    getLastMessage(UserHandler.getCurrentUser());
                    setReadyToGo(true);
                }

            }
        });
    }


    public static void getUserLocation(String userId, final Context context, UsersFetchingCallback userCallback){
        handler=new Handler(Looper.getMainLooper());
      DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Users Locations").child(userId);
      Log.d(TAG,"logging user id is "+userId );
      ref.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
          @Override
          public void onComplete(@NonNull Task<DataSnapshot> task) {
              if(task.isSuccessful()){
                  if(task.getResult().getValue()!=null){
                      Log.d(TAG,"getUserLocation method >>> this is the city idiot "+task.getResult().getValue().toString());
                      USER_LOCATION_READY=true;
                      currentUserCityName=task.getResult().getValue().toString();
                      userCallback.onLocationReady(currentUserCityName);
//                     setUsers(currentUserCityName);
                  }else{
                      Log.d(TAG,task.getResult().toString());
                      USER_LOCATION_READY=true;
                      userCallback.onLocationNull();
                      temporallySetUser(userId,context,userCallback);
                        Log.d(TAG,"getUserLocation method >>> location is null");
                  }



              }else{

                  Log.d(TAG,"getUserLocation method >>> error retrieving data from database ");
                  ERROR_DATA=-1;

              }
          }
      });
      Runnable checkUserLocationProcess=new Runnable() {
          @Override
          public void run() {
              if(ERROR_DATA<0){
                  handler.removeCallbacks(this);
                  ERROR_DATA=0;
                  return;
              }
              if(!USER_LOCATION_READY){
                  handler.postDelayed(this,1000);
              }
          }
      };
      handler.postDelayed(checkUserLocationProcess,1000);


    }

    public static ArrayList<User> getUsers() {
        return users;
    }


    //get User by id
    public static User getUser(String id) {
        if (users != null) {
            for (User user : users) {
                if (user.getId().equals(id)) {
                    return user;
                }
            }

        }
        return null;
    }

    public static HashMap<String, Uri> getPhotos() {
        if (photos.size() > 0) {
            return photos;
        }
        return null;

    }

    public static void updateUserPhoto(String id, Uri uri) {
        if (photos.containsKey(id)) {
            photos.put(id, uri);
        }
    }

    public static void setPhotos(HashMap<String, Uri> photos) {
        DATABASE.photos = photos;
    }


    public static boolean isReadyToGo() {
        return READY_TO_GO;
    }

    public static void setReadyToGo(boolean readyToGo) {
        READY_TO_GO = readyToGo;
    }

    /**
     * @return object of fireBase user of the current user that is signed in.
     */


    public static FirebaseUser getFUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static void getLastMessage(User currentUser){

        if(currentUser.getFriendsList()!=null){



        for(Friend friend:currentUser.getFriendsList()){
            Log.d(TAG,"setLastMessageToUser method >>> chatRoom id is "+friend.getChatRoomID());
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
            Query lastQuery = databaseReference.child("chatRooms").child(friend.getChatRoomID()).child("messages").orderByKey().limitToLast(1);
            lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for(DataSnapshot children:dataSnapshot.getChildren()){

                        Message message=children.getValue(Message.class);
                        lastMessageMap.put(friend.getId(),message);
                        Log.d(TAG,"setLastMessageToUser method >>> message is "+message.getMessageBody());
                    }

                    Log.d(TAG,"setLastMessageToUser method >>> currentUser name "+currentUser.getName());

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle possible errors.
                }
            });
        }
        }else{
            currentUser.setFriendsList(new ArrayList<Friend>());
        }

    }

    public static void updateLastMessageInFreindsAdapter (User currUser){
        for(Friend friend:currUser.getFriendsList()){
            Log.d(TAG,"setLastMessageToUser method >>> chatRoom id is "+friend.getChatRoomID());
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
            Query lastQuery = databaseReference.child("chatRooms").child(friend.getChatRoomID()).child("messages").orderByKey().limitToLast(1);
            lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for(DataSnapshot children:dataSnapshot.getChildren()){

                        Message message=children.getValue(Message.class);
                        lastMessageMap.put(friend.getId(),message);
                        Log.d(TAG,"setLastMessageToUser method >>> message is "+message.getMessageBody());

                    }
                    FriendsAdapter.updateLastMessageHandler();
                    Log.d(TAG,"setLastMessageToUser method >>> currentUser name "+currUser.getName());

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle possible errors.
                }
            });
        }
    }

    public static void listenToNewPhotos (){
        FirebaseDatabase.getInstance().getReference().child("PhotoChanges").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String userID= (String) snapshot.getValue();
                if(DATABASE.usersMap.containsKey(userID)){
                    refreashPhoto(userID);
                    deletePhotoUpdate(userID);
                }

                Log.d(TAG,"This is the user who changed his img"+userID);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String test= (String) snapshot.getValue();
                Log.d(TAG,"This is the user who updated his img"+test);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private static void deletePhotoUpdate(String userID) {
        FirebaseDatabase.getInstance().getReference().child("PhotoChanges").child(userID).removeValue();
    }

    private static void refreashPhoto(String userID) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images").child(userID);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                photos.put(userID, uri);
                GetHelpActivity.setUsersPhotos(photos);
                GetHelpActivity.updateMarkerPhoto(userID,uri);
            }
        });
    }

    private static ArrayList<User> getFriendsIdsToUsers(User user){
        ArrayList<User> friends=new ArrayList<>();
        for(Friend currentFriend:user.getFriendsList()){

            friends.add(DATABASE.getUser(currentFriend.getId()));
        }
        Log.d(TAG,"getFriendsIdsToUsers method >> "+friends.size());
        return friends;
    }

    public static HashMap<String, Message> getLastMessageMap() {
        return lastMessageMap;
    }

    public static void setLastMessageMap(HashMap<String, Message> lastMessageMap) {
        DATABASE.lastMessageMap = lastMessageMap;
    }

    public static HashMap<String, ChatRoom> getConversations() {
        return conversations;
    }

    public static void setConversations(HashMap<String, ChatRoom> conversations) {
        DATABASE.conversations = conversations;
    }


}
