package com.example.ingredientsapp.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ingredientsapp.R;
import com.example.ingredientsapp.data.local.AppDatabase;
import com.example.ingredientsapp.data.local.HistoryDAO;
import com.example.ingredientsapp.data.local.HistoryEntity;
import com.example.ingredientsapp.data.remote.Product;
import com.example.ingredientsapp.ui.FoodInfoActivity;
import com.example.ingredientsapp.ui.recyclerview.Item;
import com.example.ingredientsapp.ui.recyclerview.RecyclerViewAdapter;
import com.example.ingredientsapp.data.remote.ResponseProduct;
import com.example.ingredientsapp.network.RetrofitInstance;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    RecyclerViewAdapter adapter;
    List<Item> itemList = new ArrayList<>();

    int currentPage = 1;
    String currentQuery = null;
    boolean isLoading = false;
    boolean isLastPage = false;

    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable;

    Context context;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    AppDatabase ldb;
    HistoryDAO historyDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        context = requireContext();

        ldb = AppDatabase.getInstance(context);
        historyDAO = ldb.historyDAO();

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Home");

        ConstraintLayout searchLayout = view.findViewById(R.id.searchLayout);
        ConstraintLayout homeLayout = view.findViewById(R.id.homeLayout);

        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ConstraintLayout searchLayout = view.findViewById(R.id.searchLayout);
                ConstraintLayout homeLayout = view.findViewById(R.id.homeLayout);
                if (searchLayout.getVisibility() == View.VISIBLE) {
                    searchLayout.setVisibility(View.GONE);
                    homeLayout.setVisibility(View.VISIBLE);
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    toolbar.setVisibility(View.VISIBLE);
                } else {
                    setEnabled(false);
                }
            }
        });

        TextView fakeSearchView = view.findViewById(R.id.fakeSearchView);
        SearchView searchView = view.findViewById(R.id.searchView);
        fakeSearchView.setOnClickListener(v -> {
            homeLayout.setVisibility(View.GONE);
            searchLayout.setVisibility(View.VISIBLE);
            bottomNavigationView.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);

            searchView.requestFocus();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
        });

        if (user != null) {
            historyDAO.getAllProducts().observe(getViewLifecycleOwner(), historyList -> {
                for (HistoryEntity item : historyList) {
                    HashMap<String, Object> itemMap = new HashMap<>();
                    itemMap.put("name", item.name);
                    itemMap.put("brand", item.brand);
                    itemMap.put("imgURL", item.imgURL);
                    itemMap.put("code", item.code);
                    itemMap.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("users")
                            .document(user.getUid())
                            .collection("history")
                            .document(item.code)
                            .set(itemMap, SetOptions.merge());
                }
                new Thread(() -> {
                    historyDAO.deleteAll();
                }).start();
            });
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter(context, itemList, R.layout.item_search_layout, new RecyclerViewAdapter.ItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                Item clickedItem = adapter.getItem(position);

                if (user != null) {
                    HashMap<String, Object> itemMap = new HashMap<>();
                    itemMap.put("name", clickedItem.getName());
                    itemMap.put("brand", clickedItem.getBrand());
                    itemMap.put("imgURL",clickedItem.getimgURL());
                    itemMap.put("code", clickedItem.getCode());
                    itemMap.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("users")
                            .document(user.getUid())
                            .collection("history")
                            .document(clickedItem.getCode())
                            .set(itemMap, SetOptions.merge());
                } else {
                    new Thread(() -> {
                        historyDAO.insert(new HistoryEntity(
                                clickedItem.getCode(),
                                clickedItem.getName(),
                                clickedItem.getBrand(),
                                clickedItem.getimgURL(),
                                String.valueOf(System.currentTimeMillis())));
                    }).start();
                }

                Intent intent = new Intent(getActivity(), FoodInfoActivity.class);
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

        Button scanButton = view.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(v -> {
            startScan();
        });

        return view;
    }

    public void fetchItemList(String query, int page) {
        isLoading = true;

        RetrofitInstance.getApiInterface().getSearchProduct(query, page, 10).enqueue(new Callback<ResponseProduct>() {

            @Override
            public void onResponse(Call<ResponseProduct> call, Response<ResponseProduct> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null && response.body().getProducts() != null) {
                    List<Product> products = response.body().getProducts();

                    if (products.isEmpty()) {
                        isLastPage = true;
                        return;
                    }

                    for (Product product : products) {
                        String productName = product.getProductName() != null ? product.getProductName() : "No name";
                        String brands = product.getBrands() != null ? product.getBrands() : "No brand";
                        String imageURl = product.getImageURL();
                        String code = product.getCode();
                        itemList.add(new Item(productName, brands, imageURl, code));
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "Error: No Response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseProduct> call, Throwable t) {
                isLoading = false;
                if (currentQuery != null) {
                    Toast.makeText(context, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void startScan() {
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E
                )
                .enableAutoZoom()
                .build();

        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(context, options);

        scanner
                .startScan()
                .addOnSuccessListener(
                        barcode -> {
                            String rawValue = barcode.getRawValue();
                            fetchItem(rawValue, new FetchItemCallback() {
                                @Override
                                public void OnItemFetch(Item scannedItem) {
                                    if (user != null) {
                                        HashMap<String, Object> itemMap = new HashMap<>();
                                        itemMap.put("name", scannedItem.getName());
                                        itemMap.put("brand", scannedItem.getBrand());
                                        itemMap.put("imgURL",scannedItem.getimgURL());
                                        itemMap.put("code", scannedItem.getCode());
                                        itemMap.put("timestamp", FieldValue.serverTimestamp());

                                        db.collection("users")
                                                .document(user.getUid())
                                                .collection("history")
                                                .document(scannedItem.getCode())
                                                .set(itemMap, SetOptions.merge());
                                    } else {
                                        new Thread(() -> {
                                            historyDAO.insert(new HistoryEntity(
                                                    scannedItem.getCode(),
                                                    scannedItem.getName(),
                                                    scannedItem.getBrand(),
                                                    scannedItem.getimgURL(),
                                                    String.valueOf(System.currentTimeMillis())));
                                        }).start();
                                    }

                                    Intent intent = new Intent(getActivity(), FoodInfoActivity.class);
                                    intent.putExtra("product_name", scannedItem.getName());
                                    intent.putExtra("brands", scannedItem.getBrand());
                                    intent.putExtra("image_url", scannedItem.getimgURL());
                                    intent.putExtra("code", scannedItem.getCode());
                                    startActivity(intent);
                                }
                            });
                        })
                .addOnCanceledListener(
                        () -> {
                            Toast.makeText(context, "Scan cancelled", Toast.LENGTH_SHORT).show();
                        })
                .addOnFailureListener(
                        e -> {
                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    public void fetchItem(String code, FetchItemCallback fetchItemCallback) {
        RetrofitInstance.getApiInterface().getProductDetails(code).enqueue(new Callback<ResponseProduct>() {
            @Override
            public void onResponse(Call<ResponseProduct> call, Response<ResponseProduct> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body().getProduct();
                    String productName = product.getProductName() != null ? product.getProductName() : "No name";
                    String brands = product.getBrands() != null ? product.getBrands() : "No brand";
                    String imageURL = product.getImageURL();
                    Item item = new Item(productName, brands, imageURL, code);
                    fetchItemCallback.OnItemFetch(item);
                } else {
                    Toast.makeText(context, "Error: No response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseProduct> call, Throwable t) {
                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface FetchItemCallback {
        void OnItemFetch(Item item);
    }
}
