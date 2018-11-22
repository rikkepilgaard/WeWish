package com.example.wewish;

import java.util.List;

public class User {
    private String userName;
    private List<Wish> wishList;

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
}
