package com.meantime;

import androidx.annotation.Nullable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Friend extends RealmObject {
    @PrimaryKey
    String phoneNumber;
    String name, profilePicPath;

    public Friend()
    {

    }

    public Friend(String name, String phoneNumber, String profilePicPath) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profilePicPath = profilePicPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePicPath() {
        return profilePicPath;
    }

    public void setProfilePicPath(String profilePicPath) {
        this.profilePicPath = profilePicPath;
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Friend)) {
            return false;
        }

        Friend other = (Friend) obj;

        return name.equals(other.name) && phoneNumber.equals(other.phoneNumber) && profilePicPath.equals(other.profilePicPath);
    }
}
