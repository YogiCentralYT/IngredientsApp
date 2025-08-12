package com.example.ingredientsapp.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface HistoryDAO {

    @Insert
    void insert(AppDatabase.HistoryEntity item);

    @Delete
    void delete(AppDatabase.HistoryEntity item);

    @Query("DELETE FROM history_items")
    void deleteAll();

    @Query("DELETE FROM history_items WHERE code = :code")
    void deleteByCode(String code);

    @Query("SELECT * FROM history_items ORDER BY timestamp DESC")
    LiveData<List<AppDatabase.HistoryEntity>> getAllProducts();

    @Query("SELECT * FROM history_items WHERE code = :code LIMIT 1")
    AppDatabase.HistoryEntity getProductByCode(String code);
}
