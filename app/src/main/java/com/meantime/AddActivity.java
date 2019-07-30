package com.meantime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.rd.PageIndicatorView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class AddActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    Toolbar toolbar;
    TabLayout tabLayout;
    ImageView priorityOrangeRing, priorityOrangeCircle, priorityGreenRing, priorityGreenCircle, priorityRedRing, priorityRedCircle;
    int priority = 0;
    TextView priorityText;
    FloatingActionButton fabAddFriends;
    ChipGroup chipGroupFriends;
    Drawable defaultProfile;
    List<Friend> friendsList = new ArrayList<>();
    LinearLayout expandLayout, moreOptionsLayout;
    ImageView expandArrow;
    TextView expandText;
    boolean expanded = false;
    LinearLayout dateLayout, timeLayout;
    TextView dateText, timeText;
    ViewPager viewPagerPhoto;
    PageIndicatorView indicator;
    FrameLayout coverLayout;
    LinearLayout prioritiesLayout, friendsLayout;
    TextView privacyText, friendsText;
    Switch privacySwitch;
    MaterialButton saveButton;
    FloatingActionButton coverFAB;

    PhotoPagerAdapter photoPagerAdapter;
    String path;
    Realm realm;
    int startDay, startMonth, startYear, endDay, endMonth, endYear;
    boolean isStart = true;
    ImageView imageTime;
    Calendar calendar;
    EditText title,description,location;
    ProgressBar progressBar;
    TextView errorText;
    ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("database")
                .schemaVersion(2)
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        realm = Realm.getDefaultInstance();

        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        calendar = Calendar.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        if(getSupportActionBar() != null) {
            if(Build.VERSION.SDK_INT >= 21) getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add New");
        }

        tabLayout = findViewById(R.id.tabLayout);

        viewPagerPhoto = findViewById(R.id.viewPagerPhoto);
        photoPagerAdapter = new PhotoPagerAdapter(this);
        viewPagerPhoto.setAdapter(photoPagerAdapter);
        indicator = findViewById(R.id.indicator);
        indicator.setCount(photoPagerAdapter.getCount());
        indicator.setSelected(0);
        coverLayout = findViewById(R.id.coverLayout);
        prioritiesLayout = findViewById(R.id.priorityLayout);
        friendsLayout = findViewById(R.id.friendsLayout);
        priorityText = findViewById(R.id.priorityText);
        privacyText = findViewById(R.id.privacyText);
        friendsText = findViewById(R.id.friendsText);
        privacySwitch = findViewById(R.id.privacySwitch);
        saveButton = findViewById(R.id.saveButton);
        coverFAB = findViewById(R.id.coverFAB);
        imageTime = findViewById(R.id.imageTime);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                LinearLayout tLayout = (LinearLayout)((ViewGroup) tabLayout.getChildAt(0)).getChildAt(tab.getPosition());
                TextView tabTextView = (TextView) tLayout.getChildAt(1);
                tabTextView.setTypeface(tabTextView.getTypeface(), Typeface.BOLD);
                if(tab.getPosition() == 0){
                    coverLayout.setVisibility(View.GONE);
                    privacyText.setVisibility(View.GONE);
                    privacySwitch.setVisibility(View.GONE);
                    prioritiesLayout.setVisibility(View.VISIBLE);
                    priorityText.setVisibility(View.VISIBLE);
                    friendsText.setText("Add Friends:");
                    saveButton.setText("Save Task");
                    dateText.setText("Choose a date");
                    timeText.setText("Choose a time");
                    imageTime.setImageResource(R.drawable.ic_time_black_24dp);
                }
                else if(tab.getPosition() == 1){
                    coverLayout.setVisibility(View.VISIBLE);
                    privacyText.setVisibility(View.VISIBLE);
                    privacySwitch.setVisibility(View.VISIBLE);
                    prioritiesLayout.setVisibility(View.GONE);
                    priorityText.setVisibility(View.GONE);
                    friendsText.setText("Invite Friends:");
                    saveButton.setText("Create Event");
                    dateText.setText("Starts from");
                    timeText.setText("Ends on");
                    imageTime.setImageResource(R.drawable.ic_date_range_black_24dp);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                LinearLayout tLayout = (LinearLayout)((ViewGroup) tabLayout.getChildAt(0)).getChildAt(tab.getPosition());
                TextView tabTextView = (TextView) tLayout.getChildAt(1);
                tabTextView.setTypeface(tabTextView.getTypeface(), Typeface.NORMAL);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        firstTabBold();

        coverFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhoto();
            }
        });

        dateLayout = findViewById(R.id.dateLayout);
        timeLayout = findViewById(R.id.timeLayout);
        dateText = findViewById(R.id.dateText);
        timeText = findViewById(R.id.timeText);

        dateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tabLayout.getSelectedTabPosition() == 1) isStart = true;
                Calendar now = Calendar.getInstance();

                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        AddActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setMinDate(now);
                dpd.setAccentColor(getResources().getColor(R.color.colorAccent));
                dpd.show(getSupportFragmentManager(), "Datepickerdialog");
            }
        });

        timeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tabLayout.getSelectedTabPosition() == 0) {
                    Calendar now = Calendar.getInstance();

                    TimePickerDialog tpd = TimePickerDialog.newInstance(
                            AddActivity.this,
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            now.get(Calendar.SECOND),
                            false
                    );

                    tpd.setAccentColor(getResources().getColor(R.color.colorAccent));
                    tpd.show(getSupportFragmentManager(), "Timepickerdialog");
                }
                else{
                    isStart = false;
                    Calendar now = Calendar.getInstance();

                    DatePickerDialog dpd = DatePickerDialog.newInstance(
                            AddActivity.this,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                    dpd.setMinDate(now);
                    dpd.setAccentColor(getResources().getColor(R.color.colorAccent));
                    dpd.show(getSupportFragmentManager(), "Datepickerdialog");
                }
            }
        });

        int dpToPixel = Math.round(dpToPixel(24, this));
        Glide.with(this)
                .load(R.drawable.profile_picture)
                .apply(RequestOptions.circleCropTransform())
                .override(dpToPixel, dpToPixel)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        defaultProfile = resource;
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

        priorityOrangeCircle = findViewById(R.id.priorityOrangeCircle);
        priorityOrangeRing = findViewById(R.id.priorityOrangeRing);
        priorityGreenCircle = findViewById(R.id.priorityGreenCircle);
        priorityGreenRing = findViewById(R.id.priorityGreenRing);
        priorityRedCircle = findViewById(R.id.priorityRedCircle);
        priorityRedRing = findViewById(R.id.priorityRedRing);
        priorityText = findViewById(R.id.priorityText);

        View.OnClickListener priorityClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.priorityOrangeCircle){
                    priority = 0;
                    priorityOrangeRing.setVisibility(View.VISIBLE);
                    priorityGreenRing.setVisibility(View.GONE);
                    priorityRedRing.setVisibility(View.GONE);
                    priorityText.setTextColor(getResources().getColor(R.color.textOrange));
                    priorityText.setText(R.string.priority_low);
                }
                else if(v.getId() == R.id.priorityGreenCircle){
                    priority = 1;
                    priorityOrangeRing.setVisibility(View.GONE);
                    priorityGreenRing.setVisibility(View.VISIBLE);
                    priorityRedRing.setVisibility(View.GONE);
                    priorityText.setTextColor(getResources().getColor(R.color.textGreen));
                    priorityText.setText(R.string.priority_medium);
                }
                else if(v.getId() == R.id.priorityRedCircle){
                    priority = 2;
                    priorityOrangeRing.setVisibility(View.GONE);
                    priorityGreenRing.setVisibility(View.GONE);
                    priorityRedRing.setVisibility(View.VISIBLE);
                    priorityText.setTextColor(getResources().getColor(R.color.red));
                    priorityText.setText(R.string.priority_high);
                }
            }
        };

        priorityRedCircle.setOnClickListener(priorityClickListener);
        priorityGreenCircle.setOnClickListener(priorityClickListener);
        priorityOrangeCircle.setOnClickListener(priorityClickListener);

        chipGroupFriends = findViewById(R.id.chipGroupFriends);
        fabAddFriends = findViewById(R.id.fabAddFriends);
        fabAddFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPermissionAndReadContacts();
            }
        });

        expandLayout = findViewById(R.id.expandLayout);
        expandText = findViewById(R.id.expandText);
        expandArrow = findViewById(R.id.expandArrow);
        moreOptionsLayout = findViewById(R.id.moreOptionsLayout);
        expandLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!expanded){
                    expandMoreOptions();
                    expanded = true;
                }
                else{
                    collapseMoreOptions();
                    expanded = false;
                }
            }
        });

        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        location = findViewById(R.id.location);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progessBar);
        errorText = findViewById(R.id.textError);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tabLayout.getSelectedTabPosition() == 0) {
                    if (title.getText().toString().equals("")) {
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText("Please enter a title!");
                    } else if (dateText.getText().toString().equals("Choose a date")) {
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText("Please choose a date!");
                    } else if (timeText.getText().toString().equals("Choose a time")) {
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText("Please choose a time!");
                    } else if(friendsList.size() > 0 && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                            && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED){
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText("Please check your internet connection!");
                    } else {
                        saveButton.setVisibility(View.GONE);
                        errorText.setVisibility(View.GONE);
                        if (friendsList.size() > 0) progressBar.setVisibility(View.VISIBLE);
                        if (tabLayout.getSelectedTabPosition() == 0) {
                            Task task = new Task(title.getText().toString(), description.getText().toString(), dateText.getText().toString(), timeText.getText().toString(), location.getText().toString(), calendar.getTimeInMillis(), priority, friendsList);
                            if (friendsList.size() > 0) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("tasks").child(Long.toString(calendar.getTimeInMillis()));
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("title", task.getTitle());
                                hashMap.put("description", task.getDescription());
                                hashMap.put("time", task.getTime());
                                hashMap.put("date", task.getDate());
                                hashMap.put("priority", task.getPriority());
                                hashMap.put("location", task.getLocation());
                                ref.setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child("tasks").child(Long.toString(task.getTimeInMillis())).child("friends");
                                                HashMap<String, Object> hashMap1 = new HashMap<>();
                                                for (Friend friend : friendsList) {
                                                    DatabaseReference friendRef = FirebaseDatabase.getInstance().getReference().child("users").child(friend.getPhoneNumber()).child("taskRequests").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child(Long.toString(task.getTimeInMillis()));
                                                    friendRef.setValue(true);
                                                    int position = friendsList.indexOf(friend);
                                                    hashMap1.put(Integer.toString(position), friend.getPhoneNumber());
                                                }
                                                taskRef.setValue(hashMap1);
                                                task.setOnline(true);
                                                addTaskToRealm(task);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                saveButton.setVisibility(View.VISIBLE);
                                                progressBar.setVisibility(View.GONE);
                                                errorText.setVisibility(View.VISIBLE);
                                                errorText.setText("Failed to connect to database!");
                                            }
                                        });

                            } else {
                                addTaskToRealm(task);
                                startUploadWorker();
                            }

                        }
                    }
                }

            }
        });
    }

    void addTaskToRealm(Task task){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(task);
                onBackPressed();
            }
        });
    }

    void startUploadWorker(){
        long id = calendar.getTimeInMillis();

        Constraints networkConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest.Builder uploadRequestBuilder = new OneTimeWorkRequest.Builder(UploadWorker.class);
        uploadRequestBuilder.setConstraints(networkConstraints);
        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putLong("id", id);
        uploadRequestBuilder.setInputData(dataBuilder.build());
        OneTimeWorkRequest uploadRequest = uploadRequestBuilder.build();

        WorkManager.getInstance(this)
                .enqueue(uploadRequest);
    }

    private void expandMoreOptions() {
        moreOptionsLayout.setVisibility(View.VISIBLE);
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(expandArrow, "rotation", 0, 90);
        anim1.setDuration(400);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(moreOptionsLayout, "alpha", 0, 1);
        anim2.setDuration(300);
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(expandText, "alpha", 1, 0);
        anim3.setDuration(500);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(anim1, anim2, anim3);
        set.start();
        ExpandCollapseAnimation.expand(moreOptionsLayout);
    }

    private void collapseMoreOptions() {
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(expandArrow, "rotation", 90, 0);
        anim1.setDuration(400);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(moreOptionsLayout, "alpha", 1, 0);
        anim2.setDuration(300);
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(expandText, "alpha", 0, 1);
        anim3.setDuration(300);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(anim1, anim2, anim3);
        set.start();
        ExpandCollapseAnimation.collapse(moreOptionsLayout);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                moreOptionsLayout.setVisibility(View.GONE);
            }
        }, 400);
    }

    void getPermissionAndReadContacts(){
        String[] perms = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(AddActivity.this, perms)) {
            showFriendPickerDialog();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(AddActivity.this, getString(R.string.permission_rationale),
                    100, perms);
        }
    }

    void showFriendPickerDialog(){
        View view = LayoutInflater.from(AddActivity.this).inflate(R.layout.layout_dialog_friend_picker, null, false);
        RecyclerView friendPickerRecyclerView = view.findViewById(R.id.contactPickerRecyclerView);
        friendPickerRecyclerView.setLayoutManager(new LinearLayoutManager(AddActivity.this));
        final AdapterContactPicker adapter = new AdapterContactPicker(AddActivity.this, friendsList);
        friendPickerRecyclerView.setAdapter(adapter);

        final AlertDialog dialog = new AlertDialog.Builder(AddActivity.this)
                .setTitle("Add Friends")
                .setView(view)
                .setCancelable(false)
                .show();
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        Button buttonDone = view.findViewById(R.id.buttonDone);
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                friendsList.addAll(adapter.getSelectedContacts());
                for(final Friend friend: adapter.getSelectedContacts()) {
                    final Chip chip = new Chip(AddActivity.this);
                    chip.setText(friend.getName());
                    if(friend.getProfilePicPath().equals(""))
                        chip.setChipIcon(defaultProfile);
                    else
                        Glide.with(AddActivity.this).load(friend.getProfilePicPath()).apply(RequestOptions.circleCropTransform()).into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                chip.setChipIcon(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                chip.setChipIcon(defaultProfile);
                            }
                        });
                    chip.setCloseIconVisible(true);
                    chipGroupFriends.addView(chip);
                    chip.setOnCloseIconClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            friendsList.remove(friend);
                            chipGroupFriends.removeView(chip);
                        }
                    });
                }
            }
        });
        EditText search = view.findViewById(R.id.search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
            }
        });
    }

    void firstTabBold(){
        LinearLayout tLayout = (LinearLayout)((ViewGroup) tabLayout.getChildAt(0)).getChildAt(0);
        TextView tabTextView = (TextView) tLayout.getChildAt(1);
        tabTextView.setTypeface(tabTextView.getTypeface(), Typeface.BOLD);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return true;
    }

    public static float dpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        showFriendPickerDialog();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
        getPermissionAndReadContacts();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            getPermissionAndReadContacts();
        }
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);

            Uri uri = Uri.fromFile(new File(images.get(0).getPath()));
            File dirFile = new File(Environment.getExternalStorageDirectory(), "MeanTime/Events/Covers");
            if(!dirFile.exists())
                dirFile.mkdirs();
            path = Environment.getExternalStorageDirectory()+"/MeanTime/Events/Covers/"+System.currentTimeMillis()+".png";
            Uri uriDestination = Uri.fromFile(new File(path));
            UCrop.Options options = new UCrop.Options();
            options.setHideBottomControls(true);
            options.setCompressionFormat(Bitmap.CompressFormat.PNG);
            options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.NONE, UCropActivity.NONE);
            UCrop.of(uri, uriDestination)
                    .withOptions(options)
                    .withAspectRatio(2.1f, 1)
                    .withMaxResultSize(1680, 800)
                    .start(this);
        }
        if(requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK){
            photoPagerAdapter.addPath(path);
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String dateS = "";
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        dateS = DateFormat.format("dd MMM yyyy", calendar).toString();
        if(tabLayout.getSelectedTabPosition() == 0) {
            dateText.setText(dateS);
        }
        else{
            if(isStart){
                startDay = dayOfMonth;
                startMonth = monthOfYear;
                startYear = year;
                dateText.setText(dateS);
            }
            else{
                endDay = dayOfMonth;
                endMonth = monthOfYear;
                endYear = year;
                timeText.setText(dateS);
            }
        }
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        String timeS = DateFormat.format("hh:mm AA", calendar).toString();
        timeText.setText(timeS);
    }

    void pickPhoto(){
        ImagePicker.with(this)                         //  Initialize ImagePicker with activity or fragment context
                .setToolbarColor("#FFFFFF")         //  Toolbar color
                .setStatusBarColor("#EFEFEF")       //  StatusBar color (works with SDK >= 21  )
                .setToolbarTextColor("#000000")     //  Toolbar text color (Title and Done button)
                .setToolbarIconColor("#444444")     //  Toolbar icon color (Back and Camera button)
                .setProgressBarColor("#11A0FF")     //  ProgressBar color
                .setBackgroundColor("#FFFFFF")      //  Background color
                .setCameraOnly(false)               //  Camera mode
                .setMultipleMode(false)              //  Select multiple images or single image
                .setFolderMode(true)                //  Folder mode
                .setShowCamera(true)                //  Show camera button
                .setFolderTitle("Albums")           //  Folder title (works with FolderMode = true)
                .setImageTitle("Photos")            //  Image title (works with FolderMode = false)
                .setDoneTitle("Done")               //  Done button title
                .setLimitMessage("You have reached selection limit")    // Selection limit message
                .setMaxSize(1)                     //  Max images can be selected
                .setAlwaysShowDoneButton(true)      //  Set always show done button in multiple mode
                .setRequestCode(100)                //  Set request code, default Config.RC_PICK_IMAGES
                .setKeepScreenOn(true)              //  Keep screen on when selecting images
                .start();
    }

    class PhotoPagerAdapter extends PagerAdapter{

        List<String> pathsList = new ArrayList<>();

        int[] images = {R.drawable.event_cover, R.drawable.event_cover2, R.drawable.event_cover3, R.drawable.event_cover4};
        Context context;
        public PhotoPagerAdapter(Context context){
            this.context = context;
            pathsList.add("cover1");
            pathsList.add("cover2");
            pathsList.add("cover3");
            pathsList.add("cover4");
        }

        void addPath(String path){
            pathsList.add(path);
            notifyDataSetChanged();
            indicator.setCount(getCount());
            viewPagerPhoto.setCurrentItem(getCount()-1);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View v = LayoutInflater.from(context).inflate(R.layout.layout_photo_pager, container, false);
            ImageView image = v.findViewById(R.id.image);
            if(position < 4)
                image.setImageResource(images[position]);
            else
                Glide.with(context).load(pathsList.get(position)).into(image);
            container.addView(v);
            return v;
        }

        @Override
        public int getCount() {
            return pathsList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View)object);
        }
    }
}
