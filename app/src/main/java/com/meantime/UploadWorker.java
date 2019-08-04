package com.meantime;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Interpolator;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import javax.xml.transform.Result;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class UploadWorker extends Worker {
    Context context;
    private int count = 0;
    long id;
    private Task task;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        id = getInputData().getLong("id", 0);
        new Handler(Looper.getMainLooper()).post(new Runnable() { // <-- if you are not on UI thread and want to go there
            @Override
            public void run() {
                Realm realm = null;
                try {
                    realm = Realm.getDefaultInstance();
                    task = realm.where(Task.class).equalTo("timeInMillis", id).findFirst();

                    upload();
                } finally {
                    if(realm != null) {
                        realm.close();
                    }
                }
            }
        });
        //if(task != null) upload();
        return Result.failure();
    }

    private void upload(){
        count++;
        if(count < 4) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("tasks").child(Long.toString(id));
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("title", task.getTitle());
            hashMap.put("description", task.getDescription());
            hashMap.put("time", task.getTime());
            hashMap.put("date", task.getDate());
            hashMap.put("priority", task.getPriority());
            hashMap.put("location", task.getLocation());
            hashMap.put("isInTrash", false);
            ref.setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            upload();
                        }
                    });
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


    void sendNotification(){
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        createNotificationChannel();
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(R.drawable.ic_notifications_none_black_24dp);
        builder.setContentTitle(task.getTitle());
        builder.setContentText(task.getDescription())
                //.setStyle(new NotificationCompat.BigTextStyle()
                //.bigText(sentTo))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setSound(soundUri)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1234, builder.build());
    }

}
