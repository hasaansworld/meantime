package com.meantime;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmUtils {
    public static Realm realm;

    public static void init(Context context){
        Realm.init(context);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("database")
                .migration(new MyMigration(context))
                .schemaVersion(6)
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        realm = Realm.getDefaultInstance();
    }

    public static Realm getRealm(){
        return realm;
    }

    public static List<Friend> getListsFromPhones(java.util.List<String> phones){
        List<Friend> friends = new ArrayList<>();
        if(phones != null) {
            for (String string : phones) {
                Friend friend = getRealm().where(Friend.class).equalTo("phoneNumber", string).findFirst();
                if (friend != null) friends.add(friend);
                else {
                    friend = new Friend();
                    friend.setPhoneNumber(string);
                    friends.add(friend);
                }
            }
        }
        return friends;
    }
}
