package com.meantime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduleItem {
    String title,time, date, description,imagePath,location;
    int priority;
    List <Object> friendList = new ArrayList<>();

    public ScheduleItem(String title, String time, String description, String imagePath, String location, int priority) {
        this.title = title;
        this.time = time;
        this.description = description;
        this.imagePath = imagePath;
        this.location = location;
        this.priority = priority;

    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getDate(){ return date; }

    public void setDate(String date){ this.date = date; }
}
