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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;

public class TrashAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<ScheduleItem> list;
    List<ScheduleItem> allItems;
    private List<String> dateList;
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

        dateList = new ArrayList<>();
        initialize();
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
        ScheduleItem item = list.get(position);
        if (h instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) h;
            holder.title.setText(item.getTitle());
            if (item.getDescription().equals(""))
                holder.description.setText("No details yet.");
            else
                holder.description.setText(item.getDescription());
            holder.time.setText(item.getTime());

            if (item.getImagePath() != null) {
                holder.profilePicture.setVisibility(View.VISIBLE);
                if (item.getImagePath().equals(""))
                    holder.profilePicture.setImageResource(R.drawable.profile_picture);
                else
                    Glide.with(context).load(item.getImagePath()).into(holder.profilePicture);
            } else
                holder.profilePicture.setVisibility(View.GONE);
            if (item.getLocation().equals(""))
                holder.imageLocation.setVisibility(View.GONE);
            else
                holder.imageLocation.setVisibility(View.VISIBLE);


            holder.layout.setVisibility(View.VISIBLE);
            if (position == 0 || !item.getDate().equals(list.get(position - 1).getDate())) {
                holder.date.setVisibility(View.VISIBLE);
                if (item.getDate().equals(dateToday) && filterPosition <= 4)
                    holder.date.setText("Today");
                else if (item.getDate().equals(dateYesterday) && filterPosition <= 4)
                    holder.date.setText("Yesterday");
                else if (item.getDate().equals(dateTomorrow) && filterPosition <= 4)
                    holder.date.setText("Tomorrow");
                else
                    holder.date.setText(item.getDate());
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
        ScheduleItem scheduleItem = list.get(position);
        Task task = realm.where(Task.class).equalTo("title", scheduleItem.title).equalTo("description", scheduleItem.description).equalTo("date", scheduleItem.date).equalTo("time", scheduleItem.time).findFirst();
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
        list = new ArrayList<>();
        List<Task> allTasks = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long timeInMillis = calendar.getTimeInMillis();
        allTasks.addAll(realm.where(Task.class).equalTo("isInTrash", true).greaterThanOrEqualTo("timeInMillis", timeInMillis).findAll());
        Collections.sort(allTasks);
        for (Task task : allTasks) {
            ScheduleItem scheduleItem = new ScheduleItem(task.getTitle(), task.getTime(), task.getDescription(), null, task.getLocation(), task.getPriority());
            scheduleItem.setDate(task.getDate());
            list.add(scheduleItem);
        }
        allItems = new ArrayList<>(list);
    }

    void setSearchQuery(String query) {
        searchQuery = query;
        list = new ArrayList<>();
        for (int i = 0; i < allItems.size(); i++) {
            ScheduleItem item = allItems.get(i);
            boolean matchesQuery =
                    item.getTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            item.getDescription().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            item.getDate().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            item.getTime().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            item.getDate().equals(dateToday) && "today".contains(searchQuery.toLowerCase()) ||
                            item.getDate().equals(dateTomorrow) && "tomorrow".contains(searchQuery.toLowerCase());
            if(matchesQuery) {
               list.add(item);
            }
        }
        notifyDataSetChanged();
    }


}
