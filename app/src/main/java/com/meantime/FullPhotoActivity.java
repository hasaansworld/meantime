package com.meantime;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;

public class FullPhotoActivity extends AppCompatActivity {
    PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_photo);

        if(getSupportActionBar() != null){
            if(getIntent().getStringExtra("title") != null)getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
            else
                getSupportActionBar().setTitle("Photo");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        photoView = findViewById(R.id.photoView);
        String path = getIntent().getStringExtra("path");
        if(path != null) {
            Glide.with(this).load(path).diskCacheStrategy(DiskCacheStrategy.NONE).into(photoView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return true;
    }
}
