package com.example.finelpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AddEventActivity extends AppCompatActivity {///
    //Declare variables for views-------------------------------------------------------------------------------------------
    private Spinner eventTypeSpinner;
    private EditText eventDescriptionEditText;
    private EditText eventAddressEditText;
    private Spinner eventAreaSpinner;
    private Spinner eventRiskLevelSpinner;
    private Button saveEventButton;
    private Button selectDateButton;
    private Button selectTimeButton;
    private TextView eventDescriptionAsterisk;
    private TextView eventAddressAsterisk;

    private TextView eventDescriptionTextview;
    private TextView eventAddressTextview;
    private TextView eventAreaTextview;
    private TextView eventRiskTextview;
    private TextView selectDateTextview;
    private TextView selectTimeTextview;
    private TextView eventTypeTextview;
    private Button openCameraBtn;

    //Declare other variables---------------------------------------------------------------------------------------
    private String userName;
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;

    private List<String> eventTypes;
    private List<String> eventAreas;
    private List<String> eventRiskLevels;
    private Context context = this;
    private DB db;
    private static final int REQUEST_IMAGE_CAPTURE = 11;//It will be used when we return from the photo screen to the insert screen

    private Bitmap bitmap;
    private FirebaseFirestore firestoreDB;
    private CollectionReference eventsCollection;

    private LinearLayout llayout;//A necessary parameter for our snackbar display

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private Event event;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        llayout = findViewById(R.id.llayout);

        // Initialize views by finding them using their resource IDs-------------------------------------------------------------------------
        eventTypeSpinner = findViewById(R.id.eventTypeSpinner);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        eventAddressEditText = findViewById(R.id.eventAddressEditText);
        eventAreaSpinner = findViewById(R.id.eventAreaSpinner);
        eventRiskLevelSpinner = findViewById(R.id.eventRiskLevelSpinner);
        saveEventButton = findViewById(R.id.saveEventButton);
        selectDateButton = findViewById(R.id.selectDateButton);
        selectTimeButton = findViewById(R.id.selectTimeButton);
        openCameraBtn = findViewById(R.id.cameraBtn);
        eventDescriptionTextview = findViewById(R.id.eventDescriptionTextview);
        eventAddressTextview = findViewById(R.id.eventAddressTextview);
        eventAreaTextview = findViewById(R.id.eventAreaTextview);
        eventRiskTextview = findViewById(R.id.eventRiskTextview);
        selectDateTextview = findViewById(R.id.selectDateTextview);
        selectTimeTextview = findViewById(R.id.selectTimeTextview);
        eventTypeTextview = findViewById(R.id.eventTypeTextview);
        eventDescriptionAsterisk = findViewById(R.id.eventDescriptionAsterisk);
        eventAddressAsterisk = findViewById(R.id.eventAddressAsterisk);
        firestoreDB = FirebaseFirestore.getInstance();
        eventsCollection = firestoreDB.collection("events");
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        db = DB.getInstance(context);
        userName = MainActivity.getSharedPreferences().getString("user", null);

        //Initialize event type, area, and risk level lists with data from string arrays in resources------------------------------------------
        eventTypes = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.event_types)));
        eventAreas = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.regions)));
        eventRiskLevels = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.risk_levels)));

        //Set up adapters for spinners with the respective lists of data-----------------------------------------------------------------------
        ArrayAdapter<String> eventTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventTypes);
        eventTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(eventTypeAdapter);

        ArrayAdapter<String> eventAreaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventAreas);
        eventAreaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventAreaSpinner.setAdapter(eventAreaAdapter);

        ArrayAdapter<String> eventRiskLevelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventRiskLevels);
        eventRiskLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventRiskLevelSpinner.setAdapter(eventRiskLevelAdapter);
        //Set up click listeners for buttons--------------------------------------------------------------------------------------------------
        openCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open the camera to capture an image-----------------------------------------------------------------------------------------
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show a dialog to select a date
                showDatePickerDialog();


            }
        });

        selectTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show a dialog to select a time
                showTimePickerDialog();
            }
        });

        saveEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get the selected event details and create an Event object
                String type = eventTypeSpinner.getSelectedItem().toString();
                String description = eventDescriptionEditText.getText().toString();
                String address = eventAddressEditText.getText().toString();
                String area = eventAreaSpinner.getSelectedItem().toString();
                String riskLevel = eventRiskLevelSpinner.getSelectedItem().toString();
                String date = formatDate(selectedYear, selectedMonth, selectedDay);
                String time = formatTime(selectedHour, selectedMinute);


                if (description.isEmpty()) {
                    //show an error message if description is empty
                    eventDescriptionEditText.setError(getText(R.string.DESCRIPTIONERROR));
                    return;
                }

                if (address.isEmpty()) {
                    //Show an error message if address is empty
                    eventAddressEditText.setError(getText(R.string.ADDRESSERROR));
                    return;
                }
                String dateTime = date + " " + time; // Combine date and time into a single string
                //Creating an event in 2 cases, if a picture was taken and saved and if not
                if (event != null) {
                    event.setDate(dateTime);
                    event.setDescription(description);
                    event.setAddress(address);
                    event.setArea(area);
                    event.setRiskLevel(riskLevel);
                    event.setType(type);
                    event.setUserName(userName);
                } else {

                    event = new Event(type, "", description, address, area, riskLevel, dateTime);
                    event.setUserName(userName);
                }


                //There is a separation here, an event that I insert into the local database with an image (if any),
                // and an event without an image field but a string that holds an image URL to allow access to STORAGE
                eventsCollection.add(event)
                        .addOnSuccessListener(documentReference -> {
                            String eventId = documentReference.getId(); // Get the generated key from Firebase
                            event.setId(eventId); // Set the ID of the event using the generated key
                            event.setPhoto(bitmap);//Inserting the image for the local database

                            db.open();
                            db.addNewEvent(event);
                            db.close();
                            //Display a snackbar to the user on the success of adding an event to FB
                            Snackbar.make(llayout, getString(R.string.ADD_EVENT_SNACKBAR), Snackbar.LENGTH_INDEFINITE)
                                    .show();

                            //We added a delay because the INTENT interfered with the SNACKBAR display.
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    //We only need a delay from the login and registration screen,
                                    // as the local database is not enough to update while displaying the events on the splash screen
                                    EventsScreenActivity.setDelayNeeded(false);
                                    setResult(RESULT_OK);
                                    Intent intent = new Intent(AddEventActivity.this, EventsScreenActivity.class);
                                    intent.putExtra("delay_needed", false);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 600); // 600 milliseconds delay
                        })

                        .addOnFailureListener(e -> {
                            //SNACKBAR display for failure to add event to FB
                            Snackbar.make(llayout, getString(R.string.FAIL_ADD_EVENT_SNACKBAR), Snackbar.LENGTH_INDEFINITE)
                                    .show();
                            setResult(RESULT_CANCELED);
                        });

            }
        });

    }

    //The photo taken on the photo screen returns to AddActivity,
    // later we will add it to the database along with the rest of the event details
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            this.bitmap = imageBitmap;

            // Create a new Event object
            event = new Event();

            //Converts the Bitmap object to a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            String imageFileName = "event_" + System.currentTimeMillis() + ".jpg"; // Generate a unique file name
            StorageReference imageRef = storageReference.child(imageFileName);//add to Storage
            event.setImageUrl(imageFileName);
            UploadTask uploadTask = imageRef.putBytes(imageData);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Handle menu item clicks
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_myuploaded: {
                // Create an instance of the FilterFragment
                FilterFragment filterFragment = new FilterFragment();
                setHideDialog();


                // Replace the activity's content with the fragment

                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, filterFragment)
                        .addToBackStack(null)
                        .commit();

                return true;
            }
            case R.id.menuitem_house: {
                // Navigate to the EventsScreenActivity
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
                        .replace(android.R.id.content, reportFragment)
                        .addToBackStack(null)
                        .commit();

                return true;
            }
            case R.id.menuitem_approvals: {
                setHideDialog();
                //Create an instance of the ApprovalsFragment
                ApprovalsFragment approvalsFragment = new ApprovalsFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, approvalsFragment)
                        .addToBackStack(null)
                        .commit();
                return true;
            }

            case R.id.menuitem_sort: {
                setHideDialog();
                //Create an instance of the SortFragment
                SortFragment sortFragment = new SortFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, sortFragment)
                        .addToBackStack(null)
                        .commit();
                return true;
            }
            case R.id.menuitem_filter: {
                //Navigate to the FilterActivity
                Intent intent = new Intent(AddEventActivity.this, FilterActivity.class);
                startActivity(intent);
                return true;

            }
            case R.id.menuitem_myComments: {
                //Navigate to Reconnect
                setHideDialog();
                MyComments myComments = new MyComments();
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, myComments)
                        .addToBackStack(null)
                        .commit();
                return true;
            }
            case R.id.menuitem_General_Indices: {
                //Navigate to General Indices
                setHideDialog();
                GeneralIndicesFragment generalIndicesFragment = new GeneralIndicesFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, generalIndicesFragment)
                        .addToBackStack(null)
                        .commit();
                return true;
            }

        }
        return super.onOptionsItemSelected(item);
    }


    public void setHideDialog() {
        // hide various UI elements -------------------------------------------------------
        eventTypeSpinner.setVisibility(View.INVISIBLE);
        eventDescriptionEditText.setVisibility(View.INVISIBLE);
        eventAddressEditText.setVisibility(View.INVISIBLE);
        eventAreaSpinner.setVisibility(View.INVISIBLE);
        eventRiskLevelSpinner.setVisibility(View.INVISIBLE);
        saveEventButton.setVisibility(View.INVISIBLE);
        selectDateButton.setVisibility(View.INVISIBLE);
        selectTimeButton.setVisibility(View.INVISIBLE);
        openCameraBtn.setVisibility(View.INVISIBLE);
        eventTypeTextview.setVisibility(View.INVISIBLE);
        eventDescriptionTextview.setVisibility(View.INVISIBLE);
        eventAddressTextview.setVisibility(View.INVISIBLE);
        eventAreaTextview.setVisibility(View.INVISIBLE);
        eventRiskTextview.setVisibility(View.INVISIBLE);
        selectDateTextview.setVisibility(View.INVISIBLE);
        selectTimeTextview.setVisibility(View.INVISIBLE);
        eventDescriptionAsterisk.setVisibility(View.INVISIBLE);
        eventAddressAsterisk.setVisibility(View.INVISIBLE);


    }


    private void showDatePickerDialog() {
        //Get the Current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        //Create a DatePickerDialog to select a date-----------------------------------------------------
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Set the selected date
                        selectedYear = year;
                        selectedMonth = month;
                        selectedDay = dayOfMonth;
                        //Display the selected date
                        String selectedDate = formatDate(year, month, dayOfMonth);
                        selectDateButton.setText(selectedDate);
                    }
                }, year, month, day);


        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        //Get the current time
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        //Create a TimePickerDialog to select a time
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Set the selected time
                        selectedHour = hourOfDay;
                        selectedMinute = minute;
                        // Display the selected time
                        String selectedTime = formatTime(hourOfDay, minute);
                        selectTimeButton.setText(selectedTime);
                    }
                }, hour, minute, false);
        timePickerDialog.show();
    }


    private String formatDate(int year, int month, int day) {

        // Format the date as "yyyy-MM-dd"
        return String.format("%02d/%02d/%04d", day, month + 1, year);
    }

    private String formatTime(int hour, int minute) {
        //Format the time as "HH:mm"
        return String.format("%02d:%02d", hour, minute);
    }

}