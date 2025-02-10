package com.example.realestate.models;

public class ModelAd {

    String id;
    String uid;
    String estate;
    String category;
    String address;
    String price;
    String size;
    String description;
    String status;
    long timestamp;
    double latitude;
    double longitude;
    boolean favorite;

    public ModelAd() {

    }

    public ModelAd(String id, String uid, String estate, String category, String address, String price, String size, String description, String status, long timestamp, double latitude, double longitude, boolean favorite) {
        this.id = id;
        this.uid = uid;
        this.estate = estate;
        this.category = category;
        this.address = address;
        this.price = this.price;
        this.size = this.size;
        this.description = description;
        this.status = status;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.favorite = favorite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEstate() {
        return estate;
    }

    public void setEstate(String estate) {
        this.estate = estate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrice() {
        return price;
    }

    public void setRent_price(String rent_price) {
        this.price = rent_price;
    }

    public String getSize() {
        return size;
    }

    public void setSize_per_square_meter(String size_per_square_meter) {
        this.size = size_per_square_meter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
