package com.example.curs;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String name;
    public String email;
    public String passwordHash;
    public boolean isSynced;
}
