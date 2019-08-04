package com.meantime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.format.DateFormat;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;

public class TrashAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<Task> list;

    Realm realm;
    int[] colors = {R.color.orange, R.color.green, R.color.red};
    Resources resources;
    int filterPosition = 0;

    private String dateToday, dateYesterday, dateTomorrow;
    String searchQuery = "";

    public TrashAdapter(Context context) {
        this.context = context;
        resources = context.getResources();
        realm = RealmUtils.getRealm();

        Calendar calendar = Calendar.getInstance();
        dateToday = DateFormat.format("dd MMM yyyy", calendar).toString();
        calendar.add(Calendar.DATE, -1);
        dateYesterday = DateFormat.format("dd MMM yyyy", calendar).toString();
        calendar.add(Calendar.DATE, 2);
        dateTomorrow = DateFormat.format("dd MMM yyyy", calendar).toString();


        initialize();
    }

    public void deleteAll() {
        realm.beginTransaction();
        for(int i = 0; i < list.size(); i++){
            Task task = list.get(i);
            task.deleteFromRealm();
        }
        realm.commitTransaction();
        list = new ArrayList<>();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        ImageView imageColor, imageLocation, profilePicture;
        TextView title, time, date, description;
        LinearLayout background1, background2, foreground;

        public ViewHolder(View v) {
            super(v);
            layout = v.findViewById(R.id.layout);
            imageColor = v.findViewById(R.id.colorImage);
            imageLocation = v.findViewById(R.id.locationImage);
            profilePicture = v.findViewById(R.id.profilePicture);
            title = v.findViewById(R.id.title);
            time = v.findViewById(R.id.time);
            date = v.findViewById(R.id.date);
            description = v.findViewById(R.id.description);

            background1 = v.findViewById(R.id.background1);
            background2 = v.findViewById(R.id.background2);
            foreground = v.findViewById(R.id.foreground);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_trash, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        Task task = list.get(position);
        if (h instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) h;
            holder.title.setText(task.getTitle());
            if (task.getDescription().equals(""))
                holder.description.setText("No details yet.");
            else
                holder.description.setText(task.getDescription());
            holder.time.setText(task.getTime());

            if (task.getLocation().equals(""))
                holder.imageLocation.setVisibility(View.GONE);
            else
                holder.imageLocation.setVisibility(View.VISIBLE);

            holder.layout.setVisibility(View.VISIBLE);
            if (position == 0 || !task.getDate().equals(list.get(position - 1).getDate())) {
                holder.date.setVisibility(View.VISIBLE);
                if (task.getDate().equals(dateToday) && filterPosition <= 4)
                    holder.date.setText("Today");
                else if (task.getDate().equals(dateYesterday) && filterPosition <= 4)
                    holder.date.setText("Yesterday");
                else if (task.getDate().equals(dateTomorrow) && filterPosition <= 4)
                    holder.date.setText("Tomorrow");
                else
                    holder.date.setText(task.getDate());
            } else {
                holder.date.setVisibility(View.GONE);
            }


        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    void removeItem(int position, boolean left) {
        Task task = list.get(position);
        if(left) {
            DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("trash").child(Long.toString(task.getTimeInMillis()));
            taskRef.removeValue();
        }
        else{
            DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("tasks").child(Long.toString(task.getTimeInMillis()));
            taskRef.child("isInTrash").setValue(false);
        }

        if (task != null) {
            realm.beginTransaction();
            if(left) {
                task.deleteFromRealm();
            }
            else{
                task.setIsInTrash(false);
            }
            realm.commitTransaction();
            if(!left){
                String dateToday = DateFormat.format("dd MMM yyyy", Calendar.getInstance()).toString();
                if(task.getDate().equals(dateToday)){
                    AlarmReceiver.scheduleTask(context, task, (AlarmManager)context.getSystemService(Context.ALARM_SERVICE));
                }
            }
        }
        list.remove(position);
        notifyItemRemoved(position);
        if (position != list.size())
            notifyItemChanged(position);
    }

    void initialize() {
        List<Task> allTasks = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long timeInMillis = calendar.getTimeInMillis();
        allTasks.addAll(realm.where(Task.class).equalTo("isInTrash", true).equalTo("creator", "You").greaterThanOrEqualTo("timeInMillis", timeInMillis).findAll());
        Collections.sort(allTasks);
        list = new ArrayList<>(allTasks);
    }

    /*void setSearchQuery(String query) {
        searchQuery = query;
        list = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Task task = list.get(i);
            boolean matchesQuery =
                    task.getTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            task.getDescription().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            task.getDate().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            task.getTime().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            task.getDate().equals(dateToday) && "today".contains(searchQuery.toLowerCase()) ||
                            task.getDate().equals(dateTomorrow) && "tomorrow".contains(searchQuery.toLowerCase());
            if(matchesQuery) {
               list.add(task);
            }
        }
        notifyDataSetChanged();
    }*/


}
