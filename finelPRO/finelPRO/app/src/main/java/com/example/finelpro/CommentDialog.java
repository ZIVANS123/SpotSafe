package com.example.finelpro;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CommentDialog extends DialogFragment {
    //Declare variables---------------------------------------------------------------------------------------

    private EditText mEditText;
    private String eventId;
    private FirebaseFirestore firebaseFirestore;
    private DB db;
    private String commentId;
    private CommentAdapter commentAdapter;
    private String userName;


    //constructor
    public CommentDialog(String eventId) {
        this.eventId = eventId;
        firebaseFirestore = FirebaseFirestore.getInstance();
        db = DB.getInstance(getContext());
        userName = MainActivity.getSharedPreferences().getString("user", null);

    }

    //constructor
    public CommentDialog(String eventId, String commentId, CommentAdapter commentAdapter) {
        this.eventId = eventId;
        this.commentId = commentId;
        firebaseFirestore = FirebaseFirestore.getInstance();
        db = DB.getInstance(getContext());
        this.commentAdapter = commentAdapter;
        userName = MainActivity.getSharedPreferences().getString("user", null);
    }

    //The methodis responsible for inflating and returning the layout (R.layout.fragment_comment_dialog) associated with the CommentDialog Fragment when it is being created or displayed.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_comment_dialog, container, false);
    }

    //Here a dialog opens for the user, there are 2 cases for the dialog,
    // one for deleting a comment and the other for editing a comment
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        if (commentId == null || commentId.isEmpty()) {
            // Dialog for adding a new comment
            builder.setTitle(getString(R.string.ADD_COMMENT_DIALOG_TITLE));
            builder.setMessage(getString(R.string.ADD_COMMENT_DIALOG_MESAGGE));

            //The code inflates and creates a View object from the layout resource R.layout.fragment_comment_dialog using the LayoutInflater obtained from the current Fragment
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.fragment_comment_dialog, null);

            mEditText = view.findViewById(R.id.txt_your_comment);
            builder.setView(view);

            //When the user approves adding a comment, we will add the event to FB and the local database
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String commentText = mEditText.getText().toString().trim();
                    String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    Comment comment = new Comment(commentText, eventId, userEmail);

                    //Another way to get our layout for the Snackbar,
                    // I did it this way because I got an error for the way we learned...
                    View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);

                    firebaseFirestore.collection("comments")
                            .add(comment)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    String commentId = documentReference.getId();
                                    comment.setCommentId(commentId);
                                    db.open();
                                    db.addNewComment(comment);
                                    db.close();
                                    //Display a snackbar of the success of adding a comment
                                    Snackbar.make(rootView, R.string.ADD_COMMENT_SNACKBAR, Snackbar.LENGTH_LONG).show();


                                    // Perform any additional actions or UI updates as needed
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Display Snackbar of failure to add comment
                                    Snackbar.make(rootView, R.string.FAIL_ADD_COMMENT_SNACKBAR, Snackbar.LENGTH_LONG).show();
                                }
                            });
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            return builder.create();
        } else {
            // Dialog for updating an existing comment
            builder.setTitle(R.string.UPDATE_COMMENT_DIALOG_TITLE);
            builder.setMessage(R.string.UPDATE_COMMENT_DIALOG_MESAGGE);

            //The code inflates and creates a View object from the layout resource R.layout.fragment_comment_dialog using the LayoutInflater obtained from the current Fragment
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.fragment_comment_dialog, null);
            mEditText = view.findViewById(R.id.txt_your_comment);

            builder.setView(view);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String commentText = mEditText.getText().toString().trim();
                    Comment comment = new Comment(commentText, eventId, userName);
                    //Another way to get our layout for the Snackbar,
                    // I did it this way because I got an error for the way we learned...
                    View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
                    comment.setCommentId(commentId);

                    //Editing the comment on FB and the local database
                    firebaseFirestore.collection("comments").document(commentId)
                            .set(comment)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Comment updated successfully
                                    db.open();
                                    db.updateComment(comment);
                                    List<Comment> updatedComments = db.getCommentsByEventId(eventId);
                                    commentAdapter.updateCommentList(updatedComments);
                                    db.close();

                                    //Display Snackbar on successful comment editing
                                    Snackbar.make(rootView, R.string.UPDATE_COMMENT_SNACKBAR, Snackbar.LENGTH_LONG).show();

                                    //Displaying the updated event in our Adapter
                                    int position = CommentAdapter.commentList.indexOf(comment);
                                    if (position != -1) {
                                        CommentAdapter.commentList.remove(position);
                                        CommentAdapter.commentList.add(position, comment);
                                        commentAdapter.notifyItemChanged(position);
                                    }
                                    commentAdapter.updateComment(comment);


                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Display Snackbar on failure to edit comment
                                    Snackbar.make(rootView, R.string.FAIL_UPDATE_COMMENT_SNACKBAR + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            return builder.create();
        }

    }


}













