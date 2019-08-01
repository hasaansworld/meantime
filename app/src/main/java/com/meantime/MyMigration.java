package com.meantime;

import android.content.Context;
import android.widget.Toast;

import androidx.room.migration.Migration;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class MyMigration implements RealmMigration {

    Context context;

    public MyMigration(Context context){
        this.context = context;
    }

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema realmSchema = realm.getSchema();
        RealmObjectSchema taskSchema = realmSchema.get("Task");
        if(oldVersion < 3) {
            taskSchema.addField("isScheduled", boolean.class);
        }
        if(oldVersion < 4){
            taskSchema.removeField("isScheduled");
            taskSchema.removeField("online");
            taskSchema.addField("isInTrash", boolean.class);
        }
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof MyMigration;
    }

    @Override
    public int hashCode() {
        return Migration.class.hashCode();
    }
}
