package com.example.ingredientsapp.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history_items")
public class HistoryEntity {
    @PrimaryKey
    @NonNull
    public String code;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "brand")
    public String brand;

    @ColumnInfo(name = "imgURL")
    public String imgURL;

    @ColumnInfo(name = "timestamp")
    public String timestamp;

    public HistoryEntity(@NonNull String code, String name, String brand, String imgURL, String timestamp){
        this.code = code;
        this.name = name;
        this.brand = brand;
        this.imgURL = imgURL;
        this.timestamp = timestamp;
    }
}
