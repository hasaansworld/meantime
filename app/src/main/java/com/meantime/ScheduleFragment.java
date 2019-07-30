package com.meantime;

import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.transitionseverywhere.ChangeText;


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
    TextSwitcher dateSwitcher, monthSwitcher, yearSwitcher;
    int counter = 11;

    private OnItemSelectedListener itemSelectedListener;
    int filterPosition = 0;
    int prevPosition = 0;

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
        View v = inflater.inflate(R.layout.fragment_schedule, container, false);
        recyclerView = v.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int position = layoutManager.findFirstVisibleItemPosition();
                if(position != prevPosition && filterPosition == 3 || position != prevPosition && filterPosition == 4) {
                    prevPosition = position;
                    String newDate = adapter.getDateAt(position).substring(0, 2);
                    if (!((TextView) dateSwitcher.getCurrentView()).getText().toString().equals(newDate)) {
                        dateSwitcher.setText(newDate);
                    }
                    String newMonth = adapter.getDateAt(position).substring(3, 6);
                    if(!((TextView) monthSwitcher.getCurrentView()).getText().toString().equals(newMonth)){
                        monthSwitcher.setText(newMonth);
                    }
                }
            }
        });

        switcherLayout = v.findViewById(R.id.switcherLayout);
        dateSwitcher = v.findViewById(R.id.dateSwitcher);
        monthSwitcher = v.findViewById(R.id.monthSwitcher);
        yearSwitcher = v.findViewById(R.id.yearSwitcher);

        return v;
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

        if(filterPosition <= 2)
            switcherLayout.setVisibility(View.GONE);
        else{
            switcherLayout.setVisibility(View.VISIBLE);
            dateSwitcher.setVisibility(View.VISIBLE);
            monthSwitcher.setVisibility(View.VISIBLE);
            yearSwitcher.setVisibility(View.VISIBLE);
            int position = layoutManager.findFirstVisibleItemPosition();
            if(position == -1) position = 0;
            String date = adapter.getDateAt(position);
            String dateInMonth = date.substring(0, 2);
            String month = date.substring(3, 6);
            String year = date.substring(7, 11);
            dateSwitcher.setCurrentText(dateInMonth);
            monthSwitcher.setCurrentText(month);
            yearSwitcher.setCurrentText(year);
        }
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
