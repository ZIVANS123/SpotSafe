package com.example.finelpro;
//206009060 ZIVAN SHLOMO SOLOMON
//313594210 IDAN YAKOV
import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class MainActivity extends AppCompatActivity {/////
    //Our variables
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private FirebaseAuth firebaseAuth;
    private static SharedPreferences sharedPreferences;
    private FirebaseFirestore dbFirebaseFirestore;
    private DB db;
    private FirebaseStorage firebaseStorage;//*
    private StorageReference storageReference;//*

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initializing the variables and binding the controls
        setContentView(R.layout.activity_main);
        dbFirebaseFirestore = FirebaseFirestore.getInstance();
        db = DB.getInstance(this);//**********
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);//**************
        firebaseAuth = FirebaseAuth.getInstance();
        EventsScreenActivity.setDelayNeeded(true);//When moving to the events screen, we need a short delay so that the local database has time to update before the data is displayed


        //By clicking on login,
        // we extract the content that the user entered and verify whether such a user exists with the FB authentication mechanism
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (username.isEmpty() || password.isEmpty())
                    return;

                firebaseAuth.signInWithEmailAndPassword(username, password)//The use of the mechanism
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    updateUI(null);
                                }
                            }
                        });
            }
        });

        //When the user clicks register he will go to the registration page
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    //Updating the user view
    private void updateUI(FirebaseUser currentUser) {

        updateLocalDatabase();//Call the method to update the local database
        if (currentUser != null) {
            //Retrieving the user to be used by us later in the program
            String email = currentUser.getEmail();
            getSharedPreferences().edit().putString("user", email).apply();

            //Retrieving the login date for display to the user
            FirebaseUserMetadata metadata = currentUser.getMetadata();
            long ltts = metadata.getLastSignInTimestamp();
            String lastLogin = String.valueOf(new Date(ltts));
            getSharedPreferences().edit().putString("lastLogin", lastLogin).apply();
            //The user goes to the events screen
            Intent intent = new Intent(MainActivity.this, EventsScreenActivity.class);
            startActivity(intent);
        }
    }

    //A method that synchronizes the local database from the FB,
    // the goal is to allow data to be accessed without using the FB,
    // useful in cases where there are communication problems
    public void updateLocalDatabase() {
        db.open();
        db.clearData();
        db.close();

        //Updates the events table in the local database
        CollectionReference eventsCollection = dbFirebaseFirestore.collection("events");
        eventsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    // Iterate over the retrieved documents and store the data in your local database
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Convert the document snapshot to an Event object
                        Event event = document.toObject(Event.class);

                        // Check if the imageUrl field is not null
                        if (event.getImageUrl() != null && event.getImageUrl() != "") {
                            // Retrieve the image from Firebase Storage
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference imageRef = storageRef.child(event.getImageUrl());
                            imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // Create a Bitmap from the byte array
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    // Set the bitmap in the Event object
                                    event.setPhoto(bitmap);
                                    event.setId(document.getId());
                                    // Store the Event object in your local database
                                    db.open();
                                    db.addNewEvent(event); // Modify this method according to your local database implementation
                                    db.close();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                        } else {
                            // Store the Event object in your local database
                            db.open();
                            event.setId(document.getId());
                            db.addNewEvent(event);
                            db.close();
                        }
                    }

                    Toast.makeText(MainActivity.this, getString(R.string.LOCAL_UPDATE_EVENT), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.LOCAL_UPDATE_EVENT_FAIL), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Updates the approvals table in the local database
        CollectionReference approvalsCollection = dbFirebaseFirestore.collection("approvals");
        approvalsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    // Iterate over the retrieved documents and store the data in your local database
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Convert the document snapshot to a Comment object
                        Approval approval = document.toObject(Approval.class);
                        approval.setAprrovalId(document.getId());
                        // Store the Approval object in your local database
                        db.open();
                        db.addNewApproval(approval);
                        db.close();
                    }

                    Toast.makeText(MainActivity.this, getString(R.string.LOCAL_UPDATE_APPROVALS), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.LOCAL_UPDATE_APPROVALS_FAIL), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Updates the comments table in the local database
        CollectionReference commentsCollection = dbFirebaseFirestore.collection("comments");
        commentsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    // Iterate over the retrieved documents and store the data in your local database
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Convert the document snapshot to a Comment object
                        Comment comment = document.toObject(Comment.class);
                        comment.setCommentId(document.getId());
                        // Store the Comment object in your local database
                        db.open();
                        db.addNewComment(comment);
                        db.close();
                    }

                    Toast.makeText(MainActivity.this, getString(R.string.LOCAL_UPDATE_COMMENTS), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.LOCAL_UPDATE_COMMENTS_FAIL), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);

    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}
