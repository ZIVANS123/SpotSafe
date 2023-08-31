package com.example.finelpro;

public class Comment {////

    //Declare variables---------------------------------------------------------------------------------------
    private String commentId;
    private String comment;
    private String eventId;
    private String email;

    public Comment() {
    }

    //constructor
    public Comment(String comment, String eventId, String email) {

        this.comment = comment;
        this.eventId = eventId;
        this.email = email;
    }

    //setters and getters
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}


