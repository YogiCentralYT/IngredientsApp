package com.example.ingredientsapp;

public class Item {
    private final String name;
    private final String brand;
    private final String imgURL;
    private final String code;
    public Item(String name, String brand, String imageURL, String code) {
        this.name= name;
        this.brand = brand;
        this.imgURL = imageURL;
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public String getBrand() {return brand;}
    public String getimgURL() {
        return imgURL;
    }
    public String getCode() {return code;}
}
