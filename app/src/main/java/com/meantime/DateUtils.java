package com.meantime;

import java.util.Calendar;

public class DateUtils {

    public static Calendar getCalendarFromString(String string){
        String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
        Calendar calendar = Calendar.getInstance();
        String[] parts = string.split(" ");
        int date = Integer.parseInt(parts[0]);
        int month = 0;
        for(int i = 0; i < 12; i++){
            if(parts[1].toLowerCase().equals(months[i]))
                month = i;
        }
        int year = Integer.parseInt(parts[2]);
        calendar.set(Calendar.DATE, date);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);

        return calendar;
    }

    public static String getMonth(String string){
        String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
        String[] monthsFull = {
                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December"
        };
        String[] parts = string.split(" ");
        int month = 0;
        for(int i = 0; i < 12; i++){
            if(parts[1].toLowerCase().equals(months[i]))
                month = i;
        }

        return monthsFull[month];
    }

    public static int getYear(String string){
        String[] parts = string.split(" ");
        return Integer.parseInt(parts[2]);
    }

}
