package com.example.finelpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditEventActivity extends AppCompatActivity {////

    //Declare variables for views-------------------------------------------------------------------------------------------
    private Spinner eventTypeSpinner;
    private EditText eventDescriptionEditText;
    private EditText eventAddressEditText;
    private Spinner eventAreaSpinner;
    private Spinner eventRiskLevelSpinner;
    private Button saveChangesButton;
    private Button selectDateButton;
    private Button selectTimeButton;
    private Button uploadPhotoButton;

    //Declare other variables---------------------------------------------------------------------------------------
    private DB db;
    private List<String> eventTypes;
    private List<String> eventAreas;
    private List<String> eventRiskLevels;
    private Context context;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;

    private StorageReference storageReference;
    private String imageFileName;

    private static final int REQUEST_IMAGE_CAPTURE = 12;
    private Bitmap bitmap;
    private Event event;
    private LinearLayout editLayout;//We will use it for a snack bar


    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Initialize UI elements
        eventTypeSpinner = findViewById(R.id.eventTypeSpinner);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        eventAddressEditText = findViewById(R.id.eventAddressEditText);
        eventAreaSpinner = findViewById(R.id.eventAreaSpinner);
        eventRiskLevelSpinner = findViewById(R.id.eventRiskLevelSpinner);
        saveChangesButton = findViewById(R.id.saveChangesButton);
        selectDateButton = findViewById(R.id.selectDateButton);
        selectTimeButton = findViewById(R.id.selectTimeButton);
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        editLayout = findViewById(R.id.editLayout);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        event = new Event();
        // Get event types, areas, and risk levels from resources
        eventTypes = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.event_types)));
        eventAreas = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.regions)));
        eventRiskLevels = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.risk_levels)));

        // Set up adapters for spinners
        ArrayAdapter<String> eventTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventTypes);
        eventTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(eventTypeAdapter);

        ArrayAdapter<String> eventAreaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventAreas);
        eventAreaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventAreaSpinner.setAdapter(eventAreaAdapter);

        ArrayAdapter<String> eventRiskLevelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventRiskLevels);
        eventRiskLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventRiskLevelSpinner.setAdapter(eventRiskLevelAdapter);

        // Get event ID from intent
        Intent intent = getIntent();
        String eventId = intent.getStringExtra(getString(R.string.SELECTEDEVENT));

        // Initialize database and retrieve event by ID
        db = DB.getInstance(context);
        db.open();
        Event event = db.getEventById(eventId);
        db.close();

        if (event != null) {
            // Populate UI elements with event details
            eventDescriptionEditText.setText(event.getDescription());
            eventAddressEditText.setText(event.getAddress());

            int eventTypeIndex = eventTypes.indexOf(event.getType());
            if (eventTypeIndex != -1) {
                eventTypeSpinner.setSelection(eventTypeIndex);
            } else {
                eventTypeSpinner.setSelection(0);
            }

            int eventAreaIndex = eventAreas.indexOf(event.getArea());
            if (eventAreaIndex != -1) {
                eventAreaSpinner.setSelection(eventAreaIndex);
            } else {
                eventAreaSpinner.setSelection(0);
            }

            int eventRiskLevelIndex = eventRiskLevels.indexOf(event.getRiskLevel());
            if (eventRiskLevelIndex != -1) {
                eventRiskLevelSpinner.setSelection(eventRiskLevelIndex);
            } else {
                eventRiskLevelSpinner.setSelection(0);
            }

            selectDateButton.setText(event.getDate().split(" ")[0]);
            selectTimeButton.setText(event.getDate().split(" ")[1]);

            // Set click listeners for date, time, and photo selection buttons
            selectDateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog();
                }
            });

            selectTimeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimePickerDialog();
                }
            });

            uploadPhotoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            });

            // Save changes button click listener
            saveChangesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get user input values
                    String description = eventDescriptionEditText.getText().toString().trim();
                    String address = eventAddressEditText.getText().toString().trim();
                    int typeIndex = eventTypeSpinner.getSelectedItemPosition();
                    int areaIndex = eventAreaSpinner.getSelectedItemPosition();
                    int riskLevelIndex = eventRiskLevelSpinner.getSelectedItemPosition();

                    // Retrieve selected types, areas, and risk levels from lists
                    String type = eventTypes.get(typeIndex);
                    String area = eventAreas.get(areaIndex);
                    String riskLevel = eventRiskLevels.get(riskLevelIndex);
                    String date = selectDateButton.getText().toString();
                    String time = selectTimeButton.getText().toString();
                    String dateTime = date + " " + time;

                    // Validate input values
                    if (description.isEmpty()) {
                        eventDescriptionEditText.setError(getString(R.string.DESCRIPTION_REQUIRED));
                        return;
                    }

                    if (address.isEmpty()) {
                        eventAddressEditText.setError(getString(R.string.ADDRESS_REQUIRED));
                        return;
                    }

                    // Update event object with new values
                    event.setDescription(description);
                    event.setAddress(address);
                    event.setType(type);
                    event.setArea(area);
                    event.setRiskLevel(riskLevel);
                    event.setDate(dateTime);
                    event.setPhoto(null);
                    event.setImageUrl(imageFileName);


                    //Event update on FB and local database
                    firestore = FirebaseFirestore.getInstance();
                    firestore.collection("events").document(event.getId())
                            .set(event)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {


                                    db = DB.getInstance(context);
                                    db.open();
                                    event.setPhoto(bitmap);
                                    db.updateEvent(event);
                                    db.close();

                                    //Display a snackbar to the user on the success of update an event to FB
                                    Snackbar.make(editLayout, R.string.UPDATE_EVENT_SNACKBAR, Snackbar.LENGTH_INDEFINITE)
                                            .show();

                                    //We added a delay because the INTENT interfered with the SNACKBAR display.
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent resultIntent = new Intent();
                                            resultIntent.putExtra("event", event.getId());
                                            //You don't need a delay from this screen,
                                            // but only from the login and registration screen because it takes time to synchronize the data
                                            EventsScreenActivity.setDelayNeeded(false);
                                            setResult(RESULT_OK, resultIntent);
                                            finish();
                                        }
                                    }, 600); // 3000 milliseconds delay (adjust as needed)

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Showing Snackbar on FB event update failure
                                    Snackbar.make(editLayout, R.string.FAIL_UPDATE_EVENT_SNACKBAR, Snackbar.LENGTH_INDEFINITE)
                                            .show();
                                }
                            });

                }
            });
        }
    }

    // Show date picker dialog
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year, month, dayOfMonth);
                        Date selectedDate = calendar.getTime();
                        String date = dateFormat.format(selectedDate);
                        selectDateButton.setText(date);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    // Show time picker dialog
    private void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        Date selectedTime = calendar.getTime();
                        String time = timeFormat.format(selectedTime);
                        selectTimeButton.setText(time);
                    }
                }, hour, minute, true);
        timePickerDialog.show();
    }

    // Get the selected bitmap
    private Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            this.bitmap = imageBitmap;

            //Converts the Bitmap object to a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            imageFileName = "event_" + System.currentTimeMillis() + ".jpg"; // Generate a unique file name
            StorageReference imageRef = storageReference.child(imageFileName);
            event.setImageUrl(imageFileName);
            UploadTask uploadTask = imageRef.putBytes(imageData);

        }
    }
}
