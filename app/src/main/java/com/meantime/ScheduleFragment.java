package com.meantime;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.shuhart.stickyheader.StickyHeaderItemDecorator;

import java.util.Calendar;


public class ScheduleFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RecyclerView recyclerView;
    ScheduleAdapter adapter;
    LinearLayoutManager layoutManager;
    LinearLayout switcherLayout;
    TextView dateSwitcher;
    //monthSwitcher, yearSwitcher;

    String dateToday, dateYesterday, dateTomorrow;

    private OnItemSelectedListener itemSelectedListener;
    int filterPosition = 0;
    int prevPosition = -1;

    public ScheduleFragment() {
        // Required empty public constructor
    }

    public static ScheduleFragment newInstance(String param1, String param2) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        updateDates();
        View v = inflater.inflate(R.layout.fragment_schedule, container, false);
        recyclerView = v.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, new RecyclerItemTouchHelper.RecyclerItemTouchHelperListener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                adapter.removeItem(position);
            }
        });
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        /*recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                updateCurrentPosition();
            }
        });*/


        //recyclerView.setItemAnimator(new CustomItemAnimator());

        switcherLayout = v.findViewById(R.id.switcherLayout);
        dateSwitcher = v.findViewById(R.id.dateSwitcher);
        //monthSwitcher = v.findViewById(R.id.monthSwitcher);
        //yearSwitcher = v.findViewById(R.id.yearSwitcher);

        return v;
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private Bitmap convertDrawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    private void updateCurrentPosition() {
        if(adapter.getItemCount() > 0) {
            int position = layoutManager.findFirstVisibleItemPosition();
            if (position == -1) position = adapter.getNearestPosition();
            if(position == 0){
                switcherLayout.setVisibility(View.GONE);
            }
            else if (position != prevPosition && adapter.filterPosition > 2) {
                switcherLayout.setVisibility(View.VISIBLE);
                prevPosition = position;
                String ndate = adapter.getDateAt(position);
                String prevText = dateSwitcher.getText().toString();
                if (ndate.equals(dateToday) && filterPosition <= 4) {
                    if (!prevText.equals("Today")) dateSwitcher.setText("Today");
                } else if (ndate.equals(dateYesterday) && filterPosition <= 4) {
                    if (!prevText.equals("Yesterday")) dateSwitcher.setText("Yesterday");
                } else if (ndate.equals(dateTomorrow) && filterPosition <= 4) {
                    if (!prevText.equals("Tomorrow")) dateSwitcher.setText("Tomorrow");
                } else {
                    if (!prevText.equals(ndate))
                        dateSwitcher.setText(ndate);
                }

            }
        }
    }

    private void updateDates() {
        Calendar calendar = Calendar.getInstance();
        dateToday = DateFormat.format("dd MMM yyyy", calendar).toString();
        calendar.add(Calendar.DATE, -1);
        dateYesterday = DateFormat.format("dd MMM yyyy", calendar).toString();
        calendar.add(Calendar.DATE, 2);
        dateTomorrow = DateFormat.format("dd MMM yyyy", calendar).toString();
    }

    public void filterSchedule(){
        if(filterPosition <= 6) adapter.setFilterPosition(filterPosition);
        if(filterPosition == 0)
            adapter.date(0, true);
        else if(filterPosition == 1)
            adapter.date(-1, true);
        else if(filterPosition == 2)
            adapter.date(1, true);
        else if(filterPosition == 3)
            adapter.week(true);
        else if(filterPosition == 4)
            adapter.month(true);
        else if(filterPosition == 5)
            adapter.year(true);
        else if(filterPosition == 6)
            adapter.allTime(true);
        else if(filterPosition == 7)
            adapter.priority(0);
        else if(filterPosition == 8)
            adapter.priority(1);
        else if(filterPosition == 9)
            adapter.priority(2);

        recyclerView.scrollToPosition(adapter.getNearestPosition());

        /*if(adapter.filterPosition <= 2) {
            switcherLayout.setVisibility(View.GONE);
        }
        else{
            switcherLayout.setVisibility(View.VISIBLE);
            dateSwitcher.setVisibility(View.VISIBLE);
            updateCurrentPosition();
        }*/

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter = new ScheduleAdapter(getContext());
        recyclerView.setAdapter(adapter);
        if(filterPosition != 0)
            filterSchedule();
    }

    public void setOnItemSelectedListener(OnItemSelectedListener itemSelectedListener){
        this.itemSelectedListener = itemSelectedListener;
    }

    public interface OnItemSelectedListener {
        void onItemSelected(Uri uri);
    }

}
