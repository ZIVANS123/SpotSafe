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

public class ApprovalsFragment extends Fragment {////

    //Declare variables---------------------------------------------------------------------------------------
    private ListView eventListView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private DB db;
    private String userName;

    public ApprovalsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_approvals, container, false);

        eventListView = view.findViewById(R.id.reportsListView);//Initialize the ListView
        eventList = new ArrayList<>(); // Initialize the eventList

        Context context = getContext(); //Get the context of the fragment

        if (context != null) {

            db = DB.getInstance(context);//Get an instance of the DB object
            db.open();

            // Retrieve the username from shared preferences
            userName = MainActivity.getSharedPreferences().getString("user", null);

            try { // Get the accepted events for the user from the database

                eventList.addAll(db.getApprovedEventsForUser(userName));
            } catch (Exception e) {
                e.printStackTrace();
                // Handle the exception or show an error message
            } finally {
                //Close the database connection
                db.close();
            }
            // Create an EventAdapter and set it to the ListView
            eventAdapter = new EventAdapter(context, eventList);
            eventListView.setAdapter(eventAdapter);
            eventAdapter.notifyDataSetChanged();

        }

        return view;
    }


}
