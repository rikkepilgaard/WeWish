package com.example.wewish;

import java.util.List;

public class User {
    private String userName;
    private String birthDate;
    private String email;
    private List<Wish> wishList;
    private List<String> subscriberList;

    public User(String userName, List<Wish> wishList) {
        this.userName = userName;
        this.wishList = wishList;
    }

    public User() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<Wish> getWishList() {
        return wishList;
    }

    public void setWishList(List<Wish> wishList) {
        this.wishList = wishList;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getSubscriberList() {
        return subscriberList;
    }

    public void setSubscriberList(List<String> subscriberList) {
        this.subscriberList = subscriberList;
    }
}
