package com.meantime;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Task extends RealmObject implements Comparable<Task> {
    @PrimaryKey
    int id;
    String title, description, date, time, location, creator;
    long timeInMillis;
    int priority;
    RealmList<Friend> friendsList;
    boolean isInTrash = false;

    public Task()
    {

    }

    public static int nextId(){
        Number currentIdNum = RealmUtils.getRealm().where(Task.class).max("id");
        int nextId;
        if(currentIdNum == null) {
            nextId = 1;
        } else {
            nextId = currentIdNum.intValue() + 1;
        }
        return nextId;
    }

    public Task(int id, String title, String description, String date, String time, String location, long timeInMillis, int priority, List<Friend> list, String creator) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.timeInMillis = timeInMillis;
        this.priority = priority;
        this.creator = creator;
        friendsList = new RealmList<>();
        friendsList.addAll(list);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public RealmList<Friend> getFriendsList() {
        return friendsList;
    }

    public void setFriendsList(RealmList<Friend> friendsList) {
        this.friendsList = friendsList;
    }

    public boolean isInTrash() {
        return isInTrash;
    }

    public void setIsInTrash(boolean isInTrash) {
        this.isInTrash = isInTrash;
    }

    public String getCreator(){ return creator; }

    public void setCreator(String creator){
        this.creator = creator;
    }

    public int getId(){
        return id;
    }

    @Override
    public int compareTo(Task other) {
        return Long.compare(getTimeInMillis(), other.getTimeInMillis());
    }
}
