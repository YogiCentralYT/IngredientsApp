package com.example.ingredientsapp.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ingredientsapp.R;
import com.example.ingredientsapp.ui.activity.FoodInfoActivity;
import com.example.ingredientsapp.ui.recyclerview.Item;
import com.example.ingredientsapp.ui.recyclerview.RecyclerViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        Context context = requireContext();

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("History");

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            loadHistory(currentUser);
        } else {
            auth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser anonUser = auth.getCurrentUser();
                        loadHistory(anonUser);
                    }
                }
            });
        }

        return view;
    }

    public void loadHistory(FirebaseUser currentUser) {
        db.collection("users")
                .document(currentUser.getUid())
                .collection("history")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get(Source.CACHE)
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Item> historyList = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String name = documentSnapshot.getString("name");
                            String brand = documentSnapshot.getString("brand");
                            String imgURL = documentSnapshot.getString("imgURL");
                            String code = documentSnapshot.getString("code");
                            historyList.add(new Item(name, brand, imgURL, code));
                        }
                        adapter = getAdapter(requireContext(), historyList);
                        recyclerView.setAdapter(adapter);
                    }
                });
    }

    public RecyclerViewAdapter getAdapter(Context context, List<Item> historyList) {
        return new RecyclerViewAdapter(context, historyList, R.layout.item_list_item_layout, RecyclerViewAdapter.Mode.HISTORY, null, new RecyclerViewAdapter.ItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                Item clickedItem = historyList.get(position);
                Intent intent = new Intent(getActivity(), FoodInfoActivity.class);
                intent.putExtra("product_name", clickedItem.getName());
                intent.putExtra("brands", clickedItem.getBrand());
                intent.putExtra("image_url", clickedItem.getimgURL());
                intent.putExtra("code", clickedItem.getCode());
                startActivity(intent);
            }
        });
    }
}
