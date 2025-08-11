package com.example.ingredientsapp.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ingredientsapp.R;
import com.example.ingredientsapp.data.remote.Product;
import com.example.ingredientsapp.data.remote.ResponseProduct;
import com.example.ingredientsapp.network.RetrofitInstance;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FoodInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_info);

        String name = getIntent().getStringExtra("product_name");
        String brand = getIntent().getStringExtra("brands");
        String imageURL = getIntent().getStringExtra("image_url");
        String code = getIntent().getStringExtra("code");

        TextView productNameTextView = findViewById(R.id.productName);
        TextView brandsTextView = findViewById(R.id.brands);
        ImageView imageView = findViewById(R.id.imageView);
        TextView ingredientsTextView = findViewById(R.id.ingredients);

        productNameTextView.setText(name);
        brandsTextView.setText(brand);
        Glide.with(this).load(imageURL).into(imageView);

        RetrofitInstance.getApiInterface().getProductDetails(code).enqueue(new Callback<ResponseProduct>() {

            @Override
            public void onResponse(Call<ResponseProduct> call, Response<ResponseProduct> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body().getProduct();
                    String ingredientsText = product.getIngredientsText();
                    ingredientsTextView.setText(ingredientsText);
                }
            }

            @Override
            public void onFailure(Call<ResponseProduct> call, Throwable t) {
                Toast.makeText(FoodInfoActivity.this, "Network Error " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
