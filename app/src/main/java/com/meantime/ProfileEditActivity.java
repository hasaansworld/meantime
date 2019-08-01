package com.meantime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity {

    RoundedImageView profilePicture;
    ImageView buttonPhoto;
    ProgressBar progressBar;
    MaterialButton buttonSave;
    EditText firstName, lastName;
    RadioGroup radioGroupGender;
    TextView textError;
    String path = "";
    TextInputLayout aboutLayout;
    EditText about;
    ConnectivityManager connectivityManager;
    SharedPreferences sharedPreferences;
    boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Setup Your Profile");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        profilePicture = findViewById(R.id.profilePicture);
        buttonPhoto = findViewById(R.id.buttonPhoto);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        about = findViewById(R.id.about);
        aboutLayout = findViewById(R.id.aboutLayout);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        progressBar = findViewById(R.id.progessBar);
        buttonSave = findViewById(R.id.saveButton);
        textError = findViewById(R.id.textError);

        if (getIntent().getExtras() != null)
            isEditing = true;

        if(isEditing){
            if(getSupportActionBar() != null){
                getSupportActionBar().setTitle("Edit Profile");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            aboutLayout.setVisibility(View.VISIBLE);
            path = sharedPreferences.getString("profilePicPath", "");
            if(!path.equals(""))
                Glide.with(this).asBitmap().load(path).placeholder(R.drawable.profile_picture).into(profilePicture);
            String name = sharedPreferences.getString("name", "");
            if(!name.equals("") && name.contains(" ")){
                String[] names = name.split(" ");
                firstName.setText(names[0]);
                lastName.setText(names[1]);
            }
            about.setText(sharedPreferences.getString("about", ""));
            int gender = sharedPreferences.getInt("gender", 0);
            int[] genderIds = {R.id.radioMale, R.id.radioFemale, R.id.radioOthers};
            radioGroupGender.check(genderIds[gender]);
        }

        buttonPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhoto();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textError.setVisibility(View.GONE);
                int radioButtonPosition = getGender();
                if (firstName.getText().toString().equals("")) {
                    textError.setVisibility(View.VISIBLE);
                    textError.setText("Please enter your first name!");
                } else if (lastName.getText().toString().equals("")) {
                    textError.setVisibility(View.VISIBLE);
                    textError.setText("Please enter your last name!");
                } else if (radioButtonPosition == -1) {
                    textError.setVisibility(View.VISIBLE);
                    textError.setText("Please specify your gender!");
                }else if ( connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                        && connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED ) {
                    textError.setVisibility(View.VISIBLE);
                    textError.setText("Please check your internet connection!");
                } else {
                    buttonSave.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    final String userId = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

                    HashMap hashMap = new HashMap();
                        hashMap.put("name", firstName.getText().toString() + " " + lastName.getText().toString());
                        hashMap.put("gender", radioButtonPosition);
                        if(isEditing) hashMap.put("about", about.getText().toString());
                        userRef.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (!path.equals("") && !path.equals(sharedPreferences.getString("profilePicPath", ""))) {
                                    Uri file = Uri.fromFile(new File(path));
                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    StorageReference profilePictureRef = storage.getReference().child("users").child(userId).child("profile picture").child(file.getLastPathSegment());
                                    profilePictureRef.putFile(file)
                                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(firstName.getText().toString() + " " + lastName.getText().toString()).build();
                                                    user.updateProfile(profileUpdates);
                                                    createProfile();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    textError.setVisibility(View.VISIBLE);
                                                    textError.setText("Failed to upload your profile picture.");
                                                    buttonSave.setVisibility(View.VISIBLE);
                                                    progressBar.setVisibility(View.GONE);
                                                }
                                            });
                                } else {
                                    createProfile();
                                }
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        textError.setVisibility(View.VISIBLE);
                                        textError.setText("Failed to save your profile data.");
                                        buttonSave.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                }
            }
        });

    }

    private int getGender() {
        int radioButtonPosition = -1;
        int[] ids = {R.id.radioMale, R.id.radioFemale, R.id.radioOthers};
        for (int i = 0; i < ids.length; i++) {
            int id = ids[i];
            if (radioGroupGender.getCheckedRadioButtonId() == id)
                radioButtonPosition = i;
        }
        return radioButtonPosition;
    }

    private void createProfile() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", firstName.getText().toString() + " " + lastName.getText().toString());
        editor.putString("profilePicPath", path);
        editor.putBoolean("profileDone", true);
        editor.putInt("gender", getGender());
        editor.putString("about", about.getText().toString());
        editor.apply();
        finish();
    }

    void pickPhoto() {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            Uri uri = Uri.fromFile(new File(images.get(0).getPath()));
            File dirFile = new File(Environment.getExternalStorageDirectory(), "MeanTime");
            if (!dirFile.exists())
                dirFile.mkdir();
            path = Environment.getExternalStorageDirectory() + "/MeanTime/profile picture "+System.currentTimeMillis()+".png";
            Uri uriDestination = Uri.fromFile(new File(path));
            UCrop.Options options = new UCrop.Options();
            options.setHideBottomControls(true);
            options.setCompressionFormat(Bitmap.CompressFormat.PNG);
            options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.NONE, UCropActivity.NONE);
            UCrop.of(uri, uriDestination)
                    .withOptions(options)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(800, 800)
                    .start(this);

        }
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            final Uri resultUri = UCrop.getOutput(data);
            profilePicture.setImageURI(resultUri);
        }
    }
}
