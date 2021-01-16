package com.example.easyexchangetest;

//@Comment: Modal class to create obj of info of user profile.
//Used to send or retrive user info to/from database

public class Users {
    String name, email;
    public Users(){
    }

    public Users(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
