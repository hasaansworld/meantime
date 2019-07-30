package com.meantime;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FragmentAbout extends Fragment {

    TextView textAbout, textGender;
    SharedPreferences sharedPreferences;
    View aboutSeparator;
    String[] genders = {"Male", "Female", "Other"};
    boolean isMyProfile;

    public FragmentAbout(SharedPreferences sharedPreferences, boolean isMyProfile){
        this.sharedPreferences = sharedPreferences;
        this.isMyProfile = isMyProfile;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        textAbout = v.findViewById(R.id.textAbout);
        textGender = v.findViewById(R.id.textGender);
        aboutSeparator = v.findViewById(R.id.aboutSeparator);
        if(isMyProfile)
            aboutSeparator.setVisibility(View.VISIBLE);

        return v;
    }

    private void showInformation() {

        textAbout.setText(sharedPreferences.getString("about", ""));
        textGender.setText(genders[sharedPreferences.getInt("gender", 0)]);
    }

    @Override
    public void onStart() {
        super.onStart();
        showInformation();
    }
}
