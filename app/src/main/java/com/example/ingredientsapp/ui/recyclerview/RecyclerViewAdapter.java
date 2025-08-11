package com.example.ingredientsapp.ui.recyclerview;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ingredientsapp.R;
import com.example.ingredientsapp.data.local.AppDatabase;
import com.example.ingredientsapp.data.local.HistoryDAO;
import com.example.ingredientsapp.data.local.HistoryEntity;
import com.example.ingredientsapp.ui.MainActivity;
import com.example.ingredientsapp.ui.fragments.HistoryFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements Filterable {
    private final List<Item> itemList;
    private final List<Item> fullList;
    private final LayoutInflater layoutInflater;
    private final ItemClickListener itemClickListener;
    private final int layout;

    public RecyclerViewAdapter(Context context, List<Item> itemList, int layout, ItemClickListener itemClickListener) {
        this.itemList = itemList;
        this.fullList = new ArrayList<>(itemList);
        this.layoutInflater = LayoutInflater.from(context);
        this.layout = layout;
        this.itemClickListener = itemClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView brand;
        ImageView image;
        ImageButton moreButton;
        ItemClickListener itemClickListener;

        ViewHolder(View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            this.itemClickListener = itemClickListener;
            name = itemView.findViewById(R.id.itemName);
            brand = itemView.findViewById(R.id.itemBrand);
            image = itemView.findViewById(R.id.itemImage);
            moreButton = itemView.findViewById(R.id.moreButton);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.OnItemClick(view, getAdapterPosition());
            }
        }
    }

    public Item getItem(int id) {
        return itemList.get(id);
    }

    public interface ItemClickListener {
        void OnItemClick(View view, int position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(layout, parent, false);
        return new ViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item currentItem = itemList.get(position);
        holder.name.setText(currentItem.getName());
        holder.brand.setText(currentItem.getBrand());
        Glide.with(holder.itemView.getContext()).load(currentItem.getimgURL()).into(holder.image);
        if (layout == R.layout.item_history_layout) {
            holder.moreButton.setOnClickListener(view -> {
                showBottomSheet(holder.itemView.getContext(), holder.getAdapterPosition());
            });
        }
    }

    public void showBottomSheet(Context context, int position) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = layoutInflater.inflate(R.layout.bottom_dialog_layout, null);

        LinearLayout removeItem = bottomSheetView.findViewById(R.id.removeItem);
        removeItem.setOnClickListener(view -> {
            Item item = itemList.get(position);

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                db.collection("users")
                        .document(user.getUid())
                        .collection("history")
                        .document(item.getCode())
                        .delete();
            }

            AppDatabase ldb = AppDatabase.getInstance(context);
            HistoryDAO historyDAO = ldb.historyDAO();
            new Thread(() -> {
                historyDAO.deleteByCode(item.getCode());
            }).start();

            itemList.clear();
            notifyDataSetChanged();

            Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<Item> filteredResults = new ArrayList<>();
                if (charSequence == null || charSequence.length() == 0) {
                    filteredResults.addAll(fullList);
                } else {
                    String filterPattern = charSequence.toString().toLowerCase().trim();
                    for (Item item : fullList) {
                        if (item.getName().toLowerCase().contains(filterPattern)) {
                            filteredResults.add(item);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredResults;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                itemList.clear();
                itemList.addAll((List<Item>) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }
}
