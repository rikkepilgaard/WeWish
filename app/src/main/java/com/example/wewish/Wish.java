package com.example.wewish;

public class Wish {
    private String wishName, priority, comments, urlName, price;

    public Wish(String wishName, String priority, String comments, String urlName, String price) {
        this.wishName = wishName;
        this.priority = priority;
        this.comments = comments;
        this.urlName = urlName;
        this.price = price;
    }

    public Wish() {
    }

    public String getWishName() {
        return wishName;
    }

    public void setWishName(String wishName) {
        this.wishName = wishName;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getUrlName() {
        return urlName;
    }

    public void setUrlName(String urlName) {
        this.urlName = urlName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
