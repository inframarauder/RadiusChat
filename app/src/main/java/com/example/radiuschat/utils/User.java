package com.example.radiuschat.utils;

//utility class to define the structure of a user in firebase


public class User {
    private String phoneNumber,name;

    public User(){}

    public User(String phoneNumber,String name){
        this.phoneNumber = phoneNumber;
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
