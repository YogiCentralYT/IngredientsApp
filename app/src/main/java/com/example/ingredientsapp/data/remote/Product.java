package com.example.ingredientsapp.data.remote;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Product {
    @SerializedName("product_name")
    public String productName;

    @SerializedName("brands")
    public String brands;

    @SerializedName("image_url")
    public String imageURL;

    @SerializedName("code")
    public String code;

    @SerializedName("ingredients_text_en")
    public String ingredientsText;

    @SerializedName("allergens_tags")
    public List<String> allergensTags;

    @SerializedName("serving_size")
    public String servingSizeText;

    @SerializedName("nutriments")
    public Nutriments nutriments;

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

    public List<String> getAllergensTags() {
        return allergensTags;
    }

    public String getServingSizeText() {
        return servingSizeText;
    }

    public Double getEnergyKcal100g() {
        return nutriments.energyKcal100g;
    }

    public Double getFat100g() {
        return nutriments.fat100g;
    }

    public Double getSaturatedFat100g() {
        return nutriments.saturatedFat100g;
    }

    public Double getCarbohydrates100g() {
        return nutriments.carbohydrates100g;
    }

    public Double getSugars100g() {
        return nutriments.sugars100g;
    }

    public Double getProteins100g() {
        return nutriments.proteins100g;
    }

    public Double getSalt100g() {
        return nutriments.salt100g;
    }

    public Double getEnergyKcalServing() {
        return nutriments.energyKcalServing;
    }

    public Double getFatServing() {
        return nutriments.fatServing;
    }

    public Double getSaturatedFatServing() {
        return nutriments.saturatedFatServing;
    }

    public Double getCarbohydratesServing() {
        return nutriments.carbohydratesServing;
    }

    public Double getSugarsServing() {
        return nutriments.sugarsServing;
    }

    public Double getProteinsServing() {
        return nutriments.proteinsServing;
    }

    public Double getSaltServing() {
        return nutriments.saltServing;
    }
}