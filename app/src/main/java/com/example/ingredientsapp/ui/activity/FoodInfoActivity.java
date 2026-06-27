package com.example.ingredientsapp.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ingredientsapp.R;
import com.example.ingredientsapp.data.remote.Product;
import com.example.ingredientsapp.data.remote.ResponseProduct;
import com.example.ingredientsapp.network.RetrofitInstance;
import com.example.ingredientsapp.ui.recyclerview.Item;
import com.example.ingredientsapp.ui.recyclerview.ListRecyclerAdapter;
import com.example.ingredientsapp.ui.recyclerview.ListItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FoodInfoActivity extends AppCompatActivity {
    Item item;

    FirebaseAuth auth;
    FirebaseFirestore db;

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_info);

        String name = getIntent().getStringExtra("product_name");
        String brand = getIntent().getStringExtra("brands");
        String imageURL = getIntent().getStringExtra("image_url");
        String code = getIntent().getStringExtra("code");
        item = new Item(name, brand, imageURL, code);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        ImageButton addToListButton = findViewById(R.id.addToListButton);

        addToListButton.setOnClickListener(view -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(FoodInfoActivity.this);
            View bottomSheetView = getLayoutInflater().inflate(R.layout.add_to_list_bottom_dialog, null);

            recyclerView = bottomSheetView.findViewById(R.id.addToListRecycler);
            layoutManager = new LinearLayoutManager(FoodInfoActivity.this);
            recyclerView.setLayoutManager(layoutManager);
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                loadLists(currentUser);
            } else {
                auth.signInAnonymously().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser anonUser = auth.getCurrentUser();
                        loadLists(anonUser);
                    }
                });
            }

            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        });


        TextView productNameTextView = findViewById(R.id.productName);
        TextView brandsTextView = findViewById(R.id.brands);
        ImageView imageView = findViewById(R.id.imageView);
        TextView ingredientsTextView = findViewById(R.id.ingredientsText);
        TextView allergensTextView = findViewById(R.id.allergensText);
        TextView servingSizeTextView = findViewById(R.id.servingSizeText);
        TextView caloriesTextView = findViewById(R.id.caloriesText);
        TextView fatTextView = findViewById(R.id.fatText);
        TextView saturatedFatTextView = findViewById(R.id.saturatedFatText);
        TextView carbohydratesTextView = findViewById(R.id.carbohydratesText);
        TextView sugarsTextView = findViewById(R.id.sugarsText);
        TextView proteinsTextView = findViewById(R.id.proteinsText);
        TextView saltTextView = findViewById(R.id.saltText);
        TextView caloriesServingTextView = findViewById(R.id.caloriesServingText);
        TextView fatServingTextView = findViewById(R.id.fatServingText);
        TextView saturatedFatServingTextView = findViewById(R.id.saturatedFatServingText);
        TextView carbohydratesServingTextView = findViewById(R.id.carbohydratesServingText);
        TextView sugarsServingTextView = findViewById(R.id.sugarsServingText);
        TextView proteinsServingTextView = findViewById(R.id.proteinsServingText);
        TextView saltServingTextView = findViewById(R.id.saltServingText);

        productNameTextView.setText(name);
        brandsTextView.setText(brand);
        Glide.with(this).load(imageURL).into(imageView);

        RetrofitInstance.getApiInterface().getProductDetails(code).enqueue(new Callback<ResponseProduct>() {

            @Override
            public void onResponse(Call<ResponseProduct> call, Response<ResponseProduct> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body().getProduct();
                    String ingredientsText = product.getIngredientsText();
                    String allergenTags = product.getAllergensTags() != null ? String.join(", ", product.getAllergensTags()) : null;
                    String servingSizeText = product.getServingSizeText();
                    Double energyKcal100g = product.getEnergyKcal100g();
                    Double fat100g = product.getFat100g();
                    Double saturatedFat100g = product.getSaturatedFat100g();
                    Double carbohydrates100g = product.getCarbohydrates100g();
                    Double sugars100g = product.getSugars100g();
                    Double proteins100g = product.getProteins100g();
                    Double salt100g = product.getSalt100g();
                    Double energyKcalServing = product.getEnergyKcalServing();
                    Double fatServing = product.getFatServing();
                    Double saturatedFatServing = product.getSaturatedFatServing();
                    Double carbohydratesServing = product.getCarbohydratesServing();
                    Double sugarsServing = product.getSugarsServing();
                    Double proteinsServing = product.getProteinsServing();
                    Double saltServing = product.getSaltServing();

                    ingredientsTextView.setText(ingredientsText);
                    if (allergenTags != null) {
                        allergensTextView.setText(allergenTags.replace("en:", ""));
                        allergensTextView.setTextColor(Color.parseColor("#D32F2F"));
                    } else {
                        allergensTextView.setText("None");
                        allergensTextView.setTextColor(Color.parseColor("#000000"));
                    }
                    servingSizeTextView.setText(servingSizeText != null ? servingSizeText : "?");
                    caloriesTextView.setText(energyKcal100g != null ? energyKcal100g + " kcal" : "?");
                    fatTextView.setText(fat100g != null ? fat100g + " g" : "N/A");
                    saturatedFatTextView.setText(saturatedFat100g != null ? saturatedFat100g + " g" : "?");
                    carbohydratesTextView.setText(carbohydrates100g != null ? carbohydrates100g + " g" : "?");
                    sugarsTextView.setText(sugars100g != null ? sugars100g + " g" : "?");
                    proteinsTextView.setText(proteins100g != null ? proteins100g + " g" : "?");
                    saltTextView.setText(salt100g != null ? salt100g + " g" : "?");
                    caloriesServingTextView.setText(energyKcalServing != null ? energyKcalServing + " kcal" : "?");
                    fatServingTextView.setText(fatServing != null ? fatServing + " g" : "?");
                    saturatedFatServingTextView.setText(saturatedFatServing != null ? saturatedFatServing + " g" : "?");
                    carbohydratesServingTextView.setText(carbohydratesServing != null ? carbohydratesServing + " g" : "?");
                    sugarsServingTextView.setText(sugarsServing != null ? sugarsServing + " g" : "?");
                    proteinsServingTextView.setText(proteinsServing != null ? proteinsServing + " g" : "?");
                    saltServingTextView.setText(saltServing != null ? saltServing + " g" : "?");
                }
            }

            @Override
            public void onFailure(Call<ResponseProduct> call, Throwable t) {
                Toast.makeText(FoodInfoActivity.this, "Network Error " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadLists(FirebaseUser currentUser) {
        db.collection("users")
                .document(currentUser.getUid())
                .collection("lists")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<ListItem> userLists = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String id = documentSnapshot.getId();
                            String name = documentSnapshot.getString("name");
                            userLists.add(new ListItem(UUID.fromString(id), name));
                        }

                        Set<String> listsContainingItem = new HashSet<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String listId = documentSnapshot.getId();
                            documentSnapshot.getReference().collection("items").document(item.getCode());
                            listsContainingItem.add(listId);
                        }

                        ListRecyclerAdapter adapter = new ListRecyclerAdapter(FoodInfoActivity.this, userLists, item, null);
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
