package com.meantime;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import io.realm.Realm;
import io.realm.RealmList;

public class DetailsActivity extends AppCompatActivity {

    long id;
    String titleText;
    Realm realm;
    TextView title, date, time, description;
    LinearLayout friendsLayout;
    RecyclerView friendsRecyclerView;
    AdapterFriendsDetails adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        realm = RealmUtils.getRealm();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        if(getSupportActionBar() != null) {
            if(Build.VERSION.SDK_INT >= 21) getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        titleText = getIntent().getStringExtra("title");
        id = getIntent().getLongExtra("id", 0);
        title = findViewById(R.id.title);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        description = findViewById(R.id.description);
        friendsLayout = findViewById(R.id.friendsLayout);

        Task task = realm.where(Task.class).equalTo("timeInMillis", id).equalTo("title", titleText).equalTo("isInTrash", false).findFirst();
        title.setText(titleText);
        date.setText(task.getDate());
        time.setText(task.getTime());
        description.setText(task.getDescription());

        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        RealmList<Friend> friendsList = task.getFriendsList();
        if(friendsList.size() > 0){
            friendsLayout.setVisibility(View.VISIBLE);

            friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdapterFriendsDetails(this, friendsList);
            friendsRecyclerView.setAdapter(adapter);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return true;
    }

    DividerItemDecoration getDividers(){
        int[] ATTRS = new int[]{android.R.attr.listDivider};

        TypedArray a = obtainStyledAttributes(ATTRS);
        Drawable divider = a.getDrawable(0);
        int inset = getResources().getDimensionPixelSize(R.dimen.divider_margin);
        InsetDrawable insetDivider = new InsetDrawable(divider, inset, 0, inset, 0);
        a.recycle();

        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(insetDivider);
        return itemDecoration;
    }
}
