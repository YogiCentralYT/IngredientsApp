package com.example.ingredientsapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    ConstraintLayout searchLayout;
    ConstraintLayout homeLayout;

    List<Item> itemList = new ArrayList<>();
    ItemAdapter itemAdapter;

    private String currentSearchTerm = "";
    private int currentPage = 1;
    private final int pageSize = 20;
    private boolean isLoading = false;

    private final Handler filterHandler = new Handler();
    private Runnable filterRunnable;

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

        searchLayout = findViewById(R.id.searchLayout);
        homeLayout = findViewById(R.id.homeLayout);
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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        itemAdapter = new ItemAdapter(itemList, item -> {
            Intent intent = new Intent(this, FoodDetailsActivity.class);
            intent.putExtra("item", item);
            this.startActivity(intent);
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(itemAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && linearLayoutManager != null && linearLayoutManager.findLastVisibleItemPosition() == itemList.size()-1) {
                    fetchItemList(currentSearchTerm);
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                currentPage = 1;
                itemList.clear();
                itemAdapter.notifyDataSetChanged();
                fetchItemList(s.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (filterRunnable != null) {
                    filterHandler.removeCallbacks(filterRunnable);
                }
                filterRunnable = () -> itemAdapter.getFilter().filter(s.trim());
                filterHandler.postDelayed(filterRunnable, 600);
                return true;
            }
        });

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

        fetchItemList("");
    }

    public void fetchItemList(String searchTerm) {
        if (isLoading) return;
        isLoading = true;

        currentSearchTerm = searchTerm;

        String url = "https://world.openfoodfacts.org/cgi/search.pl?search_terms=" + searchTerm + "&fields=product_name,brands,image_url&json=1&page=" + currentPage + "&page_size=" + pageSize;

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray products = response.getJSONArray("products");

                        if (products.length() == 0) {
                            Toast.makeText(MainActivity.this, "No more data", Toast.LENGTH_SHORT).show();
                            isLoading = false;
                            return;
                        }

                        for(int i=0; i < products.length(); i++) {
                            JSONObject product = products.getJSONObject(i);
                            String productName = product.optString("product_name", "No name");
                            String productBrand = product.optString("brands", "No brand");
                            String imgURL = product.optString("image_url");
                            itemList.add(new Item(productName, productBrand, imgURL));
                        }

                        itemAdapter.notifyDataSetChanged();
                        currentPage++;

                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "JSON error: " + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                    isLoading = false;
                },
                error -> {
                    isLoading = false;
                    Toast.makeText(MainActivity.this, "Volley error: " + error.toString(), Toast.LENGTH_LONG).show();
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                1000, // timeout in ms
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(request);
    }

}

