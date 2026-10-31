package com.example.curs;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    UserEntity getUserById(String id);

    @Query("SELECT * FROM users WHERE isSynced = 0")
    java.util.List<UserEntity> getUnsyncedUsers();

    @Query("UPDATE users SET isSynced = 1 WHERE id = :id")
    void markAsSynced(String id);
}