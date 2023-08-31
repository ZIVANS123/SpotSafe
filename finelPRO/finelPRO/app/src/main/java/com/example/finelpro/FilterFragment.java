package com.example.finelpro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import com.example.finelpro.Event;
import com.example.finelpro.EventAdapter;

public class FilterFragment extends Fragment {////
    private ListView eventListView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private DB db;
    private String userName;

    public FilterFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
        eventListView = view.findViewById(R.id.eventListView);

        eventList = new ArrayList<>(); // Initialize the eventList
        Context context = getContext();
        if (context != null) {
            db = DB.getInstance(context); // Initialize the DB object
            db.open();

            // username with the username you want to retrieve events for
            userName = MainActivity.getSharedPreferences().getString(getString(R.string.CURRENTUSER), "a");
            eventList.addAll(db.getAllmyEvents(userName));

            db.close();

            eventAdapter = new EventAdapter(context, eventList);
            eventListView.setAdapter(eventAdapter);
            eventAdapter.notifyDataSetChanged();


        }

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        // Refresh event list when the fragment resumes
        if (db != null) {
            db.open();

            // Replace "username" with the actual username you want to retrieve events for
            String username = MainActivity.getSharedPreferences().getString(getString(R.string.CURRENTUSER), "a");
            eventList.clear();
            eventList.addAll(db.getAllmyEvents(username));

            db.close();

            eventAdapter.notifyDataSetChanged();
        }
    }
}