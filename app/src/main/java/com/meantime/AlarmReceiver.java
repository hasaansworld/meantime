package com.meantime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.List;

import io.realm.Realm;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_ALARM = "com.meantime.ACTION_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        Realm.init(context);
        Realm realm = Realm.getDefaultInstance();

        scheduleAllTask(context, realm);
    }

    private void sendNotification(Context context) {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.ic_notifications_none_black_24dp);
        builder.setContentTitle("It's 12 am");
        builder.setContentText("alarm receiver runs")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1234, builder.build());
    }

    public static void scheduleAllTask(Context context, Realm realm){
        String dateToday = DateFormat.format("dd MMM yyyy", Calendar.getInstance()).toString();
        List<Task> todayTasks = realm.where(Task.class).equalTo("isInTrash", false).equalTo("date", dateToday).findAll();
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        for(int i = 0; i < todayTasks.size(); i++){
            scheduleTask(context, todayTasks.get(i), alarmManager);
        }

        long currentTimeMillis = System.currentTimeMillis();
        List<Task> deleteTasks = realm.where(Task.class).lessThan("timeInMillis", currentTimeMillis).findAll();
        realm.beginTransaction();
        for(Task task: deleteTasks){
            task.deleteFromRealm();
        }
        realm.commitTransaction();
    }

    public static void scheduleTask(Context context, Task task, AlarmManager alarmManager){
        long timeInMillis = task.getTimeInMillis();
        Intent intent1 = new Intent(context.getApplicationContext(), NotificationReceiver.class);
        intent1.setAction(NotificationReceiver.ACTION_NOTIFICATION);
        int id = (int)timeInMillis/10000;
        intent1.putExtra("id", timeInMillis);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, intent1,
                0);
        if (Build.VERSION.SDK_INT >= 19)
            alarmManager.setExact(AlarmManager.RTC, timeInMillis, pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC, timeInMillis, pendingIntent);
    }

    public static void startSchedule(Context context) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context.getApplicationContext(), AlarmReceiver.class);
        i.setAction(AlarmReceiver.ACTION_ALARM);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long tomorrowInMillis = calendar.getTimeInMillis();
        int id = (int)tomorrowInMillis/10000;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, i,
                0);
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC, tomorrowInMillis, AlarmManager.INTERVAL_DAY, pendingIntent);
    }

}
