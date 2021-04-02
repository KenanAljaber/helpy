package com.peoplehandstech.helpy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

//public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder>  {

    private List<Request> requestList;

    private final MyAdapterListener onClickListener;



    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CircleImageView circleImageView;
        private TextView name,requestMessage,title;
        private RelativeLayout yes,no;
        private WeakReference<MyAdapterListener> listenerRef;
        private LinearLayout buttonsLL;

        public MyViewHolder( View view,MyAdapterListener listner) {
            super(view);
            listenerRef = new WeakReference<>(listner);
            circleImageView=view.findViewById(R.id.request_list_content_imageView);
            name=view.findViewById(R.id.request_list_content_name);
            requestMessage=view.findViewById(R.id.request_list_content_message);
            yes=view.findViewById(R.id.request_list_content_yesButton_RL);
            no=view.findViewById(R.id.request_list_content_noButton_RL);
            title=view.findViewById(R.id.request_list_title);
            buttonsLL=view.findViewById(R.id.request_list_LL_buttons);

            yes.setOnClickListener(this);
            no.setOnClickListener(this);
        }
        public void onClick(View view)
        {
            listenerRef.get().onIconClicked(view,getAdapterPosition());
        }

    }
    public MyRecyclerViewAdapter(List<Request> requestList, MyAdapterListener onClickListener)
    {
        this.requestList = requestList;
        this.onClickListener=onClickListener;
    }



    @Override
    public MyViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.request_list_content, parent, false);
        return new MyViewHolder(itemView,onClickListener);
    }

    @Override
    public void onBindViewHolder( MyViewHolder holder, int position) {
        Request request = requestList.get(position);
        holder.name.setText(request.getName());
        holder.requestMessage.setText(request.getMessage());
        holder.title.setText(request.getTitle());
        Glide.with(holder.itemView.getContext()).load(GetHelpActivity.usersPhotos.get(request.getRequestId())).into(holder.circleImageView);
        if(request.getTitle().contentEquals(holder.itemView.getResources().getText(R.string.accepted)))
        {
            holder.buttonsLL.setVisibility(View.GONE);
            holder.title.setVisibility(View.INVISIBLE);
            holder.requestMessage.setTextSize(15);

        }
        // we use holder to use the get resource function
        if(request.getTitle().contentEquals(holder.itemView.getResources().getText(R.string.request_accepted))
                || request.getTitle().contentEquals("Solicitud acceptada")  )
        {
            holder.buttonsLL.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

}
