package com.example.ingredientsapp.data.remote;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("product_name")
    public String productName;

    @SerializedName("brands")
    public String brands;

    @SerializedName("image_url")
    public String imageURL;

    @SerializedName("code")
    public String code;

    @SerializedName("ingredients_text")
    public String ingredientsText;

    public String getProductName() {
        return productName;
    }

    public String getBrands() {
        return brands;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getCode() {
        return code;
    }

    public String getIngredientsText() {
        return ingredientsText;
    }
}