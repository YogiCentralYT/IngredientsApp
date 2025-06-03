package com.example.ingredientsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements Filterable {
    private final List<Item> itemList;
    private final List<Item> fullList;
    private final LayoutInflater layoutInflater;
    private final ItemClickListener itemClickListener;

    RecyclerViewAdapter(Context context, List<Item> itemList, ItemClickListener itemClickListener) {
        this.itemList = itemList;
        this.fullList = new ArrayList<>(itemList);
        this.layoutInflater = LayoutInflater.from(context);
        this.itemClickListener = itemClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView brand;
        ImageView image;
        ItemClickListener itemClickListener;

        ViewHolder(View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            this.itemClickListener = itemClickListener;
            name = itemView.findViewById(R.id.itemName);
            brand = itemView.findViewById(R.id.itemBrand);
            image = itemView.findViewById(R.id.itemImage);
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
        View view = layoutInflater.inflate(R.layout.layout_item, parent, false);
        return new ViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item currentItem = itemList.get(position);
        holder.name.setText(currentItem.getName());
        holder.brand.setText(currentItem.getBrand());
        Glide.with(holder.itemView.getContext()).load(currentItem.getimgURL()).into(holder.image);
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
