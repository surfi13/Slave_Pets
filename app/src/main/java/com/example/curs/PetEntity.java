package com.example.curs;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "pets")
public class PetEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String name;
    public String species;
    public String breed;
    public long birthDate;
    public boolean isSynced;
}
