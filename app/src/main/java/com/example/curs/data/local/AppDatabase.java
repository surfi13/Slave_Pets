package com.example.curs.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {PetEntity.class, UserEntity.class}, version = 2, exportSchema = false) // <-- УВЕЛИЧЬТЕ ВЕРСИЮ
public abstract class AppDatabase extends RoomDatabase {
    public abstract PetDao petDao();
    public abstract UserDao userDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "pet_database")
                            .fallbackToDestructiveMigration() // <-- ДОБАВЬТЕ ЭТУ СТРОКУ
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}