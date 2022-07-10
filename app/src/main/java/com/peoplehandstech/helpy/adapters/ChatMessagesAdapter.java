package com.peoplehandstech.helpy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.peoplehandstech.helpy.R;
import com.peoplehandstech.helpy.models.Message;
import com.peoplehandstech.helpy.utilites.UserHandler;

import java.util.ArrayList;
import java.util.List;

public class ChatMessagesAdapter extends RecyclerView.Adapter {

    List<Message> messages;
    Context chatActivity;
    /*
    if ITEM_TYPE > 0
    its mean the message is from current user
    otherwise its from other user
     */
     private int ITEM_TYPE=0;

    public void clear() {
        int size = messages.size();
        messages.clear();
        notifyItemRangeRemoved(0, size);
    }

    public ChatMessagesAdapter(List<Message> messages, Context chatActivity) {

            this.messages=new ArrayList<>();

            this.messages = messages;


        this.chatActivity = chatActivity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType>0){
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.sender_message_layout_,parent,false);
            return new SenderHolder(view);
        }else{
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.reciever_message_layout_,parent,false);
            return new ReceiverHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message=messages.get(position);

        if(holder.getClass()==SenderHolder.class){
            SenderHolder senderHolder=(SenderHolder) holder;
            senderHolder.messageHolder.setText(message.getMessageBody());
            senderHolder.messageTime.setText(message.getDate());

        }else{
            ReceiverHolder receiverHolder=(ReceiverHolder) holder;

            receiverHolder.messageHolder.setText(message.getMessageBody());
            receiverHolder.messageTime.setText(message.getDate());
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message=messages.get(position);
        if(message.getSenderId().equals(UserHandler.getCurrentUser().getId())){
            ITEM_TYPE=1;
            return ITEM_TYPE;
        }
        else
        {
            ITEM_TYPE=-1;
            return ITEM_TYPE;
        }
    }

    class SenderHolder extends RecyclerView.ViewHolder{
        TextView messageHolder,messageTime;
        public SenderHolder(@NonNull View itemView) {
            super(itemView);
            messageHolder=itemView.findViewById(R.id.sender_messageView);
            messageTime=itemView.findViewById(R.id.sender_time);
        }
    }
    class ReceiverHolder extends RecyclerView.ViewHolder{
        TextView messageHolder,messageTime;
        public ReceiverHolder(@NonNull View itemView) {
            super(itemView);
            messageHolder=itemView.findViewById(R.id.receiver_messageView);
            messageTime=itemView.findViewById(R.id.receiver_time);
        }
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        this.notifyDataSetChanged();
    }
}
