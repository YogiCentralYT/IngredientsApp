package com.example.ingredientsapp.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
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
import com.example.ingredientsapp.ui.recyclerview.RecyclerViewAdapter;
import com.google.android.material.textfield.TextInputEditText;

public class ListsFragment extends Fragment {

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

        return view;
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

                    dialog.dismiss();
                }
            });
        });

        return dialog;
    }
}
