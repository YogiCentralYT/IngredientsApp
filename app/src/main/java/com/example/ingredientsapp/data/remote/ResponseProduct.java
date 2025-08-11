package com.example.ingredientsapp.data.remote;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseProduct {
    @SerializedName("products")
    public List<Product> products;

    @SerializedName("product")
    public Product product;

    public List<Product> getProducts() {
        return products;
    }

    public Product getProduct() {
        return product;
    }
}
