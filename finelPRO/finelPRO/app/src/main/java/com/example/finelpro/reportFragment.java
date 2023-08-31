package com.example.finelpro;

import static com.example.finelpro.MainActivity.getSharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class reportFragment extends Fragment {////
    private DB db;
    private String userName;
    public reportFragment() {
        // Required empty public constructor
    }

    //method inflates and configures the user interface for a fragment,
    // displaying statistics related to reported events, approved events,
    // rejected events, upload points, and operations points based on data retrieved from the local database.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        userName = MainActivity.getSharedPreferences().getString("user", null);
        TextView textReportedEvents = view.findViewById(R.id.text_reported_events);
        TextView textApprovedEvents = view.findViewById(R.id.text_approved_events);
        TextView textRejectedEvents = view.findViewById(R.id.text_rejected_events);
        TextView uploadPoints = view.findViewById(R.id.uploadPoints);
        TextView operationsPoints = view.findViewById(R.id.operationsPoints);

        int rejectedEvents;
        int approvedEvents;
        int uploadEvents;
        int uploadEventsPoints;
        Context context = getContext();
        if (context != null) {
            db = DB.getInstance(context); // Initialize the DB object
            db.open();
            rejectedEvents = db.getRejectedEventCountForUser(userName);
            approvedEvents = db.getApprovedEventCountForUser(userName);
            uploadEvents = db.getAllmyEvents(userName).size();
            uploadEventsPoints = db.getEventsApprovedByMajorityCountForUser(userName);
            textReportedEvents.setText(String.valueOf(uploadEvents));
            textApprovedEvents.setText(String.valueOf(approvedEvents));
            textRejectedEvents.setText(String.valueOf(rejectedEvents));
            uploadPoints.setText(String.valueOf(uploadEventsPoints));
            operationsPoints.setText(String.valueOf((rejectedEvents + approvedEvents) * 3));
            db.close();
        }

        return view;
    }
}
