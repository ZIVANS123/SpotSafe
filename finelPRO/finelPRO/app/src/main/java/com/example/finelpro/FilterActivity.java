package com.example.finelpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends AppCompatActivity {////
    private Spinner riskLevelSpinner;
    private Spinner regionSpinner;
    private Spinner eventTypeSpinner;
    private Button applyFiltersButton;
    private ListView filteredEventsListView;
    private EventAdapter eventAdapter;
    private DB db;
    private Context context = this; //Get the context of the activity
    private List<Event> filteredEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        //Initialize UI elements----------------------------------------------
        riskLevelSpinner = findViewById(R.id.riskLevelSpinner);
        regionSpinner = findViewById(R.id.regionSpinner);
        eventTypeSpinner = findViewById(R.id.eventTypeSpinner);
        applyFiltersButton = findViewById(R.id.applyFiltersButton);
        filteredEventsListView = findViewById(R.id.filteredEventsListView);

        // Set up adapters for spinners
        ArrayAdapter<CharSequence> riskLevelAdapter = ArrayAdapter.createFromResource(this,
                R.array.risk_levels, android.R.layout.simple_spinner_item);
        riskLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        riskLevelSpinner.setAdapter(riskLevelAdapter);

        ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(this,
                R.array.regions, android.R.layout.simple_spinner_item);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regionAdapter);

        ArrayAdapter<CharSequence> eventTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.event_types, android.R.layout.simple_spinner_item);
        eventTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(eventTypeAdapter);

        // Initialize the EventAdapter for the ListView
        eventAdapter = new EventAdapter(this, new ArrayList<>());
        filteredEventsListView.setAdapter(eventAdapter);


        applyFiltersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected filter options
                String selectedRiskLevel = riskLevelSpinner.getSelectedItem().toString();
                String selectedRegion = regionSpinner.getSelectedItem().toString();
                String selectedEventType = eventTypeSpinner.getSelectedItem().toString();

                // Filter the events based on the selected options
                filteredEvents = filterEvents(selectedRiskLevel, selectedRegion, selectedEventType);

                // Update the ListView with the filtered events
                eventAdapter.clear();
                eventAdapter.addAll(filteredEvents);
                eventAdapter.notifyDataSetChanged();
            }
        });
    }

    private List<Event> filterEvents(String riskLevel, String region, String eventType) {
        List<Event> events = new ArrayList<>(); // Create a list to store the filtered events


        if (context != null) {


            db = DB.getInstance(context); // Initialize the DB object
            db.open();


            for (Event event : db.getAllEvents()) {
                // Check if the event matches the selected filter options

                if (event.getRiskLevel().equals(riskLevel) &&
                        event.getArea().equals(region) &&
                        event.getType().equals(eventType)) {
                    // Add the matching event to the list of filtered events
                    events.add(event);
                }
            }
            db.close();
        }
        return events;
    }


}
