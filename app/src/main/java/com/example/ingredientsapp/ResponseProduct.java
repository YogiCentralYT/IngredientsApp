package com.example.ingredientsapp;

import com.google.gson.annotations.SerializedName;

import java.io.Serial;
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

    public static class Product {
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
}
