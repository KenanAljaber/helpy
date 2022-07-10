package com.peoplehandstech.helpy.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.adapters.FriendsAdapter;
import com.peoplehandstech.helpy.models.Friend;
import com.peoplehandstech.helpy.models.Message;
import com.peoplehandstech.helpy.models.User;
import com.peoplehandstech.helpy.utilites.DATABASE;
import com.peoplehandstech.helpy.utilites.UserHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class FriendsListActivity extends AppCompatActivity {

    private ArrayList<User>friends;
    private FriendsAdapter adapter;
    private RecyclerView.LayoutManager manager;
    private androidx.recyclerview.widget.RecyclerView recyclerView;
    private RelativeLayout emptyFriendsList;
    private static String TAG="CHAT_LIST_ACTIVITY";
    private ArrayList<Message> lastMessage;
    private static FriendsListActivity activity;
    private static HashMap <String,Integer> friendsIndices=new HashMap<>();
    private static DatabaseReference friendsListRef;
    private ChildEventListener friendsListListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        recyclerView=findViewById(R.id.friendsRecyclerView);
        emptyFriendsList=findViewById(R.id.empty_friendsList);
        lastMessage=new ArrayList<>();
        activity=this;

        friends= getFriendsIdsToUsers(UserHandler.getCurrentUser());
        if(GetHelpActivity.getUnreadMessageVisibility()){
            GetHelpActivity.showUnreadMessage(false);
        }

        //set the last message for each chat

        if(friends.size()==0){
            emptyFriendsList.setVisibility(View.VISIBLE);
        }else{
            emptyFriendsList.setVisibility(View.GONE);
        }
        adapter=new FriendsAdapter(friends,this);
        manager=new LinearLayoutManager(this);

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        adapter.setItems(friends);
        adapter.notifyDataSetChanged();


        friendsListRef= FirebaseDatabase.getInstance().getReference()
                .child(UserHandler.getCurrentUser().getCity()).child(UserHandler.getCurrentUser().getId()).child("Friends");
       friendsListListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                UserHandler.refreshUserFriendsList(UserHandler.getCurrentUser());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                UserHandler.refreshUserFriendsList(UserHandler.getCurrentUser());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                UserHandler.refreshUserFriendsList(UserHandler.getCurrentUser());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        friendsListRef.addChildEventListener(friendsListListener);

    }


    public static FriendsListActivity instance (){
        return activity;
    }


    private ArrayList<User> getFriendsIdsToUsers(User user){
        ArrayList<User> friends=new ArrayList<>();
        Log.d(TAG,"This is the size of the map before the loop  "+ friendsIndices.size());
        for(Friend currentFriend:user.getFriendsList()){

            friends.add(DATABASE.getUser(currentFriend.getId()));
            friendsIndices.put(currentFriend.getId(),friends.size()-1);



        }
        Log.d(TAG,"getFriendsIdsToUsers method >> "+friends.size());
        Log.d(TAG,"This is the size of the map after the loop  "+ friendsIndices.size());
        return friends;
    }
    @Override
    protected void onResume(){
        super.onResume();
        //FriendsAdapter.updateLastMessageHandler();

    }

    public void setFriendTop (String id){
        ArrayList<User> updatedFriends=new ArrayList<>();
        if(!friends.get(0).getId().equals(id)){

           int index=friendsIndices.get(id);
           updatedFriends.add(friends.get(index));

           for(User currentUser:friends){
               if(!currentUser.getId().equals(id)){
                   updatedFriends.add(currentUser);
               }
           }
           friends.clear();
           friends=new ArrayList<>(updatedFriends);

           adapter.setItems(friends);
           recyclerView.setAdapter(adapter);
            Log.d(TAG,"updatedFriends list size is "+ updatedFriends.size()+" The first item is "
                    + updatedFriends.get(0).getId()+" the second item is "+updatedFriends.get(1).getId());
           Log.d(TAG,"friends list size is "+ friends.size()+" The first item is "
                   + friends.get(0).getId()+" the second item is "+friends.get(1).getId());

        }



    }



}
