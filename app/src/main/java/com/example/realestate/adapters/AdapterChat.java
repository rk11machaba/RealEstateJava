package com.example.realestate.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.realestate.MyUtils;
import com.example.realestate.R;
import com.example.realestate.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.HolderChat>{

    private static final String TAG = "ADAPTER_CHAT_TAG";
    private Context context;
    private ArrayList<ModelChat> chatArrayList;

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private FirebaseUser firebaseUser;

    public AdapterChat(Context context, ArrayList<ModelChat> chatArrayList) {
        this.context = context;
        this.chatArrayList = chatArrayList;

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public HolderChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);

            return new HolderChat(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);

            return new HolderChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderChat holder, int position) {

        ModelChat modelChat = chatArrayList.get(position);

        String message = modelChat.getMessage();
        String messageType = modelChat.getMessageType();
        long timestamp = modelChat.getTimestamp();

        String formattedDate = MyUtils.formatTimeStampDateTime(timestamp);

        if (messageType.equals(MyUtils.MESSAGE_TYPE_TEXT)){
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.imageIv.setVisibility(View.GONE);

            holder.messageTv.setText(message);
        }else {
            holder.messageTv.setVisibility(View.GONE);
            holder.imageIv.setVisibility(View.VISIBLE);

            try {
                Glide.with(context)
                        .load(message)
                        .placeholder(R.drawable.image_grey)
                        .error(R.drawable.broken_image)
                        .into(holder.imageIv);
            } catch (Exception e) {
                Log.e(TAG, "onBindViewHolder: ", e);
            }
        }

        holder.timeTv.setText(formattedDate);
    }

    @Override
    public int getItemCount() {

        return chatArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatArrayList.get(position).getFromUid().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class HolderChat extends RecyclerView.ViewHolder{

        TextView messageTv, timeTv;
        ImageView imageIv;
        public HolderChat(@NonNull View itemView) {
            super(itemView);

            messageTv = itemView.findViewById(R.id.messageTv);
            imageIv = itemView.findViewById(R.id.imageIv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
