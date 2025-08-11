package com.example.ingredientsapp.ui.recyclerview;

import java.sql.Timestamp;

public class Item {
    private String name;
    private String brand;
    private String imgURL;
    private String code;

    public Item() {}

    public Item(String name, String brand, String imageURL, String code) {
        this.name = name;
        this.brand = brand;
        this.imgURL = imageURL;
        this.code = code;
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

    public String getCode() {
        return code;
    }
}
