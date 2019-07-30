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

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class AdapterFriends extends RecyclerView.Adapter<AdapterFriends.ViewHolder> {
    Context context;
    Realm realm;
    List<Friend> allFriends = new ArrayList<>();

    public AdapterFriends(Context context){
        this.context = context;
        Realm.init(context);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("database")
                .schemaVersion(2)
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        realm = Realm.getDefaultInstance();
        List<Friend> friends = realm.where(Friend.class).findAll();
        for(Friend friend: friends){
            if(!allFriends.contains(friend)){
                allFriends.add(friend);
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView profilePicture;
        TextView name, lastMessage, messageCount;

        public ViewHolder(View v){
            super(v);
            profilePicture = v.findViewById(R.id.profilePicture);
            name = v.findViewById(R.id.name);
            lastMessage = v.findViewById(R.id.lastMessage);
            messageCount = v.findViewById(R.id.messageCount);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_friend_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Friend friend = allFriends.get(position);
        if(friend.getProfilePicPath().equals("")){
            holder.profilePicture.setImageResource(R.drawable.profile_picture);
        }
        else{
            Glide.with(context).asBitmap().load(friend.getProfilePicPath()).into(holder.profilePicture);
        }
        holder.name.setText(friend.getName());
    }

    @Override
    public int getItemCount() {
        return allFriends.size();
    }

}
