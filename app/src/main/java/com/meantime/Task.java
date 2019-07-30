package com.meantime;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Task extends RealmObject implements Comparable<Task> {
    String title,description,date,time,location;
    long timeInMillis;
    int priority;
    RealmList<Friend> friendsList;
    boolean online = false;

    public Task()
    {

    }

    public Task(String title, String description, String date, String time, String location, long timeInMillis, int priority, List<Friend> list) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.timeInMillis = timeInMillis;
        this.priority = priority;
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

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public int compareTo(Task other) {
        return Long.compare(getTimeInMillis(), other.getTimeInMillis());
    }
}
