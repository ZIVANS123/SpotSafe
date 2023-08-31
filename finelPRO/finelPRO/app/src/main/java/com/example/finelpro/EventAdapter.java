package com.example.finelpro;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class EventAdapter extends ArrayAdapter<Event> {////
    //Custom ArrayAdapter for displaying Event objects in a ListView.
    private LayoutInflater inflater;
    private String userName = MainActivity.getSharedPreferences().getString("user", null);
    private DB db;
    private Context context = EventAdapter.this.getContext();
    private FirebaseFirestore firebaseFirestore;

    public EventAdapter(Context context, List<Event> eventList) {
        super(context, 0, eventList);
        inflater = LayoutInflater.from(context);
        db = DB.getInstance(context);
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    //displays the event's view
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            //If the convertView is null, inflate the layout for the item
            convertView = inflater.inflate(R.layout.item_event, parent, false);
            viewHolder = new ViewHolder();
            //Get references to the views in the layout------------------------------------------
            viewHolder.typeTextView = convertView.findViewById(R.id.typeTextView);
            viewHolder.descriptionTextView = convertView.findViewById(R.id.descriptionTextView);
            viewHolder.addressTextView = convertView.findViewById(R.id.addressTextView);
            viewHolder.areaTextView = convertView.findViewById(R.id.areaTextView);
            viewHolder.riskLevelTextView = convertView.findViewById(R.id.riskLevelTextView);
            viewHolder.photoImageView = convertView.findViewById(R.id.photoImageView);
            viewHolder.usernameTextView = convertView.findViewById(R.id.usernameTextView);
            viewHolder.idTextView = convertView.findViewById(R.id.idTextView);
            viewHolder.editMarkImageView = convertView.findViewById(R.id.editMarkImageView);
            viewHolder.acceptrejectImageView = convertView.findViewById(R.id.acceptrejectImageView);
            viewHolder.dateTextView = convertView.findViewById(R.id.dateTextView);
            viewHolder.commentImageView = convertView.findViewById(R.id.commentImageView);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Event event = getItem(position);

        if (event != null) {
            //Bind the data from the Event object to the views in the layout
            viewHolder.typeTextView.setText("Type is: " + event.getType());
            viewHolder.descriptionTextView.setText("Description: " + event.getDescription());
            viewHolder.addressTextView.setText("Addres: " + event.getAddress());
            viewHolder.areaTextView.setText("Area: " + event.getArea());
            viewHolder.riskLevelTextView.setText("Risk Level: " + event.getRiskLevel());
            viewHolder.usernameTextView.setText("Added by: " + event.getUserName());
            viewHolder.dateTextView.setText("Date: " + event.getDate());
            // Retrieve the image from the event object
            Bitmap photo = db.getEventImageById(event.getId());

            if (photo != null) {
                viewHolder.photoImageView.setImageBitmap(photo);
            } else {
                viewHolder.photoImageView.setImageResource(R.drawable.defultimage);
            }


            // Set click listeners for edit and accept/reject actions
            if (event.getUserName().equals(userName)) {
                viewHolder.editMarkImageView.setImageResource(R.drawable.edit);
                viewHolder.acceptrejectImageView.setVisibility(View.GONE);
                viewHolder.commentImageView.setVisibility(View.GONE);
            } else
                viewHolder.commentImageView.setImageResource(R.drawable.comment);


            viewHolder.editMarkImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userName.equals(event.getUserName()))
                        // Show a dialog for editing or deleting the event
                        showDialog(event);

                }
            });

            viewHolder.acceptrejectImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!userName.equals(event.getUserName()))
                        // Show a dialog for accepting or rejecting the event
                        showDialog(userName, event.getId());

                }
            });

            viewHolder.commentImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CommentDialog dialogFragment = new CommentDialog(event.getId());
                    dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "comment_dialog");


                }
            });

            viewHolder.idTextView.setText(String.valueOf("event id: " + event.getId()));
        }

        return convertView;
    }

    // Method to show a dialog for accepting or rejecting an event
    private void showDialog(String userName, String eventId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.CONFIRMATIONDIALOG);
        builder.setMessage(R.string.ACCEPTREJECTQUESTION);
        //Another way to get our layout for the Snackbar,
        // I did it this way because I got an error for the way we learned...
        View rootView = ((AppCompatActivity) context).getWindow().getDecorView().findViewById(android.R.id.content);

        //The user clicks Accept
        builder.setPositiveButton(R.string.ACCEPT, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Approval approval = new Approval(eventId, userName, true);
                //Adding accept to FB and the local database, there are 2 cases. Or add a new record or update an existing one
                firebaseFirestore.collection("approvals").whereEqualTo("eventId", eventId).whereEqualTo("userName", userName)
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();
                                if (!querySnapshot.isEmpty()) {
                                    DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                    String documentId = documentSnapshot.getId();
                                    boolean existingStatus = documentSnapshot.getBoolean("status");

                                    if (!existingStatus) {
                                        // Update the existing document with status = true
                                        firebaseFirestore.collection("approvals")
                                                .document(documentId)
                                                .update("status", true)
                                                .addOnSuccessListener(aVoid -> {

                                                    approval.setAprrovalId(documentId);
                                                    db.open();
                                                    db.updateApprovalStatus(approval);
                                                    db.close();
                                                    //Displaying a Snackbar to the user on the success of the approval update on FB
                                                    Snackbar.make(rootView, R.string.UPDATE_REPORT_SNACKBAR, Snackbar.LENGTH_LONG).show();

                                                })
                                                .addOnFailureListener(e -> {
                                                    //Showing a Snackbar to the user about the failure to update the Approval in FB
                                                    Snackbar.make(rootView, R.string.FAIL_UPDATE_REPORT_SNACKBAR, Snackbar.LENGTH_LONG).show();
                                                    // Handle the error here
                                                });
                                    } else {

                                    }
                                } else {
                                    // Document doesn't exist, create a new record
                                    firebaseFirestore.collection("approvals")
                                            .add(approval)
                                            .addOnSuccessListener(documentReference -> {
                                                String documentId = documentReference.getId();
                                                approval.setAprrovalId(documentId);
                                                db.open();
                                                db.addNewApproval(approval);
                                                db.close();
                                                //Displaying a Snackbar to the user on the success of the approval update on FB
                                                Snackbar.make(rootView, R.string.UPDATE_REPORT_SNACKBAR, Snackbar.LENGTH_LONG).show();


                                            })
                                            .addOnFailureListener(e -> {
                                                //Showing a Snackbar to the user about the failure to update the Approval in FB
                                                Snackbar.make(rootView, R.string.FAIL_UPDATE_REPORT_SNACKBAR, Snackbar.LENGTH_LONG).show();

                                            });
                                }
                            } else {

                            }
                        });


            }
        });
        //The user clicks Reject,
        // Updates if an Approval already exists or adds a new record, happens both in FB and in the local database
        builder.setNegativeButton(R.string.REJECT, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Approval approval = new Approval(eventId, userName, false);
                firebaseFirestore.collection("approvals").whereEqualTo("eventId", eventId).whereEqualTo("userName", userName)
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();
                                if (!querySnapshot.isEmpty()) {
                                    DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                    String documentId = documentSnapshot.getId();
                                    boolean existingStatus = documentSnapshot.getBoolean("status");

                                    if (existingStatus) {

                                        // Update the existing document with status = true
                                        firebaseFirestore.collection("approvals")
                                                .document(documentId)
                                                .update("status", false)
                                                .addOnSuccessListener(aVoid -> {
                                                    approval.setAprrovalId(documentId);
                                                    db.open();
                                                    db.updateApprovalStatus(approval);
                                                    db.close();
                                                    //Displaying a Snackbar to the user on the success of the approval update on FB
                                                    Snackbar.make(rootView, R.string.UPDATE_REPORT_SNACKBAR, Snackbar.LENGTH_LONG).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    //Showing a Snackbar to the user about the failure to update the Approval in FB
                                                    Snackbar.make(rootView, R.string.FAIL_UPDATE_REPORT_SNACKBAR, Snackbar.LENGTH_LONG).show();

                                                });
                                    } else {

                                    }
                                } else {
                                    // Document doesn't exist, create a new record
                                    firebaseFirestore.collection("approvals")
                                            .add(approval)
                                            .addOnSuccessListener(documentReference -> {
                                                String documentId = documentReference.getId();
                                                approval.setAprrovalId(documentId);
                                                db.open();
                                                db.addNewApproval(approval);
                                                db.close();
                                                //Displaying a Snackbar to the user on the success of the approval update on FB
                                                Snackbar.make(rootView, R.string.UPDATE_REPORT_SNACKBAR, Snackbar.LENGTH_LONG).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                //Showing a Snackbar to the user about the failure to update the Approval in FB
                                                Snackbar.make(rootView, R.string.FAIL_UPDATE_REPORT_SNACKBAR, Snackbar.LENGTH_LONG).show();

                                            });
                                }
                            } else {
                                //Showing a Snackbar to the user about the failure to update the Approval in FB
                                Snackbar.make(rootView, R.string.FAIL_UPDATE_REPORT_SNACKBAR, Snackbar.LENGTH_LONG).show();

                            }
                        });
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Method to show a dialog for editing or deleting event
    private void showDialog(Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.EDITDELETDIALOG);
        builder.setMessage(R.string.EDITDELETTITLE);

        builder.setPositiveButton(R.string.EDIT, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Open the EditEventActivity to edit the event
                Intent intent = new Intent(context, EditEventActivity.class);

                String selectedEvent = context.getString(R.string.SELECTEDEVENT);

                intent.putExtra(selectedEvent, event.getId());

                context.startActivity(intent);
            }
        });
        //Click on delete event
        builder.setNegativeButton(R.string.DELETE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.DELETE_DIALOG_TITLE)
                        .setMessage(R.string.DELETE_DIALOG_MESSAGE)
                        //Deleting the record of the event from FB and the local database
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                View rootView = ((AppCompatActivity) context).getWindow().getDecorView().findViewById(android.R.id.content);


                                firebaseFirestore.collection("events").document(event.getId()).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //Snackbar to the user for successfully deleting the event from FB
                                                Snackbar.make(rootView, R.string.DELETE_EVENT_SNACKBAR, Snackbar.LENGTH_LONG).show();

                                                // Delete all approvals associated with the event
                                                firebaseFirestore.collection("approvals")
                                                        .whereEqualTo("eventId", event.getId()) // Filter by eventid
                                                        .get()
                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot querySnapshot) {
                                                                // Loop through the matching documents and delete them
                                                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                                                    document.getReference().delete();
                                                                }

                                                                // All approvals deleted successfully, now proceed with deleting comments
                                                                firebaseFirestore.collection("comments")
                                                                        .whereEqualTo("eventId", event.getId()) // Filter by eventidComment
                                                                        .get()
                                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(QuerySnapshot querySnapshot) {
                                                                                // Loop through the matching documents and delete them
                                                                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                                                                    document.getReference().delete();
                                                                                }

                                                                                // All comments deleted successfully, now proceed with the rest of the actions
                                                                                db.open();
                                                                                db.deleteEvent(event);
                                                                                db.deleteApprovalsByEvent(event);
                                                                                db.deleteCommentsByEventId(event.getId());
                                                                                db.close();
                                                                                EventsScreenActivity.getEventeventAdapter().remove(event);
                                                                                EventsScreenActivity.getEventeventAdapter().notifyDataSetChanged();
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                            }
                                                                        });
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                // Snackbar to the user whose event deletion failedH
                                                                Snackbar.make(rootView, R.string.FAIL_DELETE_EVENT_SNACKBAR, Snackbar.LENGTH_LONG).show();

                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Snackbar to the user whose event deletion failedH
                                                Snackbar.make(rootView, R.string.FAIL_DELETE_EVENT_SNACKBAR, Snackbar.LENGTH_LONG).show();
                                            }
                                        });
//
//            }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        builder.setNeutralButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, just dismiss the dialog
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static class ViewHolder {
        //ViewHolder class to hold references to the views in the item layout
        TextView typeTextView;
        TextView descriptionTextView;
        TextView addressTextView;
        TextView areaTextView;
        TextView riskLevelTextView;
        ImageView photoImageView;
        TextView usernameTextView;
        TextView idTextView;
        ImageView editMarkImageView;
        ImageView acceptrejectImageView;
        TextView dateTextView;
        ImageView commentImageView;

    }
}