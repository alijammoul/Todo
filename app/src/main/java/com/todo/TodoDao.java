package com.todo;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface TodoDao {
    @Query("SELECT * FROM todos")
    List<TodoItem> getAllItems();

    @Query("SELECT COUNT(*) from todos")
    int countUsers();

    @Insert
    void insertAll(TodoItem... items);

    @Delete
    void delete(TodoItem item);

    @Update()
    void markAsCompleted(TodoItem item);
}
