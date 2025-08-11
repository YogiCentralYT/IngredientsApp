package com.example.ingredientsapp.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ingredientsapp.R;
import com.example.ingredientsapp.data.local.AppDatabase;
import com.example.ingredientsapp.data.local.HistoryDAO;
import com.example.ingredientsapp.data.local.HistoryEntity;
import com.example.ingredientsapp.ui.FoodInfoActivity;
import com.example.ingredientsapp.ui.recyclerview.Item;
import com.example.ingredientsapp.ui.recyclerview.RecyclerViewAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    RecyclerViewAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        Context context = requireContext();

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("History");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        AppDatabase ldb = AppDatabase.getInstance(context);
        HistoryDAO historyDAO = ldb.historyDAO();


        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .collection("history")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
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

                            adapter = getAdapter(context, historyList);
                            recyclerView.setAdapter(adapter);
                        }
                    });
        } else {
            List<Item> itemHistoryList = new ArrayList<>();

            historyDAO.getAllProducts().observe(getViewLifecycleOwner(), historyList -> {
                for (HistoryEntity item : historyList) {
                    itemHistoryList.add(new Item(item.name, item.brand, item.imgURL, item.code));
                }
                adapter = getAdapter(context, itemHistoryList);
                recyclerView.setAdapter(adapter);
            });
        }
        return view;
    }

    public RecyclerViewAdapter getAdapter(Context context, List<Item> historyList) {
        return new RecyclerViewAdapter(context, historyList, R.layout.item_history_layout, new RecyclerViewAdapter.ItemClickListener() {
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
