package com.example.finelpro;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MyComments extends Fragment {////


    private RecyclerView recyclerViewComments;
    private DB db;
    private String currentUser;


    public MyComments() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get the current user's username
        currentUser = MainActivity.getSharedPreferences().getString("user", null);
        db = DB.getInstance(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_comments, container, false);

        // Initialize views
        recyclerViewComments = rootView.findViewById(R.id.recyclerViewComments);
        // Set up RecyclerView and CommentAdapter
        CommentAdapter commentAdapter = new CommentAdapter(db.getCommentsByUser(currentUser), getContext());
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewComments.setAdapter(commentAdapter);

        // Load comments associated with the current user
        List<Comment> comments = db.getCommentsByUser(currentUser);

        if (comments != null && !comments.isEmpty()) {
            commentAdapter.setComments(comments);
            recyclerViewComments.setVisibility(View.VISIBLE);
        } else {
            recyclerViewComments.setVisibility(View.GONE);
        }

        return rootView;
    }
}