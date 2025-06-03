package com.example.ingredientsapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class FoodInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_info);

        String name = getIntent().getStringExtra("product_name");
        String brand = getIntent().getStringExtra("brands");
        String imageURL = getIntent().getStringExtra("image_url");
        String code = getIntent().getStringExtra("code");

        RetrofitInstance.getApiInterface().getProductDetails(code).enqueue(new Callback<ResponseProduct>() {

            @Override
            public void onResponse(Call<ResponseProduct> call, Response<ResponseProduct> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResponseProduct.Product product = response.body().getProduct();
                    String ingredientsText = product.getIngredientsText();
                }
            }

            @Override
            public void onFailure(Call<ResponseProduct> call, Throwable t) {
                Toast.makeText(FoodInfoActivity.this, "Network Error " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
