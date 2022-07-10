package com.peoplehandstech.helpy.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.activities.ChatActivity;
import com.peoplehandstech.helpy.activities.FriendsListActivity;
import com.peoplehandstech.helpy.activities.GetHelpActivity;
import com.peoplehandstech.helpy.models.Message;
import com.peoplehandstech.helpy.models.User;
import com.peoplehandstech.helpy.utilites.DATABASE;
import com.peoplehandstech.helpy.utilites.UserHandler;

import java.util.ArrayList;


import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> {

    private static Runnable updateLastMessage;
    private static Handler handler;
    ArrayList<User> friends;
    Context chatListActivity;

    private static String TAG="FRIENDS_ADAPTER";


    public void setItems(ArrayList<User> persons) {
        this.friends = persons;
    }

    public FriendsAdapter(ArrayList<User> friends, Context chatListActivity) {
        this.friends = friends;
        this.chatListActivity = chatListActivity;

    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_row_layout,parent,false);
        return new FriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
        User user=friends.get(position);
        Log.d(TAG,String.valueOf(getItemCount()));

        if(DATABASE.getLastMessageMap().get(user.getId())!=null){
            Message message= DATABASE.getLastMessageMap().get(user.getId());
            holder.lastMessage.setText(message.getMessageBody());
            holder.lastMessageTime.setText(message.getDate());
            Log.d(TAG,"onBindViewHolder method >>> message body is "+message.getMessageBody());

        }else{
            holder.lastMessage.setText("Message");
        }


        Log.d(TAG,"OnBindViewHolder method >>> "+user.getName());
        holder.friendsName.setText(user.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLastMessage=new Runnable() {
                    @Override
                    public void run() {
                        if(DATABASE.getLastMessageMap().get(user.getId())!=null){
                            Log.d(TAG,"onBindViewHolder method >>> user name is from runnable updateLastMessage "+user.getName());
                            Message message= DATABASE.getLastMessageMap().get(user.getId());
                            holder.lastMessage.setText(message.getMessageBody());
                            Log.d(TAG,"onBindViewHolder method >>> message body is "+message.getMessageBody());
                            holder.lastMessageTime.setText(message.getDate());
                        }else{
                            holder.lastMessage.setText("Message");
                        }
                    }
                };
                Log.d(TAG,"OnBindViewHolder method >>> clicked "+ user.getName());
                UserHandler.setChatUser(user);
                Intent i=new Intent(chatListActivity, ChatActivity.class);
                chatListActivity.startActivity(i);

            }
        });
        Glide.with(holder.itemView.getContext()).load(GetHelpActivity.usersPhotos.get(user.getId())).into(holder.friendImageView);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder {

        TextView friendsName,lastMessage,lastMessageTime;
        CircleImageView friendImageView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            friendsName= itemView.findViewById(R.id.friendNameTextView);
            lastMessage= itemView.findViewById(R.id.lastMessageTextView);
            friendImageView= itemView.findViewById(R.id.friendCircleImageView);
            lastMessageTime=itemView.findViewById(R.id.messageTimeTextView);
        }
    }

    public static void updateLastMessageHandler (){
        handler=new Handler();
        handler.postDelayed(updateLastMessage,1000);
    }
}
