package com.example.ingredientsapp.data.remote;

import com.google.gson.annotations.SerializedName;

public class Nutriments {

    @SerializedName("energy-kcal_100g")
    public Double energyKcal100g;

    @SerializedName("fat_100g")
    public Double fat100g;

    @SerializedName("saturated-fat_100g")
    public Double saturatedFat100g;

    @SerializedName("carbohydrates_100g")
    public Double carbohydrates100g;

    @SerializedName("sugars_100g")
    public Double sugars100g;

    @SerializedName("proteins_100g")
    public Double proteins100g;

    @SerializedName("salt_100g")
    public Double salt100g;

    @SerializedName("energy-kcal_serving")
    public Double energyKcalServing;

    @SerializedName("fat_serving")
    public Double fatServing;

    @SerializedName("saturated-fat_serving")
    public Double saturatedFatServing;

    @SerializedName("carbohydrates_serving")
    public Double carbohydratesServing;

    @SerializedName("sugars_serving")
    public Double sugarsServing;

    @SerializedName("proteins_serving")
    public Double proteinsServing;

    @SerializedName("salt_serving")
    public Double saltServing;
}