package com.meantime;

import android.content.Context;
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
    List <ScheduleItem> list ;
    Realm realm;
    int[] colors = {R.color.orange, R.color.green, R.color.red};
    Resources resources;
    int filterPosition = 0;

    final static int TYPE_TASK = 0;
    final static int TYPE_DATE = 1;
    final static int TYPE_MONTH = 2;
    final static int TYPE_YEAR = 3;

    String dateToday, dateYesterday, dateTomorrow;
    String thisMonth, lastMonth, nextMonth;

    public ScheduleAdapter(Context context){
        this.context = context;
        resources = context.getResources();
        Realm.init(context);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("database")
                .schemaVersion(2)
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        realm = Realm.getDefaultInstance();

        Calendar calendar = Calendar.getInstance();
        dateToday = DateFormat.format("dd MMM yyyy", calendar).toString();
        calendar.add(Calendar.DATE, -1);
        dateYesterday = DateFormat.format("dd MMM yyyy", calendar).toString();
        calendar.add(Calendar.DATE, 2);
        dateTomorrow = DateFormat.format("dd MMM yyyy", calendar).toString();
        date(0, false);

        Calendar cal = Calendar.getInstance();
        thisMonth = DateUtils.getMonth(DateFormat.format("dd MMM yyyy", cal).toString());
        cal.add(Calendar.MONTH, -1);
        lastMonth = DateUtils.getMonth(DateFormat.format("dd MMM yyyy", cal).toString());
        cal.add(Calendar.MONTH, +2);
        nextMonth = DateUtils.getMonth(DateFormat.format("dd MMM yyyy", cal).toString());
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout layout;
        ImageView imageColor,imageLocation,profilePicture;
        TextView title,time,date,description, month, year;
        CardView cardYear;
        View gap;
        public ViewHolder(View v){
            super(v);
            layout = v.findViewById(R.id.layout);
            imageColor = v.findViewById(R.id.colorImage);
            imageLocation = v.findViewById(R.id.locationImage);
            profilePicture = v.findViewById(R.id.profilePicture);
            title = v.findViewById(R.id.title);
            time = v.findViewById(R.id.time);
            cardYear = v.findViewById(R.id.cardYear);
            year = v.findViewById(R.id.year);
            month = v.findViewById(R.id.month);
            date = v.findViewById(R.id.date);
            description = v.findViewById(R.id.description);
            gap = v.findViewById(R.id.gap);
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
        if(h instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder)h;
            holder.title.setText(item.getTitle());
            if(item.getDescription().equals(""))
                holder.description.setText("No details yet.");
            else
                holder.description.setText(item.getDescription());
            holder.time.setText(item.getTime());
            Drawable d = context.getResources().getDrawable(R.drawable.white_circle);
            d.setColorFilter(resources.getColor(colors[item.getPriority()]), PorterDuff.Mode.SRC_ATOP);
            holder.imageColor.setImageDrawable(d);
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
            if(filterPosition <= 2){
                holder.date.setVisibility(View.GONE);
                holder.gap.setVisibility(View.GONE);
            }
            else{
                if(position == 0 || !item.getDate().equals(list.get(position-1).getDate())){
                    holder.date.setVisibility(View.VISIBLE);
                    holder.gap.setVisibility(View.VISIBLE);
                    if(item.getDate().equals(dateToday))
                        holder.date.setText("Today");
                    else if(item.getDate().equals(dateYesterday))
                        holder.date.setText("Yesterday");
                    else if(item.getDate().equals(dateTomorrow))
                        holder.date.setText("Tomorrow");
                    else
                        holder.date.setText(item.getDate());
                }
                else{
                    holder.gap.setVisibility(View.GONE);
                    holder.date.setVisibility(View.GONE);
                }
            }
            if(filterPosition <= 4){
                holder.month.setVisibility(View.GONE);
            }
            else{
                String month = DateUtils.getMonth(item.getDate());
                if(position == 0 || !DateUtils.getMonth(list.get(position-1).getDate()).equals(month) || DateUtils.getYear(list.get(position-1).getDate()) != DateUtils.getYear(item.getDate())){
                    holder.month.setVisibility(View.VISIBLE);
                    if(month.equals(thisMonth)){
                        holder.month.setText("This Month");
                    }
                    else if(month.equals(lastMonth)){
                        holder.month.setText("Last Month");
                    }
                    else if(month.equals(nextMonth)){
                        holder.month.setText("Next Month");
                    }
                    else{
                        holder.month.setText(month);
                    }
                }
                else
                    holder.month.setVisibility(View.GONE);
            }

            if(filterPosition != 6){
                holder.cardYear.setVisibility(View.GONE);
            }
            else{
                int year = DateUtils.getYear(item.getDate());
                if(position == 0 || year != DateUtils.getYear(list.get(position-1).getDate())){
                    holder.cardYear.setVisibility(View.VISIBLE);
                    holder.year.setText(Integer.toString(year));
                }
                else
                    holder.cardYear.setVisibility(View.GONE);

            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setFilterPosition(int p){
        filterPosition = p;
    }

    public void date(int difference, boolean filter){
        list = new ArrayList<>();
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
                list.add(scheduleItem);
            }
        }
        if(update) notifyDataSetChanged();
    }

    void month(boolean update){
        list = new ArrayList<>();
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
                list.add(scheduleItem);
            }
        }
        if(update) notifyDataSetChanged();
    }

    void year(boolean update){
        list = new ArrayList<>();
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
                list.add(scheduleItem);
            }
        }
        if(update) notifyDataSetChanged();
    }

    void allTime(boolean update){
        list = new ArrayList<>();
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(realm.where(Task.class).findAll());
        Collections.sort(allTasks);
        for(Task task: allTasks) {
            ScheduleItem scheduleItem = new ScheduleItem(task.getTitle(), task.getTime(), task.getDescription(), null, task.getLocation(), task.getPriority());
            scheduleItem.setDate(task.getDate());
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
}
