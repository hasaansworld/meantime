package com.meantime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jkb.slidemenu.SlideMenuLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import jagerfield.mobilecontactslibrary.Contact.Contact;
import jagerfield.mobilecontactslibrary.ElementContainers.NumberContainer;
import jagerfield.mobilecontactslibrary.ImportContacts;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    SharedPreferences sharedPreferences;
    SlideMenuLayout slideMenu;
    Toolbar toolbar;
    ImageView profilePicture;

    CoordinatorLayout contentLayout;
    TextView name, phoneNumber, friendCount, requestCount;
    FloatingActionButton fabAdd;
    LinearLayout optionProfile, optionNotifications, optionTrash, optionHelp, optionSettings, optionLogout;
    boolean friends = false;
    Realm realm;
    int tab = 0;

    ImageView filterImage, filterCloseImage, priorityImage;
    TextView filterText, priorityText;
    CardView filterCard;
    LinearLayout filterLayout, layoutSpanOptions, layoutPriorityOptions;
    View overlay, toolbarOverlay;
    Fragment fragment = null;
    FriendsFragment friendsFragment;
    ScheduleFragment scheduleFragment;
    int filterPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        if(Build.VERSION.SDK_INT >= 21 && getSupportActionBar() != null)
            getSupportActionBar().setElevation(0);

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);

        if(!sharedPreferences.getBoolean("profileDone", false))
            startActivity(new Intent(MainActivity.this, ProfileEditActivity.class));

        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("database")
                .schemaVersion(2)
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        realm = Realm.getDefaultInstance();

        //backgroundWork();
        getContactsPermission();
        showSchedule();


        slideMenu = findViewById(R.id.slideMenu);
        slideMenu.setParallaxSwitch(false);
        slideMenu.setContentShadowColor(R.color.contentShadow);
        slideMenu.setContentAlpha(0.5f);
        slideMenu.setContentToggle(true);

        contentLayout = findViewById(R.id.contentLayout);
        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideMenu.closeLeftSlide();
            }
        });

        profilePicture = findViewById(R.id.profilePicture);
        Glide.with(this).asBitmap().load(sharedPreferences.getString("profilePicPath", "")).placeholder(R.drawable.profile_picture).into(profilePicture);
        name = findViewById(R.id.name);
        phoneNumber = findViewById(R.id.phoneNumber);
        friendCount = findViewById(R.id.friendCount);
        requestCount = findViewById(R.id.requestCount);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        phoneNumber.setText(currentUser.getPhoneNumber());

        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tab == 0)
                    startActivity(new Intent(MainActivity.this, AddActivity.class));
                else
                {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, FirebaseAuth.getInstance().getCurrentUser().getDisplayName()+" invited you to install Meantime app. Download here: bit.ly/2QbyX4D");
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Meantime app");
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent,
                            getResources().getText(R.string.send_to)));
                }
            }
        });

        optionProfile = findViewById(R.id.optionProfile);
        optionNotifications = findViewById(R.id.optionNotifications);
        optionTrash = findViewById(R.id.optionTrash);
        optionSettings = findViewById(R.id.optionSettings);
        optionHelp = findViewById(R.id.optionHelp);
        optionLogout = findViewById(R.id.optionLogout);
        View.OnClickListener drawerListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.optionProfile){
                    Intent i = new Intent(MainActivity.this, ProfileActivity.class);
                    i.putExtra("isMyProfile", true);
                    startActivity(i);
                }
                else if(v.getId() == R.id.optionNotifications){
                    startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
                }
                else if(v.getId() == R.id.optionTrash){
                    startActivity(new Intent(MainActivity.this, TrashActivity.class));
                }
                else if(v.getId() == R.id.optionSettings){
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                }
                else if(v.getId() == R.id.optionHelp){
                    startActivity(new Intent(MainActivity.this, HelpActivity.class));
                }
            }
        };
        optionProfile.setOnClickListener(drawerListener);
        optionNotifications.setOnClickListener(drawerListener);
        optionTrash.setOnClickListener(drawerListener);
        optionSettings.setOnClickListener(drawerListener);
        optionHelp.setOnClickListener(drawerListener);

        optionLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        filterText = findViewById(R.id.filterText);
        filterImage = findViewById(R.id.filterImage);
        priorityImage = findViewById(R.id.priorityImage);
        priorityText = findViewById(R.id.priorityText);
        filterCard = findViewById(R.id.filterCard);
        filterCloseImage = findViewById(R.id.filterCloseImage);
        filterLayout = findViewById(R.id.filterLayout);
        layoutSpanOptions = findViewById(R.id.layoutSpanOptions);
        layoutPriorityOptions = findViewById(R.id.layoutPriorityOptions);
        overlay = findViewById(R.id.overlay);
        toolbarOverlay = findViewById(R.id.toolbarOverlay);

        filterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterCard.setVisibility(View.VISIBLE);
                overlay.setVisibility(View.VISIBLE);
                toolbarOverlay.setVisibility(View.VISIBLE);
                fabAdd.setClickable(false);
            }
        });


        View.OnClickListener filterCloseListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterCard.setVisibility(View.GONE);
                overlay.setVisibility(View.GONE);
                toolbarOverlay.setVisibility(View.GONE);
                fabAdd.setClickable(true);
            }
        };
        filterCloseImage.setOnClickListener(filterCloseListener);
        overlay.setOnClickListener(filterCloseListener);
        toolbarOverlay.setOnClickListener(filterCloseListener);

        for(int i = 2; i < layoutSpanOptions.getChildCount(); i++){
            View view = layoutSpanOptions.getChildAt(i);
            final int finalI = i;
            if(view instanceof TextView){
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(scheduleFragment.filterPosition != finalI -2) {
                                    scheduleFragment.filterPosition = finalI - 2;
                                    scheduleFragment.filterSchedule();
                                }
                                overlay.setVisibility(View.GONE);
                                toolbarOverlay.setVisibility(View.GONE);
                                priorityImage.setVisibility(View.GONE);
                                priorityText.setVisibility(View.GONE);
                                filterText.setTextColor(getResources().getColor(R.color.colorAccent));
                                filterCard.setVisibility(View.GONE);
                                filterText.setText(((TextView)v).getText().toString());
                            }
                        }, 300);
                    }
                });
            }
        }


        for(int i = 1; i < layoutPriorityOptions.getChildCount(); i++){
            final LinearLayout layout = (LinearLayout) layoutPriorityOptions.getChildAt(i);
            int finalI = i;
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView = (TextView)layout.getChildAt(1);
                            filterCard.setVisibility(View.GONE);
                            overlay.setVisibility(View.GONE);
                            toolbarOverlay.setVisibility(View.GONE);
                            priorityText.setVisibility(View.VISIBLE);
                            priorityText.setText(textView.getText().toString());
                            priorityImage.setVisibility(View.VISIBLE);
                            if(scheduleFragment.filterPosition != finalI +6) {
                                scheduleFragment.filterPosition = finalI +6;
                                scheduleFragment.filterSchedule();
                            }
                            if(finalI == 1){
                                priorityImage.setImageResource(R.drawable.orange_circle);
                                priorityText.setText("Low Priority");
                                priorityText.setTextColor(getResources().getColor(R.color.textOrange));
                            }
                            else if(finalI == 2){
                                priorityImage.setImageResource(R.drawable.green_circle);
                                priorityText.setText("Medium Priority");
                                priorityText.setTextColor(getResources().getColor(R.color.textGreen));
                            }
                            else{
                                priorityImage.setImageResource(R.drawable.red_circle);
                                priorityText.setText("High Priority");
                                priorityText.setTextColor(getResources().getColor(R.color.red));
                            }
                        }
                    }, 300);
                }
            });
        }

    }

    private void backgroundWork() {
        Constraints batteryConstraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build();

        Constraints networkConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest scheduleRequest =
                new PeriodicWorkRequest.Builder(SchedulingWorker.class, 20, TimeUnit.MINUTES)
                        .setConstraints(batteryConstraints)
                        .build();

        PeriodicWorkRequest backupRequest =
                new PeriodicWorkRequest.Builder(SchedulingWorker.class, 20, TimeUnit.MINUTES)
                        .setConstraints(networkConstraints)
                        .build();

        WorkManager.getInstance(this)
                .enqueue(scheduleRequest);

        WorkManager.getInstance(this)
                .enqueue(backupRequest);

    }

    private void searchNewFriends(){
        File dir = new File(Environment.getExternalStorageDirectory()+"/MeanTime/Profile Pictures");
        if(!dir.exists())
            dir.mkdirs();
        List<String> newFriends = new ArrayList<>();
        ImportContacts importContacts;
        importContacts = new ImportContacts(this);
        List<Contact> allContacts =importContacts.getContacts();
        String myNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        StorageReference picRef = FirebaseStorage.getInstance().getReference().child("users");
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        for(Contact contact: allContacts){
            for(NumberContainer num: contact.getNumbers()){
                String phone = num.getNormalizedNumber();
                if(!phone.equals("") && !phone.equals(myNumber) && realm.where(Friend.class).equalTo("phoneNumber", phone).findFirst() == null){
                    usersRef.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists() && !newFriends.contains(dataSnapshot.getKey())){
                                Friend friend = new Friend();
                                String key = dataSnapshot.getKey();
                                friend.setPhoneNumber(key);
                                friend.setName((String)dataSnapshot.child("name").getValue());
                                StorageReference friendPicRef = picRef.child(key).child("profile picture").child("profile picture.png");
                                File file = new File(dir.getAbsolutePath(), key+".png");
                                friendPicRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        friend.setProfilePicPath(file.getAbsolutePath());
                                        realm.beginTransaction();
                                        realm.copyToRealmOrUpdate(friend);
                                        realm.commitTransaction();
                                        Toast.makeText(MainActivity.this, "Downloaded profile picture for " + key, Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                        friend.setProfilePicPath("");
                                        realm.beginTransaction();
                                        realm.copyToRealmOrUpdate(friend);
                                        realm.commitTransaction();
                                        Toast.makeText(MainActivity.this, "Failed to download profile picture for " + key, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                newFriends.add(key);
                                Toast.makeText(MainActivity.this, "Found new friend:\n"+key+"\nYAY!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        }
    }

    private void getContactsPermission() {
        String[] perms = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            searchNewFriends();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.permission_rationale),
                    100, perms);
        }
    }

    void setProfileIcon(){
        String path = sharedPreferences.getString("profilePicPath", "");
        if(path.equals("")){
            if(getSupportActionBar() != null){
                setDefaultProfilePic();
            }
        }
        else{
            if(getSupportActionBar() != null){
                final int dpToPx = Math.round(dpToPixel(30, this));
                Glide.with(this)
                        .load(path)
                        .override(dpToPx, dpToPx)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.profile_picture)
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                                getSupportActionBar().setHomeAsUpIndicator(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            }
        }
    }

    void showSchedule(){
        scheduleFragment = new ScheduleFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.content, scheduleFragment);
        transaction.commit();
    }
    public static float dpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public void setDefaultProfilePic(){
        profilePicture = findViewById(R.id.profilePicture);
        Glide.with(this).asBitmap().load(sharedPreferences.getString("profilePicPath", "")).placeholder(R.drawable.profile_picture).into(profilePicture);

        int dpToPx = Math.round(dpToPixel(30, this));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Glide.with(this)
                .load(R.drawable.profile_picture)
                .override(dpToPx, dpToPx)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        getSupportActionBar().setHomeAsUpIndicator(resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            slideMenu.openLeftSlide();
        }
        else if(item.getItemId() == R.id.friends){
            if(!friends) {
                tab = 1;
                filterPosition = scheduleFragment.filterPosition;
                ExpandCollapseAnimation.collapse(filterLayout);
                item.setIcon(R.drawable.ic_time_black_24dp);
                friends = true;
                if(friendsFragment == null)
                    friendsFragment = new FriendsFragment();
                fragment = friendsFragment;
                fabAdd.setImageResource(R.drawable.ic_person_add_white_24dp);
            }
            else{
                tab = 0;
                ExpandCollapseAnimation.expand(filterLayout);
                item.setIcon(R.drawable.ic_person_black_24dp);
                friends = false;
                if(scheduleFragment == null)
                    scheduleFragment = new ScheduleFragment();
                fragment = scheduleFragment;
                scheduleFragment = (ScheduleFragment) fragment;
                scheduleFragment.filterPosition = filterPosition;
                fabAdd.setImageResource(R.drawable.ic_add_white_24dp);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    if(tab == 0)
                        transaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
                    else
                        transaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
                    transaction.replace(R.id.content, fragment);
                    transaction.commit();
                }
            }, 300);

        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        setProfileIcon();
        name.setText(sharedPreferences.getString("name", ""));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            getContactsPermission();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        searchNewFriends();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
        getContactsPermission();
    }
}
