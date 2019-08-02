package com.meantime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.renderscript.Type;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.shuhart.stickyheader.StickyAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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

    public ScheduleAdapter(Context context) {
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
        LinearLayout background, foreground;

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

            background = v.findViewById(R.id.background);
            foreground = v.findViewById(R.id.foreground);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false);
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
            //Drawable d = context.getResources().getDrawable(R.drawable.white_circle);
            //d.setColorFilter(resources.getColor(colors[item.getPriority()]), PorterDuff.Mode.SRC_ATOP);
            //holder.imageColor.setImageDrawable(d);
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
            //if(filterPosition <= 2){
            //    holder.date.setVisibility(View.GONE);
            //}
            //else{

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

            //
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    void removeItem(int position) {
        ScheduleItem scheduleItem = list.get(position);
        Task task = realm.where(Task.class).equalTo("title", scheduleItem.title).equalTo("description", scheduleItem.description).equalTo("date", scheduleItem.date).equalTo("time", scheduleItem.time).findFirst();
        if (task != null) {
            realm.beginTransaction();
            task.setIsInTrash(true);
            realm.commitTransaction();

            long timeInMillis = task.getTimeInMillis();
            Intent intent1 = new Intent(context.getApplicationContext(), NotificationReceiver.class);
            intent1.setAction(NotificationReceiver.ACTION_NOTIFICATION);
            int id = (int) timeInMillis / 10000;
            intent1.putExtra("id", timeInMillis);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, intent1,
                    0);
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(pendingIntent);
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
        allTasks.addAll(realm.where(Task.class).equalTo("isInTrash", false).greaterThanOrEqualTo("timeInMillis", timeInMillis).findAll());
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
















    /*public void setFilterPosition(int p){
        filterPosition = p;
    }

    public void date(int difference, boolean filter){
        list = new ArrayList<>();
        dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, difference);
        String dateToday = DateFormat.format("dd MMM yyyy", calendar).toString();
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(realm.where(Task.class).equalTo("date", dateToday).findAll());
        Collections.sort(allTasks);
        for(Task task: allTasks) {
            ScheduleItem scheduleItem = new ScheduleItem(task.getTitle(), task.getTime(), task.getDescription(), null, task.getLocation(), task.getPriority());
            list.add(scheduleItem);
        }
        if(filter) notifyDataSetChanged();
    }

    public void week(boolean update){
        list = new ArrayList<>();
        dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(realm.where(Task.class).findAll());
        Collections.sort(allTasks);
        for(Task task: allTasks) {
            String taskDate = task.getDate();
            Calendar taskCal = DateUtils.getCalendarFromString(taskDate);
            if(calendar.get(Calendar.YEAR) == taskCal.get(Calendar.YEAR) && calendar.get(Calendar.WEEK_OF_YEAR) == taskCal.get(Calendar.WEEK_OF_YEAR)) {
                ScheduleItem scheduleItem = new ScheduleItem(task.getTitle(), task.getTime(), task.getDescription(), null, task.getLocation(), task.getPriority());
                scheduleItem.setDate(task.getDate());
                if(!dateList.contains(task.getDate())) dateList.add(task.getDate());
                list.add(scheduleItem);
            }
        }
        if(update) notifyDataSetChanged();
    }

    void month(boolean update){
        list = new ArrayList<>();
        dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(realm.where(Task.class).findAll());
        Collections.sort(allTasks);
        for(Task task: allTasks) {
            String taskDate = task.getDate();
            Calendar taskCal = DateUtils.getCalendarFromString(taskDate);
            if(calendar.get(Calendar.YEAR) == taskCal.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) == taskCal.get(Calendar.MONTH)) {
                ScheduleItem scheduleItem = new ScheduleItem(task.getTitle(), task.getTime(), task.getDescription(), null, task.getLocation(), task.getPriority());
                scheduleItem.setDate(task.getDate());
                if(!dateList.contains(task.getDate())) dateList.add(task.getDate());
                list.add(scheduleItem);
            }
        }
        if(update) notifyDataSetChanged();
    }

    void year(boolean update){
        list = new ArrayList<>();
        dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(realm.where(Task.class).findAll());
        Collections.sort(allTasks);
        for(Task task: allTasks) {
            String taskDate = task.getDate();
            Calendar taskCal = DateUtils.getCalendarFromString(taskDate);
            if(calendar.get(Calendar.YEAR) == taskCal.get(Calendar.YEAR)) {
                ScheduleItem scheduleItem = new ScheduleItem(task.getTitle(), task.getTime(), task.getDescription(), null, task.getLocation(), task.getPriority());
                scheduleItem.setDate(task.getDate());
                if(!dateList.contains(task.getDate())) dateList.add(task.getDate());
                list.add(scheduleItem);
            }
        }
        if(update) notifyDataSetChanged();
    }

    void allTime(boolean update){
        list = new ArrayList<>();
        dateList = new ArrayList<>();
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(realm.where(Task.class).findAll());
        Collections.sort(allTasks);
        for(Task task: allTasks) {
            ScheduleItem scheduleItem = new ScheduleItem(task.getTitle(), task.getTime(), task.getDescription(), null, task.getLocation(), task.getPriority());
            scheduleItem.setDate(task.getDate());
            if(!dateList.contains(task.getDate())) dateList.add(task.getDate());
            list.add(scheduleItem);
        }
        if(update) notifyDataSetChanged();
    }

    void filter(){
        if(filterPosition == 0)
            date(0, false);
        else if(filterPosition == 1)
            date(-1, false);
        else if(filterPosition == 2)
            date(1, false);
        else if(filterPosition == 3)
            week(false);
        else if(filterPosition == 4)
            month(false);
        else if(filterPosition == 5)
            year(false);
        else if(filterPosition == 6)
            allTime(false);
    }

    void priority(int p){
        filter();
        for(int i = list.size()-1; i >= 0; i--){
            ScheduleItem scheduleItem = list.get(i);
            if(scheduleItem.getPriority() != p){
                list.remove(i);
            }
        }
        notifyDataSetChanged();
    }

    String getDateAt(int position){
        return list.get(position).getDate();
    }

    int getNearestPosition(){
        if(list.size() == 0)
            return 0;
        int position = 0;

        Calendar calendar = Calendar.getInstance();

        boolean isFound = false;
        for(int i = 0; i < getItemCount(); i++){
            ScheduleItem item = list.get(i);
            String date = item.getDate();
            if(date != null && date.equals(dateToday)){
                isFound = true;
                position = i;
            }
        }
        if(!isFound){
            long timeInMillis = calendar.getTimeInMillis();
            for(int i = 0; i < list.size(); i++){
                ScheduleItem item = list.get(i);
                String date = item.getDate();
                if(date != null && DateUtils.getCalendarFromString(date).getTimeInMillis() > timeInMillis){
                    position = i;
                }
            }
        }

        String lastDate = list.get(list.size()-1).getDate();
        if(lastDate != null && calendar.getTimeInMillis() > DateUtils.getCalendarFromString(lastDate).getTimeInMillis())
            position = list.size()-1;

        return position;
    }*/
}
