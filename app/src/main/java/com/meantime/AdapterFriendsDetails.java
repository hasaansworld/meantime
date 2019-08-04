package com.meantime;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

public class AdapterFriendsDetails extends RecyclerView.Adapter<AdapterFriendsDetails.ViewHolder> {
    Context context;
    List<Friend> friendsList;

    public AdapterFriendsDetails(Context context, RealmList<Friend> fl){
        this.context = context;
        friendsList = new ArrayList<>(fl);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView profilePicture, imageCreated;
        TextView name, phone;

        public ViewHolder(View v){
            super(v);
            profilePicture = v.findViewById(R.id.profilePicture);
            name = v.findViewById(R.id.name);
            phone = v.findViewById(R.id.phone);
            imageCreated = v.findViewById(R.id.imageCreated);
        }

        @Override
        public void onClick(View v) {

        }
    }

    @NonNull
    @Override
    public AdapterFriendsDetails.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_friend_details, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterFriendsDetails.ViewHolder holder, int position) {
        Friend friend = friendsList.get(position);
        String profilePicPath = friend.getProfilePicPath();
        if(profilePicPath == null || profilePicPath.equals(""))
            holder.profilePicture.setImageResource(R.drawable.profile_picture);
        else
            Glide.with(context).asBitmap().load(friend.getProfilePicPath()).into(holder.profilePicture);
        if(friend.getName() == null)
            holder.name.setText("Unknown");
        else
            holder.name.setText(friend.getName());
        holder.phone.setText(friend.getPhoneNumber());
        holder.imageCreated.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }
}
