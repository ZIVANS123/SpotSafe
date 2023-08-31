package com.example.finelpro;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {////
    //Custom RecyclerViewAdapter for displaying Comments object.

    public static List<Comment> commentList;
    private String userName;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private DB db;

    //constructor
    public CommentAdapter(List<Comment> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
        this.firebaseFirestore = FirebaseFirestore.getInstance();
        this.db = DB.getInstance(context);
        userName = MainActivity.getSharedPreferences().getString("user", null);

    }

    //Gets a list and puts it in our comment list
    public void setComments(List<Comment> comments) {
        this.commentList = comments;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    //Updating our list and refreshing the Adapter
    public void updateCommentList(List<Comment> updatedCommentList) {
        commentList.clear();
        commentList.addAll(updatedCommentList);
        notifyDataSetChanged();
    }

    //Updates the single comment for our view in the Adapter
    public void updateComment(Comment comment) {
        int position = commentList.indexOf(comment);
        if (position != -1) {
            commentList.set(position, comment);
            notifyItemChanged(position);
        }
    }

    //The method in a RecyclerView adapter inflates and initializes the view for each item in the list.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    //The onBindViewHolder method in a RecyclerView adapter binds data to the views within the ViewHolder for each item in the list.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.comment = comment;
        holder.commentTextView.setText(comment.getComment() + "  ");
        holder.usernameTextView.setText("Addad by: " + comment.getEmail());
        if (!userName.equals(comment.getEmail())) {
            holder.editComment.setVisibility(View.GONE);
            holder.deleteComment.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    //Display a dialog with used for deleting a comment
    private void showDialog(Comment comment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.DELETE_COMMENT_DIALOG_TITLE));
        builder.setMessage(context.getString(R.string.DELETE_COMMENT_DIALOG_MESAGGE));

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Another way to get our layout for the Snackbar,
                // I did it this way because I got an error for the way we learned...
                View rootView = ((AppCompatActivity) context).getWindow().getDecorView().findViewById(android.R.id.content);
                int position = commentList.indexOf(comment);
                String commentId = comment.getCommentId();
                //Deleting the comment from FB as well as the local database
                firebaseFirestore.collection("comments").document(commentId).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                db.open();
                                db.deleteComment(comment);
                                db.close();
                                //Refreshing the Adapter display
                                commentList.remove(position);
                                notifyItemRemoved(position);
                                //Snackbar that shows the user the success of deleting the comment from FB
                                Snackbar.make(rootView, context.getString(R.string.COMMENT_DELETE_SNACKBAR), Snackbar.LENGTH_LONG).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Displaying a Snackbar to the user about the failure to delete the event from the FB
                                Snackbar.make(rootView, context.getString(R.string.FAIL_COMMENT_DELETE_SNACKBAR), Snackbar.LENGTH_LONG).show();
                            }
                        });

            }
        });

        builder.setNegativeButton("NO", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        //ViewHolder class to hold references to the views in the item layout
        TextView commentTextView;
        TextView usernameTextView;
        ImageView editComment;
        ImageView deleteComment;
        Comment comment;
        View llyout;

        //defines a constructor for a ViewHolder class used in a RecyclerView adapter, where it initializes various view elements based on the itemView layout,
        // and sets click listeners on the edit and delete buttons to perform specific actions when clicked (showing a dialog for deleting a comment or opening a CommentDialog for editing the comment).
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            editComment = itemView.findViewById(R.id.editComment);
            deleteComment = itemView.findViewById(R.id.deletecomment);
            llyout = itemView.findViewById(R.id.lllyout);
            deleteComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDialog(comment);
                }
            });

            editComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Open the CommentDialog with other data

                    CommentDialog dialogFragment = new CommentDialog(comment.getEventId(), comment.getCommentId(), CommentAdapter.this);
                    dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "comment_dialog");
                }
            });

        }
    }


}


