package com.meantime;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObjectSchema;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_NOTIFICATION = "com.meantime.ACTION_NOTIFICATION";
    Context context;
    int notificationId = 12345;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Realm.init(context);
        Realm realm = Realm.getDefaultInstance();
        long id = intent.getLongExtra("id", 0);
        List<Task> taskList = realm.where(Task.class).equalTo("timeInMillis", id).findAll();
        for(Task task: taskList){
            sendNotification(task);
        }
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "alerts";
            String description = "Get notifications about tasks and events.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    void sendNotification(Task task){
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        createNotificationChannel();
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.ic_notifications_none_black_24dp);
        builder.setContentTitle("Reminder: \""+task.getTitle()+"\"");
        builder.setContentText(task.getDescription())
                .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(task.getDescription()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setSound(soundUri)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
        notificationId++;
    }

}
