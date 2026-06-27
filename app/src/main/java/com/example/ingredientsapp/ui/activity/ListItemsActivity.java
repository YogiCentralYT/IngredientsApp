package com.example.ingredientsapp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ingredientsapp.R;
import com.example.ingredientsapp.ui.recyclerview.Item;
import com.example.ingredientsapp.ui.recyclerview.RecyclerViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListItemsActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_items);

        ImageButton addItemButton = this.findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListItemsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        UUID listId = (UUID) getIntent().getSerializableExtra("list_id");
        String listName = getIntent().getStringExtra("list_name");

        TextView listNameTextView = findViewById(R.id.listName);
        listNameTextView.setText(listName);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        loadItems(listId);
    }

    public void loadItems(UUID listId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .collection("lists")
                .document(listId.toString())
                .collection("items")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get(Source.CACHE)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Item> itemsList = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        String name = documentSnapshot.getString("name");
                        String brand = documentSnapshot.getString("brand");
                        String imgUrl = documentSnapshot.getString("imgURL");
                        String code = documentSnapshot.getString("code");
                        itemsList.add(new Item(name, brand, imgUrl, code));
                    }
                    adapter = getAdapter(itemsList, listId.toString());
                    recyclerView.setAdapter(adapter);
                });
    }

    public RecyclerViewAdapter getAdapter(List<Item> itemsList, String listId) {
        return new RecyclerViewAdapter(this, itemsList, R.layout.item_list_item_layout, RecyclerViewAdapter.Mode.LIST_ITEMS, listId, new RecyclerViewAdapter.ItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                Item clickedItem = itemsList.get(position);
                Intent intent = new Intent(ListItemsActivity.this, FoodInfoActivity.class);
                intent.putExtra("product_name", clickedItem.getName());
                intent.putExtra("brands", clickedItem.getBrand());
                intent.putExtra("image_url", clickedItem.getimgURL());
                intent.putExtra("code", clickedItem.getCode());
                startActivity(intent);
            }
        });
    }
}
