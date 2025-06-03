package com.example.ingredientsapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    RecyclerViewAdapter adapter;
    List<Item> itemList = new ArrayList<>();

    int currentPage = 1;
    String currentQuery = "Nutella";
    boolean isLoading = false;
    boolean isLastPage = false;

    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ConstraintLayout searchLayout = findViewById(R.id.searchLayout);
        ConstraintLayout homeLayout = findViewById(R.id.homeLayout);
        this.getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchLayout.getVisibility() == View.VISIBLE) {
                    searchLayout.setVisibility(View.GONE);
                    homeLayout.setVisibility(View.VISIBLE);
                } else {
                    finish();
                }
            }
        });

        TextView fakeSearchView = findViewById(R.id.fakeSearchView);
        SearchView searchView = findViewById(R.id.searchView);
        fakeSearchView.setOnClickListener(v -> {
            homeLayout.setVisibility(View.GONE);
            searchLayout.setVisibility(View.VISIBLE);
            searchView.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter(this, itemList, new RecyclerViewAdapter.ItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                Item clickedItem = adapter.getItem(position);
                Intent intent = new Intent(MainActivity.this, FoodInfoActivity.class);
                intent.putExtra("product_name", clickedItem.getName());
                intent.putExtra("brands", clickedItem.getBrand());
                intent.putExtra("image_url", clickedItem.getimgURL());
                intent.putExtra("code", clickedItem.getCode());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        fetchItemList(currentQuery, currentPage);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLoading && !isLastPage) {
                    if ((layoutManager.findFirstVisibleItemPosition()
                            + layoutManager.getChildCount())
                            >= layoutManager.getItemCount()) {
                        currentPage++;
                        fetchItemList(currentQuery, currentPage);
                    }
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                currentPage = 1;
                isLastPage = false;
                itemList.clear();
                adapter.notifyDataSetChanged();
                fetchItemList(currentQuery, currentPage);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchView.setQueryHint("");

                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }

                runnable = () -> {
                    currentQuery = newText;
                    currentPage = 1;
                    isLastPage = false;
                    itemList.clear();
                    adapter.notifyDataSetChanged();
                    fetchItemList(currentQuery, currentPage);
                };

                handler.postDelayed(runnable, 500);
                return true;
            }
        });
    }

    public void fetchItemList(String query, int page) {
        isLoading = true;

        RetrofitInstance.getApiInterface().getSearchProduct(query, page, 10).enqueue(new Callback<ResponseProduct>() {

            @Override
            public void onResponse(Call<ResponseProduct> call, Response<ResponseProduct> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null && response.body().getProducts() != null) {
                    List<ResponseProduct.Product> products = response.body().getProducts();

                    if (products.isEmpty()) {
                        isLastPage = true;
                        return;
                    }

                    for (ResponseProduct.Product product : products) {
                        String name = product.getProductName() != null ? product.getProductName() : "No name";
                        String brand = product.getBrands() != null ? product.getBrands() : "No brand";
                        String imageURl = product.getImageURL();
                        String code = product.getCode();
                        itemList.add(new Item(name, brand, imageURl, code));
                    }
                    adapter.notifyDataSetChanged();
                }
                else {
                    Toast.makeText(MainActivity.this, "Error: No Response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseProduct> call, Throwable t) {
                isLoading = false;
                Toast.makeText(MainActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
