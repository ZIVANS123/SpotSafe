package com.example.finelpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class EventDetailsActivity extends AppCompatActivity {////
    private DB db;
    private RecyclerView recyclerView;
    private static CommentAdapter commentAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        //We only need a delay from the login and registration screen,
        // as the local database is not enough to update while displaying the events on the splash screen
        EventsScreenActivity.setDelayNeeded(false);
        // Initialize RecyclerView
        String eventId = getIntent().getStringExtra("eventDetails");
        if (eventId == null || eventId.equals("")) {
            // Start the EmptyEventActivity because the event is empty
            Intent intent = new Intent(this, EventsScreenActivity.class);
            startActivity(intent);
            finish();
        }// Fin
        recyclerView = findViewById(R.id.commentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = DB.getInstance(this);


        db.open();
        List<Comment> commentsList = db.getCommentsByEventId(eventId);

        Event event = db.getEventById(eventId);
        db.close();
        //Create and set the event adapter for the list view
        commentAdapter = new CommentAdapter(commentsList, this);
        recyclerView.setAdapter(commentAdapter);
        commentAdapter.notifyDataSetChanged();

        populateEventDetails(event);


    }


    private void populateEventDetails(Event event) {
        // Find UI elements from the layout
        db.open();
        ImageView imageView = findViewById(R.id.eventImageView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
        TextView riskLevelTextView = findViewById(R.id.riskLevelTextView);
        TextView typeTextView = findViewById(R.id.typeTextView);
        TextView approvalCountTextView = findViewById(R.id.approvalCountTextView);

        if (event == null) {

            // Start the EmptyEventActivity because the event is empty
            Intent intent = new Intent(this, EventsScreenActivity.class);
            startActivity(intent);
            finish();
        }// Finish the current activity so that the user can't navigate back to it

        // Set event details to the corresponding UI elements
        descriptionTextView.setText(getString(R.string.DESCRIPTION) + ": " + event.getDescription());
        riskLevelTextView.setText(getString(R.string.RISKLEVEL) + ": " + event.getRiskLevel());
        typeTextView.setText(getString(R.string.TYPE) + ": " + event.getType());
        approvalCountTextView.setText(getString(R.string.APPROVALCOUNT) + ": " + String.valueOf(db.getApprovalCountForEvent(event.getId())));


        if (event.getPhoto() != null)
            imageView.setImageBitmap(event.getPhoto());
        else {
            imageView.setImageResource(R.drawable.defultimage);
        }

//        }
    }

    public static CommentAdapter getCommentAdapter() {
        return EventDetailsActivity.commentAdapter;
    }


}
