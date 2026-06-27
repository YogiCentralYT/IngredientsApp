package com.example.ingredientsapp.ui.recyclerview;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ingredientsapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.List;

public class ListRecyclerAdapter extends RecyclerView.Adapter<ListRecyclerAdapter.ViewHolder> {
    List<ListItem> listItems;
    LayoutInflater layoutInflater;
    Item item;
    ListClickListener listClickListener;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    public ListRecyclerAdapter(Context context, List<ListItem> listItems, Item item, ListClickListener listClickListener) {
        this.listItems = listItems;
        this.layoutInflater = LayoutInflater.from(context);
        this.item = item;
        this.listClickListener = listClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView listName;
        TextView itemCount;
        ImageButton moreButton;
        CheckBox checkBox;
        LinearLayout secondaryLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            listName = itemView.findViewById(R.id.listName);
            itemCount = itemView.findViewById(R.id.itemCount);
            moreButton = itemView.findViewById(R.id.moreListButton);
            checkBox = itemView.findViewById(R.id.checkBoxButton);
            secondaryLayout = itemView.findViewById(R.id.secondaryLayout);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listClickListener.OnListClick(listItems.get(pos));
            }
        }
    }

    public interface ListClickListener {
        void OnListClick(ListItem listItem);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (item != null) {
            holder.itemView.setOnClickListener(view -> {
                holder.checkBox.performClick();
            });
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.moreButton.setVisibility(View.GONE);
            holder.secondaryLayout.setVisibility(View.GONE);
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.moreButton.setVisibility(View.VISIBLE);
            holder.secondaryLayout.setVisibility(View.VISIBLE);
        }

        ListItem currentUserList = listItems.get(position);
        holder.listName.setText(currentUserList.getName());

        db.collection("users")
                .document(currentUser.getUid())
                .collection("lists")
                .document(String.valueOf(currentUserList.getId()))
                .collection("items")
                .get(Source.CACHE)
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (item == null) {
                            holder.itemCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                        }

                        if (item != null) {
                            boolean exists = queryDocumentSnapshots.getDocuments().stream()
                                    .anyMatch(doc -> doc.getId().equals(item.getCode()));
                            holder.checkBox.setChecked(exists);
                        }
                    }
                });

        holder.moreButton.setOnClickListener(view -> {
                int pos = holder.getAdapterPosition();
                showBottomSheet(holder.itemView.getContext(), pos, listItems.get(pos));
            });


        holder.checkBox.setOnClickListener(view -> {
            if (holder.checkBox.isChecked()) {
                addItemToList(holder.getAdapterPosition());
            } else {
                removeItemFromList(holder.getAdapterPosition());
            }
        });
    }

    public void showBottomSheet(Context context, int position, ListItem listItem) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = layoutInflater.inflate(R.layout.bottom_dialog_layout, null);

        TextView deleteText = bottomSheetView.findViewById(R.id.deleteText);
        deleteText.setText("Delete list");

        LinearLayout renameList = bottomSheetView.findViewById(R.id.renameList);
        renameList.setVisibility(View.VISIBLE);
        renameList.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            LayoutInflater inflater = LayoutInflater.from(context);
            View inputView = inflater.inflate(R.layout.custom_dialog_input, null);
            TextInputEditText input = inputView.findViewById(R.id.listNameInput);
            input.setText(listItem.getName());

            builder.setTitle("Rename List");
            builder.setMessage("Enter a new name for your list:");
            builder.setView(inputView);

            builder.setPositiveButton("Rename", null);
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(view1 -> {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(v ->{
                    String listName = input.getText().toString().trim();
                    if (listName.isEmpty()) {
                        input.setError("List name cannot be empty");
                    } else {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        FirebaseUser user = auth.getCurrentUser();
                        db.collection("users")
                                .document(user.getUid())
                                .collection("lists")
                                .document(String.valueOf(listItem.getId()))
                                .update("name", listName);
                        listItem.setName(listName);
                        notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
            });

            dialog.show();
            bottomSheetDialog.dismiss();
        });

        LinearLayout removeItem = bottomSheetView.findViewById(R.id.removeItem);
        removeItem.setOnClickListener(view2 -> {
            ListItem list = listItems.get(position);

            if (currentUser != null) {
                db.collection("users")
                        .document(currentUser.getUid())
                        .collection("lists")
                        .document(String.valueOf(list.getId()))
                        .delete();
                listItems.remove(position);
                notifyDataSetChanged();
            }

            Toast.makeText(context, "List deleted", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    public void addItemToList(int position) {
        HashMap<String, Object> itemMap = new HashMap<>();
        itemMap.put("name", item.getName());
        itemMap.put("brand", item.getBrand());
        itemMap.put("imgURL", item.getimgURL());
        itemMap.put("code", item.getCode());
        itemMap.put("timestamp", FieldValue.serverTimestamp());

        ListItem list = listItems.get(position);

        db.collection("users")
                .document(currentUser.getUid())
                .collection("lists")
                .document(String.valueOf(list.getId()))
                .collection("items")
                .document(item.getCode())
                .set(itemMap, SetOptions.merge());
    }

    public void removeItemFromList(int position) {
        ListItem list = listItems.get(position);

        db.collection("users")
                .document(currentUser.getUid())
                .collection("lists")
                .document(String.valueOf(list.getId()))
                .collection("items")
                .document(item.getCode())
                .delete();
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }
}
