package com.example.finelpro;

public class Approval {////
    //Declare variables---------------------------------------------------------------------------------------

    private String aprrovalId;
    private String eventId;
    private String userName;
    private Boolean status;

        public Approval() {
    }
    //constructor
    public Approval(String eventId, String userName, Boolean status) {
        this.eventId = eventId;
        this.userName = userName;
        this.status = status;
    }

    //getters and setters
    public String getAprrovalId() {
        return aprrovalId;
    }

    public void setAprrovalId(String aprrovalId) {
        this.aprrovalId = aprrovalId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
