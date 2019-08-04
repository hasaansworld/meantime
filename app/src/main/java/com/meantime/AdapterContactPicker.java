package com.meantime;

import android.app.Activity;
import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import jagerfield.mobilecontactslibrary.Contact.Contact;
import jagerfield.mobilecontactslibrary.ImportContacts;

public class AdapterContactPicker extends RecyclerView.Adapter<AdapterContactPicker.ViewHolder> {

    Context context;
    List<Friend> contactsList = new ArrayList<>();
    List<Friend> selectedContacts = new ArrayList<>();
    OnContactSelectedListener listener;
    ImportContacts importContacts;
    List<Contact> allContacts;
    List<Friend> alreadySelected;

    Realm realm;

    public AdapterContactPicker(Context context, List<Friend> alreadySelected){
        this.context = context;
        this.alreadySelected = alreadySelected;

        /*importContacts = new ImportContacts((Activity)context);
        allContacts =importContacts.getContacts();

        for(Contact contact: allContacts){
            Friend friend = new Friend(contact.getDisplaydName(), "", "");
            if(contact.getNumbers().size() > 1) {
                friend.setPhoneNumber(contact.getNumbers().get(0).getNormalizedNumber());

                boolean selected = alreadySelected.contains(friend);
                if(!selected) contactsList.add(friend);
            }
        }*/
        realm = RealmUtils.getRealm();

        String myPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        contactsList.addAll(realm.where(Friend.class).notEqualTo("phoneNumber", myPhone).findAll());
        Collections.sort(contactsList, new Comparator<Friend>() {
            @Override
            public int compare(Friend o1, Friend o2) {
                String o1name = o1.getName();
                String o2name = o2.getName();
                if(o1name == null) o1name = "Unknown";
                if(o2name == null) o2name = "Unknown";
                return o1name.compareTo(o2name);
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView profilePicture;
        TextView name;

        public ViewHolder(View v){
            super(v);
            v.setOnClickListener(this);
            profilePicture = v.findViewById(R.id.profilePicture);
            name = v.findViewById(R.id.name);
        }

        @Override
        public void onClick(View v) {
            boolean isSelected = selectedContacts.contains(contactsList.get(getAdapterPosition()));
            if(!isSelected) {
                selectedContacts.add(contactsList.get(getAdapterPosition()));
            }
            else{
                selectedContacts.remove(contactsList.get(getAdapterPosition()));
            }
            notifyItemChanged(getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public AdapterContactPicker.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_item_friend_picker, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterContactPicker.ViewHolder holder, int position) {
        Friend friend = contactsList.get(position);
        holder.name.setText(friend.getName());
        if(selectedContacts.contains(friend)){
            holder.profilePicture.setImageResource(R.drawable.ic_check_circle_green_24dp);
        }
        else{
            if(friend.getProfilePicPath().equals(""))
                holder.profilePicture.setImageResource(R.drawable.profile_picture);
            else
                Glide.with(context).asBitmap().load(friend.getProfilePicPath()).into(holder.profilePicture);
        }
    }

    public void filter(String query){
        contactsList.clear();
        for(Contact contact: allContacts){
            Friend friend = new Friend(contact.getDisplaydName(), "", "");
            if(contact.getNumbers().size() > 1) {
                friend.setPhoneNumber(contact.getNumbers().get(0).getNormalizedNumber());
                boolean selected = alreadySelected.contains(friend);
                if (!selected && query.equals("") || !selected && friend.getName().toLowerCase().contains(query.toLowerCase())) contactsList.add(friend);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public List<Friend> getSelectedContacts(){
        return selectedContacts;
    }
    public void setContactSelectedListener(OnContactSelectedListener listener){
        this.listener = listener;
    }

    public interface OnContactSelectedListener{
        public void onContactSelected(String name);
    }
}
