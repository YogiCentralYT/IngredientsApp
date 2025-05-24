package com.example.ingredientsapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> implements Filterable {
    private List<Item> itemList;
    private final List<Item> fullItemList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    ItemAdapter(List<Item> itemList, OnItemClickListener listener) {
        this.itemList = itemList;
        this.fullItemList = new ArrayList<>(itemList);
        this.listener = listener;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView brand;
        ImageView image;

        public ItemViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            name = itemView.findViewById(R.id.textView2);
            brand = itemView.findViewById(R.id.textView3);
            image = itemView.findViewById(R.id.imageView5);
            CardView container = (CardView) itemView;
            container.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(itemList.get(position));
                }
            });
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<Item> filteredResults = new ArrayList<>();
                if (charSequence == null || charSequence.length() == 0) {
                    filteredResults.addAll(fullItemList);
                } else {
                    String filterPattern = charSequence.toString().toLowerCase().trim();
                    for (Item item : fullItemList) {
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

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item, parent, false);
        return new ItemViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item currentItem = itemList.get(position);
        holder.name.setText(currentItem.getName());
        holder.brand.setText(currentItem.getBrand());
        Glide.with(holder.itemView.getContext()).load(currentItem.getimgURL()).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
