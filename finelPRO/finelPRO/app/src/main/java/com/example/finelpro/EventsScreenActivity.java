package com.example.finelpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class EventsScreenActivity extends AppCompatActivity {////
    private ListView eventsListView;
    private TextView usernameTextView;
    private TextView lastLoginTextView;
    private static EventAdapter eventAdapter;/////**
    private Button addEventButton;
    private DB db;
    private Context context = this;
    private String userName = MainActivity.getSharedPreferences().getString("user", null);
    private FirebaseAuth firebaseAuth;
    private static boolean delayNeeded;//A variable that tells us if a delay is needed, helps in synchronizing and displaying the data from the FB to the local database


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_screen);
        //Initialize views
        eventsListView = findViewById(R.id.eventsListView);
        addEventButton = findViewById(R.id.addEventButton);
        usernameTextView = findViewById(R.id.usernameTextView);
        lastLoginTextView = findViewById(R.id.lastLoginTextView);
        firebaseAuth = FirebaseAuth.getInstance();

        usernameTextView.setText("Hello " + userName);
        lastLoginTextView.setText("last Sign In: " + MainActivity.getSharedPreferences().getString("lastLogin", null));

        db = DB.getInstance(context);
        db.open();

        //Get all events from database
        List<Event> eventList = db.getAllEvents();
        db.close();

        //Create and set the event adapter for the list view
        eventAdapter = new EventAdapter(this, eventList);
        eventsListView.setAdapter(eventAdapter);
        //Add event button click listener
        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start AddEventActivity to add a new event
                Intent intent = new Intent(EventsScreenActivity.this, AddEventActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        // List view item click listener
        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Get the selected event
                Event selectedEvent = eventAdapter.getItem(position);
                if (selectedEvent != null) {
                    //Start EventDetailsActivity to display event details
                    Intent intent = new Intent(EventsScreenActivity.this, EventDetailsActivity.class);
                    intent.putExtra("eventDetails", selectedEvent.getId());

                    startActivity(intent);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Create Options menu

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle options menu item selection
        switch (item.getItemId()) {
            case R.id.menuitem_myuploaded: {
                setHideDialog();
                FilterFragment filterFragment = new FilterFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, filterFragment)
                        .addToBackStack(null)
                        .commit();

                return true;
            }
            case R.id.menuitem_house: {
                //Start EventScreenActivity to display events
                //We only need a delay from the login and registration screen,
                // as the local database is not enough to update while displaying the events on the splash screen
                EventsScreenActivity.setDelayNeeded(false);
                Intent intent = new Intent(this, EventsScreenActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.menuitem_report: {
                setHideDialog();
                //Create an instance of the reportFragment

                reportFragment reportFragment = new reportFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, reportFragment)
                        .addToBackStack(null)
                        .commit();


                return true;
            }
            case R.id.menuitem_approvals: {
                setHideDialog();
                //Create an instance of the ApprovalsFragment

                ApprovalsFragment approvalsFragment = new ApprovalsFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, approvalsFragment)
                        .addToBackStack(null)
                        .commit();
                return true;
            }

            case R.id.menuitem_sort: {

                //Create an instance of the SortFragment

                setHideDialog();
                SortFragment sortFragment = new SortFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, sortFragment)
                        .addToBackStack(null)
                        .commit();
                return true;
            }
            case R.id.menuitem_filter: {
                //Start FilterActivity to apply filters
                Intent intent = new Intent(EventsScreenActivity.this, FilterActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.menuitem_logout: {
                //Navigate to Reconnect
                firebaseAuth.signOut();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            }

            case R.id.menuitem_myComments: {
                //Navigate to my comments
                setHideDialog();
                MyComments myComments = new MyComments();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, myComments)
                        .addToBackStack(null)
                        .commit();
                return true;
            }

            case R.id.menuitem_General_Indices: {
                //Navigate to general indices
                setHideDialog();
                GeneralIndicesFragment generalIndicesFragment = new GeneralIndicesFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, generalIndicesFragment)
                        .addToBackStack(null)
                        .commit();
                return true;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    public void setHideDialog() {
        //Hide dialog
        eventsListView.setVisibility(View.INVISIBLE);
        addEventButton.setVisibility(View.INVISIBLE);
        usernameTextView.setVisibility(View.INVISIBLE);
        lastLoginTextView.setVisibility(View.INVISIBLE);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {///*
        //Handle activity result
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Event event = (Event) data.getSerializableExtra("event");
            if (event != null) {
                //Add event to the adapter and database
                eventAdapter.add(event);
                eventAdapter.notifyDataSetChanged();
                db.open();
                db.addNewEvent(event);
                db.close();

            }
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            //Existing event updated
            Event event = (Event) data.getSerializableExtra("event");
            if (event != null) {
                //Update event in the adapter and database
                int position = eventAdapter.getPosition(event);
                if (position != -1) {
                    eventAdapter.remove(eventAdapter.getItem(position));
                    eventAdapter.insert(event, position);
                    eventAdapter.notifyDataSetChanged();

                    db.open();
                    db.updateEvent(event);
                    db.close();

                }
            }
        }
    }

    //There are 2 situations here,
    // or a delay is needed (as soon as the user comes from the registration/login screen),
    // the reason is that the events screen is not enough to be updated from FB and is empty,
    // so we made a short delay and then we update the display
    protected void onResume() {
        //Refresh event list when the activity resumes
        super.onResume();
        if (delayNeeded) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    db.open();
                    List<Event> eventList = db.getAllEvents();
                    db.close();
                    eventAdapter.clear();
                    eventAdapter.addAll(eventList);
                    eventAdapter.notifyDataSetChanged();

                }
            }, 2500);
        } else {
            db.open();
            List<Event> eventList = db.getAllEvents();
            db.close();
            eventAdapter.clear();
            eventAdapter.addAll(eventList);
            eventAdapter.notifyDataSetChanged();
        }


    }


    public static EventAdapter getEventeventAdapter() {
        return EventsScreenActivity.eventAdapter;
    }

    public static void setDelayNeeded(boolean delayNeeded) {
        EventsScreenActivity.delayNeeded = delayNeeded;
    }

}



