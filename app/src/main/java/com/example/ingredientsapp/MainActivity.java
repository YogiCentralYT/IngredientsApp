package com.example.ingredientsapp;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    ConstraintLayout searchLayout;
    ConstraintLayout homeLayout;

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

        TextView fakeSearchView = findViewById(R.id.fakeSearchView);
        SearchView searchView = findViewById(R.id.searchView);

        searchLayout = findViewById(R.id.searchLayout);
        homeLayout = findViewById(R.id.homeLayout);

        fakeSearchView.setOnClickListener(v -> {
            homeLayout.setVisibility(View.GONE);
            searchLayout.setVisibility(View.VISIBLE);
            searchView.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
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
    }
}

