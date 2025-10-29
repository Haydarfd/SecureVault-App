package com.example.securefilestorageappdemo.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.securefilestorageappdemo.models.User;

@Dao
public interface UserDao {

    @Insert
    void insertUser(User user);

    @Query("SELECT * FROM User WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);
}
