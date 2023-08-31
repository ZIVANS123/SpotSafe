package com.example.finelpro;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;


public class Event implements Serializable {////
    private static int sid = 1; //Static ID for generating unique IDs
    private String id; //Event ID
    private String type; //Type of event
    private Bitmap photo; //Event photo
    private String description; //Event description
    private String address; //Event address
    private String area;//Event Area
    private String riskLevel; //Event risk level
    private String userName;// user name associated with the event
    private String date; //event date

    private String imageUrl;

    public Event(String type, Bitmap photo, String description,
                 String address, String area, String riskLevel, String date) {
        this.type = type;
        this.photo = photo;
        this.description = description;
        this.address = address;
        this.area = area;
        this.riskLevel = riskLevel;
        this.date = date;
        imageUrl = null;

        sid++;
    }

    //constructor
    public Event(String type, String imageUrl, String description,
                 String address, String area, String riskLevel, String date) {
        this.type = type;
        this.imageUrl = imageUrl;
        this.description = description;
        this.address = address;
        this.area = area;
        this.riskLevel = riskLevel;
        this.date = date;

        sid++;
    }

    //constructor
    public Event(String type, String description, String address, String area, String riskLevel, String date) {
        this.type = type;
        this.description = description;
        this.address = address;
        this.area = area;
        this.riskLevel = riskLevel;
        this.date = date;
        imageUrl = null;

    }

    //constructor
    public Event() {

    }

    // Getters
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getArea() {
        return area;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getUserName() {
        return userName;
    }

    public String getDate() {
        return date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Setters

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }


    public void setPhoto(Bitmap bitmap) {
        this.photo = bitmap;
        ;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public byte[] getImgAsByteArray(Bitmap bitmap) { // Converts a given Bitmap image to a byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


}

