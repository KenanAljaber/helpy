package com.peoplehandstech.helpy.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.adapters.ChatMessagesAdapter;
import com.peoplehandstech.helpy.adapters.FriendsAdapter;
import com.peoplehandstech.helpy.models.ChatRoom;
import com.peoplehandstech.helpy.models.Message;
import com.peoplehandstech.helpy.models.NotificationFCM;
import com.peoplehandstech.helpy.models.User;
import com.peoplehandstech.helpy.notification.FirebaseNotificationHandler;
import com.peoplehandstech.helpy.utilites.DATABASE;
import com.peoplehandstech.helpy.utilites.MessagesHandler;
import com.peoplehandstech.helpy.utilites.UserHandler;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private CircleImageView chatHeaderImageView;
    private TextView chatHeaderTitle;
    private EditText messageEditText;
    private RelativeLayout sendMessageButton;
    private User chatUser,currentUser;
    private static String TAG="CHAT_ACTIVITY";
    private static  ChatRoom currentChatRoom;
    private Handler handler;
    private Runnable getChatRoom;
    private RecyclerView chatRecyclerView;
    private ChatMessagesAdapter adapter;
    private LinearLayoutManager manager;
    public  static  boolean userIsHere=false;
    private MediaPlayer myMessageSentRing,messageReceived;
    private static HashMap<String, Message> newMessages;
    private static FriendsListActivity friendsListActivity;
    private static ChildEventListener messagesListener;
    private static DatabaseReference messagesRef;




    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userIsHere=true;
        chatHeaderImageView=findViewById(R.id.chatHeaderImage);
        chatHeaderTitle=findViewById(R.id.chatHeaderName);
        messageEditText =findViewById(R.id.chatMessage);
        sendMessageButton=findViewById(R.id.sendMessageChat);
        chatRecyclerView =findViewById(R.id.chat_recycleView);
        myMessageSentRing =MediaPlayer.create(this,R.raw.message_sent);
        messageReceived=MediaPlayer.create(this,R.raw.message_received);
        //setting the recyclerView and its adapter and layout manager
        manager= new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        adapter=new ChatMessagesAdapter(new ArrayList<>(),this);
        friendsListActivity=FriendsListActivity.instance();
        chatRecyclerView.setLayoutManager(manager);
        chatRecyclerView.setAdapter(adapter);
        newMessages=new HashMap<>();
        //get the current user
        currentUser= UserHandler.getCurrentUser();
        adapter.clear();

        //make sure the keyboard only pops up when a user clicks into an EditText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);



        handler=new Handler();
        //in case chat room is not downloaded yet we wait another second for it
        getChatRoom=new Runnable() {
            @Override
            public void run() {
                if(currentChatRoom==null){
                    handler.postDelayed(this,1000);
                }
            }
        };


        if(UserHandler.getChatUser()!=null){
            chatUser= UserHandler.getChatUser();
           // Log.d(TAG,String.valueOf(DATABASE.getConversations().size()));
//            currentChatRoom.setMessages(new ArrayList<>());

            currentChatRoom=DATABASE.getConversations().get(chatUser.getId());
            Log.d(TAG,"The size of the messages list TOP is "+currentChatRoom.getMessages().size());
            if(currentChatRoom!=null && currentChatRoom.getMessages()!=null){
                adapter.setMessages(new ArrayList<>());
                adapter.setMessages(currentChatRoom.getMessages());
                Log.d(TAG,"The size of the adapter message list is "+adapter.getItemCount());
                Log.d(TAG,"currentChatRoom is not null");
            }else{
                currentChatRoom.setMessages(new ArrayList<>());
                adapter.setMessages(currentChatRoom.getMessages());
            }

//            String chatRoomID=currentUser.getChatRoomID(chatUser.getId());
//
//            setChatRoom(chatRoomID);
            Glide.with(this).load(GetHelpActivity.usersPhotos.get(chatUser.getId())).into(chatHeaderImageView);
            chatHeaderTitle.setText(chatUser.getName());


        }else{
            Log.d(TAG,"onCreate method >>> chatUser is null");
            this.finish();
        }




        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(messageEditText.getText()))
                {

                }else{
                    String messageBody=messageEditText.getText().toString();
                    //DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("chatRooms").child(currentChatRoom.getRoomID()).child("messages").push();

                    Message message=createMessage(messageBody);
                    sendMessage(message);
                    messageEditText.setText("");
                }
            }
        });

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = adapter.getItemCount();
                Log.d(TAG,"onItemRangeInserted method >>> adapter count "+ friendlyMessageCount);
                int lastVisiblePosition =
                        manager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    chatRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        messagesRef= FirebaseDatabase.getInstance().getReference().child("chatRooms").child(currentChatRoom.getRoomID()).child("messages");
        messagesListener =new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Message message=snapshot.getValue(Message.class);
                String messageID=snapshot.getKey();
                String senderID= message.getSenderId();
                Log.d(TAG,"Message id is "+ messageID+" message body is "+message.getMessageBody());
                boolean couldBeAdded=MessagesHandler.addMessage(messageID,message);
                Log.d(TAG,"could be added "+couldBeAdded);
                if(couldBeAdded){
                    Log.d(TAG,"Message added "+message.getMessageBody());
                    currentChatRoom.addMessage(message);
                    adapter.setMessages(currentChatRoom.getMessages());
                    updateLastMessage(currentChatRoom,"sendMessage if",message);
                    FriendsAdapter.updateLastMessageHandler();

                    adapter.notifyDataSetChanged();
                    friendsListActivity.setFriendTop(chatUser.getId());

                    Log.d(TAG,"Current chat user id is "+chatUser.getId());
                }
                else{
                    Log.d(TAG,"the message exist is "+MessagesHandler.getMessages().get(messageID).getMessageBody());
                }
                chatRecyclerView.scrollToPosition(currentChatRoom.getMessages().size()-1);
                        Log.d(TAG,"listenTomessages method >>> onChildAdded "+message.getMessageBody()+" is seen "+message.isSeen());



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

        messagesRef.addChildEventListener(messagesListener);


        if(adapter.getItemCount()>0){
            chatRecyclerView.smoothScrollToPosition( chatRecyclerView.getAdapter().getItemCount() - 1);
        }

        chatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if ( bottom < oldBottom) {
                    if(adapter.getItemCount()>0){


                    chatRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            chatRecyclerView.smoothScrollToPosition( chatRecyclerView.getAdapter().getItemCount() - 1);
                        }
                    }, 100);
                } else if (bottom == oldBottom){
                    chatRecyclerView.smoothScrollToPosition( chatRecyclerView.getAdapter().getItemCount() - 1);
                }
                }
            }
        });



    }



    private void playMessageSound(Message message) {
        if (message.getSenderId().equals(currentUser.getId())){
            myMessageSentRing.start();
        }else{
            messageReceived.start();
        }
    }


    private Message createMessage(String messageBody){
        Calendar calendar = Calendar.getInstance();
        int minutes = calendar.get(Calendar.MINUTE);
        int hour24hrs = calendar.get(Calendar.HOUR_OF_DAY);
        if(minutes<10 && hour24hrs<10 ){
            return new Message(currentUser.getId(),messageBody,hour24hrs + ":" + "0"+minutes);
        }
        return new Message(currentUser.getId(),messageBody,hour24hrs + ":" + minutes);
    }

    private void sendMessage(Message message){

        //make sure that current chat room is not null
        if(currentChatRoom==null){
            handler.postDelayed(getChatRoom,1000);
        }else{

            //currentChatRoom.getMessages().clear();
            //add message to the chat room for both currentUser and chatUser
            if(currentChatRoom.getMessages()!=null){
                Log.d(TAG,"sendMessage method >>> currentChatRoom is not null");
                playMessageSound(message);

            }else{
                ArrayList<Message> messages=new ArrayList<>();
                messages.add(message);
                currentChatRoom.setMessages(messages);
                adapter.setMessages(messages);
                updateLastMessage(currentChatRoom,"sendMessage else",message);
                FriendsAdapter.updateLastMessageHandler();
            }

            //send the message
            try {
                pushMessage(message);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }



    private void updateLastMessage (ChatRoom chatRoom,String from,Message message){
        HashMap<String,Message> lastMessages=DATABASE.getLastMessageMap();
        if(chatRoom.getMessageSize()>0){
            lastMessages.put(chatUser.getId(),chatRoom.getMessages().get(chatRoom.getMessageSize()-1));
            DATABASE.setLastMessageMap(lastMessages);
            Log.d(TAG,"called from "+from+" last message is "+message.getMessageBody());
        }


    }


    private void pushMessage(Message message) throws FileNotFoundException {
        //update the message list in the lastMessage hashmap in DATABASE class and call the updateLastMessageHandler() method to
        //update the last message
      /*  updateLastMessage(chatRoom);
        FriendsAdapter.updateLastMessageHandler();*/

       DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("chatRooms")
               .child(currentChatRoom.getRoomID()).child("messages").push();
       ref.setValue(message);
        FirebaseDatabase.getInstance().getReference().child("chatRooms")
                .child(currentChatRoom.getRoomID()).child("messageSize").setValue(currentChatRoom.getMessages().size());
        //    MessagesHandler.addMessage(ref.getKey(),message);

        //send notification to chat user
        NotificationFCM notificationFCM=new NotificationFCM(chatUser.getName(),"You have a new message!",currentUser.getName(),chatUser.getToken());
        JSONObject json=FirebaseNotificationHandler.createJSONObject(notificationFCM,currentUser,chatUser,"",Message.MESSAGE_TAG);
        FirebaseNotificationHandler.sendNotification(json,this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        userIsHere=false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        userIsHere=false;
    }

    public static ChatRoom getCurrentChatRoom() {
        return currentChatRoom;
    }

    public static boolean isUserIsHere() {
        return userIsHere;
    }
}
