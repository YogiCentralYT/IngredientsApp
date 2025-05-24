package com.example.ingredientsapp;

import java.io.Serializable;

public class Item implements Serializable {
    private String name;
    private String brand;
    private String imgURL;
    public Item(String name, String brand, String imgURL) {
        this.name= name;
        this.brand = brand;
        this.imgURL = imgURL;
    }
    public String getName() {
        return name;
    }
    public String getBrand() {
        return brand;
    }
    public String getimgURL() {
        return imgURL;
    }
}
