package com.example.finelpro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class SortFragment extends Fragment {////

    private ListView eventListView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private DB db;

    public SortFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Retrieve necessary data or arguments if any
        }
    }
    // inflates the user interface, retrieves a list of events from the local database,
    // sorts the events based on their dates in descending order, creates an adapter for the sorted event list,
    // and sets the adapter to the ListView (eventsListView) to display the events in the UI in the sorted order.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sort, container, false);

        eventListView = view.findViewById(R.id.eventsListView);

        Context context = getContext();

        if (context != null) {
            db = DB.getInstance(context); // Initialize the DB object
            db.open();
            eventList = new ArrayList<>();
            eventList = db.getAllEvents();// Get the event list from the local database

            db.close();

            Collections.sort(eventList, new Comparator<Event>() {
                @Override
                public int compare(Event event1, Event event2) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    try {
                        Date date1 = dateFormat.parse(event1.getDate());
                        Date date2 = dateFormat.parse(event2.getDate());
                        return date2.compareTo(date1);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            });


            // Create an adapter for the event list
            eventAdapter = new EventAdapter(getActivity(), eventList);

            // Set the adapter to the ListView
            eventListView.setAdapter(eventAdapter);




        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh event list when the fragment resumes
        if (db != null) {
            db.open();
            eventList = db.getAllEvents();
            db.close();

            Collections.sort(eventList, new Comparator<Event>() {
                @Override
                public int compare(Event event1, Event event2) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    try {
                        Date date1 = dateFormat.parse(event1.getDate());
                        Date date2 = dateFormat.parse(event2.getDate());

                        return date2.compareTo(date1);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            });


            eventAdapter.clear();
            eventAdapter.addAll(eventList);
            eventAdapter.notifyDataSetChanged();
        }


    }
}