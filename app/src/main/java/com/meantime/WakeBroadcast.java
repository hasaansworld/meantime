package com.meantime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.realm.Realm;

public class WakeBroadcast extends BroadcastReceiver {
    Realm realm;

    @Override
    public void onReceive(Context context, Intent intent) {
        Realm.init(context);
        realm = Realm.getDefaultInstance();

        AlarmReceiver.scheduleAllTask(context, realm);
        AlarmReceiver.startSchedule(context);
    }

}
