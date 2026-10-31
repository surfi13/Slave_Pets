package com.example.curs;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PetDao {
    @Query("SELECT * FROM pets")
    LiveData<List<PetEntity>> getAllPets();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPet(PetEntity pet);

    @Query("SELECT * FROM pets WHERE isSynced = 0")
    List<PetEntity> getUnsyncedPets();

    @Update
    void updatePet(PetEntity pet);
}
