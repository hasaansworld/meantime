package com.meantime;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.RealmList;

public class AdapterContacts extends RecyclerView.Adapter<AdapterContacts.ViewHolder> {
    Context context;
    List<Friend> friends;
    String myPhoneNumber;

    public AdapterContacts(Context context){
        this.context = context;
        friends = new ArrayList<>(RealmUtils.getRealm().where(Friend.class).findAll());
        Collections.sort(friends);
        myPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView profilePicture;
        TextView name, phone;
        LinearLayout layout;

        public ViewHolder(View v){
            super(v);
            profilePicture = v.findViewById(R.id.profilePicture);
            name = v.findViewById(R.id.name);
            phone = v.findViewById(R.id.phone);
            layout = v.findViewById(R.id.layout);
            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }

    @NonNull
    @Override
    public AdapterContacts.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_contact_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterContacts.ViewHolder holder, int position) {
        Friend friend = friends.get(position);
        if(friend.getPhoneNumber().equals(myPhoneNumber)){
            SharedPreferences sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
            String profilePath = sharedPreferences.getString("profilePicPath", "");
            if(profilePath.equals(""))
                holder.profilePicture.setImageResource(R.drawable.profile_picture);
            else
                Glide.with(context).asBitmap().load(profilePath).into(holder.profilePicture);
            holder.name.setText("You");
            holder.phone.setText(friend.getPhoneNumber());
        }
        else {
            if (friend.getProfilePicPath() == null || friend.getProfilePicPath().equals("")) {
                holder.profilePicture.setImageResource(R.drawable.profile_picture);
            } else {
                Glide.with(context).asBitmap().load(friend.getProfilePicPath()).into(holder.profilePicture);
            }
            if (friend.getName() == null)
                holder.name.setText("Unknown");
            else
                holder.name.setText(friend.getName());
            holder.phone.setText(friend.getPhoneNumber());
        }
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }
}
