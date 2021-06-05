package com.example.radiuschat.utils;

public class User {
    private String phoneNumber;
    private double lat,lon;

    public User(String phoneNumber,double lat,double lon){
        this.lat = lat;
        this.lon = lon;
        this.phoneNumber = phoneNumber;
    }

    public User(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }


    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
