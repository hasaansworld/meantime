package com.meantime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    Toolbar toolbar;
    TextView name, phoneNumber;
    ImageView profilePicture;
    MaterialButton buttonMessage;
    TabLayout tabLayout;
    boolean isMyProfile = false;
    ViewPager viewPager;
    TabItem tab1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        if(getSupportActionBar() != null) {
            if(Build.VERSION.SDK_INT >= 21) getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }

        if(getIntent().getExtras() != null)
            isMyProfile = true;

        name = findViewById(R.id.name);
        phoneNumber = findViewById(R.id.phoneNumber);
        profilePicture = findViewById(R.id.profilePicture);
        buttonMessage = findViewById(R.id.buttonMessage);
        tabLayout = findViewById(R.id.tabLayout);
        if(isMyProfile)
        {
            buttonMessage.setVisibility(View.GONE);
            tabLayout.getTabAt(1).setText("Contacts");
        }

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);


        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = sharedPreferences.getString("profilePicPath", "");
                if(!path.equals("")) {
                    Intent i = new Intent(ProfileActivity.this, FullPhotoActivity.class);
                    i.putExtra("title", "Profile Picture");
                    i.putExtra("path", path);
                    startActivity(i);
                }
            }
        });
    }

    private void showInformation() {
        name.setText(sharedPreferences.getString("name", ""));
        phoneNumber.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        String profilePicPath = sharedPreferences.getString("profilePicPath", "");
        if(!profilePicPath.equals("")){
            Glide.with(this).asBitmap().load(profilePicPath).diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.drawable.profile_picture).into(profilePicture);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        else if(item.getItemId() == R.id.edit){
            Intent i = new Intent(this, ProfileEditActivity.class);
            i.putExtra("editing", true);
            startActivity(i);
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        showInformation();
    }

    class MyPagerAdapter extends FragmentPagerAdapter{

        List<Fragment> fragmentList = new ArrayList<>();

        public MyPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            fragmentList.add(new FragmentAbout(sharedPreferences, isMyProfile));
            fragmentList.add(new ContactsFragment());
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0)
                return "Info";
            else
            {
                if(isMyProfile)
                    return "Contacts";
                else
                    return "Activity";
            }
        }
    }
}
