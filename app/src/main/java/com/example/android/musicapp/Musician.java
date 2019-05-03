package com.example.android.musicapp;

public class Musician {
    private String Age;
    private String Gender;
    private String Image;
    private String PhoneNumber;
    private String UserType;
    private String username;

    public Musician(){

    }

    public Musician(String age, String gender, String image, String phoneNumber, String userType, String username) {
        Age = age;
        Gender = gender;
        Image = image;
        PhoneNumber = phoneNumber;
        UserType = userType;
        this.username = username;
    }

    public String getAge() {
        return Age;
    }

    public void setAge(String age) {
        Age = age;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getUserType() {
        return UserType;
    }

    public void setUserType(String userType) {
        UserType = userType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
