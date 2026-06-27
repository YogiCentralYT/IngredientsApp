package com.example.ingredientsapp.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ingredientsapp.R;
import com.example.ingredientsapp.ui.activity.ListItemsActivity;
import com.example.ingredientsapp.ui.recyclerview.ListRecyclerAdapter;
import com.example.ingredientsapp.ui.recyclerview.ListItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ListsFragment extends Fragment {
    List<ListItem> userLists = new ArrayList<>();
    ListRecyclerAdapter adapter;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lists, container, false);
        Context context = requireContext();

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Lists");

        ImageButton addButton = view.findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            AlertDialog dialog = listDialog(context);
            dialog.show();
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        if (currentUser != null) {
            loadLists(currentUser);
        } else {
            auth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser anonUser = auth.getCurrentUser();
                        loadLists(anonUser);
                    }
                }
            });
        }
        adapter = new ListRecyclerAdapter(context, userLists, null, new ListRecyclerAdapter.ListClickListener() {
            @Override
            public void OnListClick(ListItem listItem) {
                startIntent(listItem);
            }
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void startIntent(ListItem listItem) {
        Intent intent = new Intent(getActivity(), ListItemsActivity.class);
        intent.putExtra("list_id", listItem.getId());
        intent.putExtra("list_name", listItem.getName());
        startActivity(intent);
    }

    AlertDialog listDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View inputView = inflater.inflate(R.layout.custom_dialog_input, null);
        TextInputEditText input =  inputView.findViewById(R.id.listNameInput);

        builder.setTitle("New List");
        builder.setMessage("Enter a name for your new list:");
        builder.setView(inputView);

        builder.setPositiveButton("Create", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String listName = input.getText().toString().trim();
                if (listName.isEmpty()) {
                    input.setError("List name cannot be empty");
                } else {
                    UUID id = UUID.randomUUID();
                    userLists.add(new ListItem(id, listName));

                    HashMap<String, Object> listMap = new HashMap<>();
                    listMap.put("id", id);
                    listMap.put("name", listName);
                    listMap.put("itemCount", 0);
                    listMap.put("timestamp", FieldValue.serverTimestamp());
                    db.collection("users")
                            .document(auth.getCurrentUser().getUid())
                            .collection("lists")
                            .document(String.valueOf(id))
                            .set(listMap, SetOptions.merge());
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            });
        });

        return dialog;
    }

    public void loadLists(FirebaseUser currentUser) {
        db.collection("users")
                .document(currentUser.getUid())
                .collection("lists")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get(Source.CACHE)
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        userLists.clear();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String id = documentSnapshot.getId();
                            String name = documentSnapshot.getString("name");
                            userLists.add(new ListItem(UUID.fromString(id), name));
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
