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
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;

public class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<Task> list;
    List<Task> allTasks;
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
        fetchUpdates();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout layout, friendsLayout;
        ImageView imageColor, imageLocation, profilePicture;
        TextView title, time, date, description, friendCount;
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
            friendsLayout = v.findViewById(R.id.friendsLayout);
            friendCount = v.findViewById(R.id.friendCount);

            background = v.findViewById(R.id.background);
            foreground = v.findViewById(R.id.foreground);
            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Task task = list.get(getAdapterPosition());
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("title", task.getTitle());
            intent.putExtra("id", task.getTimeInMillis());
            context.startActivity(intent);
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
        Task task = list.get(position);
        if (h instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) h;
            holder.title.setText(task.getTitle());
            if (task.getDescription().equals(""))
                holder.description.setText("No details yet.");
            else
                holder.description.setText(task.getDescription());
            holder.time.setText(task.getTime());
            //Drawable d = context.getResources().getDrawable(R.drawable.white_circle);
            //d.setColorFilter(resources.getColor(colors[task.getPriority()]), PorterDuff.Mode.SRC_ATOP);
            //holder.imageColor.setImageDrawable(d);
            RealmList<Friend> friendsList = task.getFriendsList();
            if (friendsList.size() > 0) {
                holder.friendsLayout.setVisibility(View.VISIBLE);
                Friend first = friendsList.get(0);
                String profilePicPath = first.getProfilePicPath();
                if (profilePicPath == null || profilePicPath.equals(""))
                    holder.profilePicture.setImageResource(R.drawable.profile_picture);
                else
                    Glide.with(context).load(profilePicPath).into(holder.profilePicture);
                if(friendsList.size() > 1){
                    holder.friendCount.setVisibility(View.VISIBLE);
                    holder.friendCount.setText("+"+(friendsList.size()-1));
                }
                else
                    holder.friendCount.setVisibility(View.GONE);
            } else {
                holder.friendsLayout.setVisibility(View.GONE);
            }
            if (task.getLocation().equals(""))
                holder.imageLocation.setVisibility(View.GONE);
            else
                holder.imageLocation.setVisibility(View.VISIBLE);
            //if(filterPosition <= 2){
            //    holder.date.setVisibility(View.GONE);
            //}
            //else{

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

            //
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    void removeItem(int position) {
        Task task = list.get(position);
        DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("tasks").child(Long.toString(task.getTimeInMillis()));
        taskRef.child("isInTrash").setValue(true);

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
        allTasks = new ArrayList<>();
        long timeInMillis = getTodayInMillis();
        allTasks.addAll(realm.where(Task.class).equalTo("isInTrash", false).greaterThanOrEqualTo("timeInMillis", timeInMillis).findAll());
        Collections.sort(allTasks);
        list = new ArrayList<>();
        if(allTasks != null) list.addAll(allTasks);
    }

    long getTodayInMillis(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    void setSearchQuery(String query) {
        searchQuery = query;
        list = new ArrayList<>();
        for (int i = 0; i < allTasks.size(); i++) {
            Task task = allTasks.get(i);
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
    }


    void fetchUpdates(){
        final long todayInMillis = getTodayInMillis();
        String myPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference().child("users").child(myPhone).child("taskRequests");
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                        String phoneNumber = itemSnapshot.getKey();
                        for (DataSnapshot ds : itemSnapshot.getChildren()) {
                            String requestId = ds.getKey();
                            final DatabaseReference requestReference = FirebaseDatabase.getInstance().getReference().child("users").child(myPhone).child("taskRequests").child(phoneNumber).child(requestId);
                            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(phoneNumber).child("tasks").child(requestId);
                            if(Long.parseLong(requestId) >= todayInMillis) {

                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            List<Friend> friends = RealmUtils.getListsFromPhones((ArrayList<String>) dataSnapshot.child("friends").getValue());
                                            Task task = new Task(
                                                    Task.nextId(),
                                                    (String) dataSnapshot.child("title").getValue(),
                                                    (String) dataSnapshot.child("description").getValue(),
                                                    (String) dataSnapshot.child("date").getValue(),
                                                    (String) dataSnapshot.child("time").getValue(),
                                                    (String) dataSnapshot.child("location").getValue(),
                                                    Long.parseLong(requestId),
                                                    0,
                                                    friends,
                                                    phoneNumber);

                                            realm.beginTransaction();
                                            realm.copyToRealmOrUpdate(task);
                                            realm.commitTransaction();

                                            if(!task.isInTrash()) {
                                                for (int i = 0; i < list.size(); i++) {
                                                    if (list.get(i).getTimeInMillis() > task.getTimeInMillis()) {
                                                        list.add(i, task);
                                                        notifyItemInserted(i);
                                                        break;
                                                    }
                                                }
                                            }

                                            requestReference.removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }else{
                                requestReference.removeValue();
                            }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

            /*@Override
            public void ond(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    requestsRef.child(phone).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            if(dataSnapshot.exists()){
                                String taskId = dataSnapshot.getKey();
                                Random random = new Random();
                                Toast.makeText(context, taskId+" "+(random.nextInt()%6), Toast.LENGTH_SHORT).show();
                                final DatabaseReference thisRequest = requestsRef.child(phone).child(taskId);
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(phone).child("tasks").child(taskId);

                                if(Long.parseLong(taskId) >= todayInMillis) {
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                List<Friend> friends = RealmUtils.getListsFromPhones((ArrayList<String>) dataSnapshot.child("friends").getValue());
                                                Task task = new Task(
                                                        Task.nextId(),
                                                        (String) dataSnapshot.child("title").getValue(),
                                                        (String) dataSnapshot.child("description").getValue(),
                                                        (String) dataSnapshot.child("date").getValue(),
                                                        (String) dataSnapshot.child("time").getValue(),
                                                        (String) dataSnapshot.child("location").getValue(),
                                                        Long.parseLong(taskId),
                                                        0,
                                                        friends,
                                                        phone);

                                                realm.beginTransaction();
                                                realm.copyToRealmOrUpdate(task);
                                                realm.commitTransaction();
                                                if(!task.isInTrash()) {
                                                    for (int i = 0; i < list.size(); i++) {
                                                        if (list.get(i).getTimeInMillis() > task.getTimeInMillis()) {
                                                            list.add(i, task);
                                                            notifyItemInserted(i);
                                                            break;
                                                        }
                                                    }
                                                }
                                                thisRequest.removeValue();
                                                databaseReference.removeEventListener(this);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                                else{
                                    thisRequest.removeValue();
                                }
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }*/

        });
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
            Scheduletask scheduletask = new Scheduletask(task.getTitle(), task.getTime(), task.getDescription(), null, task.getLocation(), task.getPriority());
            list.add(scheduletask);
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
                Scheduletask scheduletask = new Scheduletask(task.getTitle(), task.getTime(), task.getDescription(), null, task.getLocation(), task.getPriority());
                scheduletask.setDate(task.getDate());
                if(!dateList.contains(task.getDate())) dateList.add(task.getDate());
                list.add(scheduletask);
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
                Scheduletask scheduletask = new Scheduletask(task.getTitle(), task.getTime(), task.getDescription(), null, task.getLocation(), task.getPriority());
                scheduletask.setDate(task.getDate());
                if(!dateList.contains(task.getDate())) dateList.add(task.getDate());
                list.add(scheduletask);
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
                Scheduletask scheduletask = new Scheduletask(task.getTitle(), task.getTime(), task.getDescription(), null, task.getLocation(), task.getPriority());
                scheduletask.setDate(task.getDate());
                if(!dateList.contains(task.getDate())) dateList.add(task.getDate());
                list.add(scheduletask);
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
            Scheduletask scheduletask = new Scheduletask(task.getTitle(), task.getTime(), task.getDescription(), null, task.getLocation(), task.getPriority());
            scheduletask.setDate(task.getDate());
            if(!dateList.contains(task.getDate())) dateList.add(task.getDate());
            list.add(scheduletask);
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
            Scheduletask scheduletask = list.get(i);
            if(scheduletask.getPriority() != p){
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
        for(int i = 0; i < gettaskCount(); i++){
            Scheduletask task = list.get(i);
            String date = task.getDate();
            if(date != null && date.equals(dateToday)){
                isFound = true;
                position = i;
            }
        }
        if(!isFound){
            long timeInMillis = calendar.getTimeInMillis();
            for(int i = 0; i < list.size(); i++){
                Scheduletask task = list.get(i);
                String date = task.getDate();
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
