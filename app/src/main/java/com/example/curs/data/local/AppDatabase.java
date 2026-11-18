package com.example.curs.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

@Database(entities = {PetEntity.class, UserEntity.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PetDao petDao();
    public abstract UserDao userDao();

    private static volatile AppDatabase INSTANCE;
    private static String currentUid;

    public static AppDatabase getDatabase(final Context context) {
        String uid = currentUid();
        if (INSTANCE == null || !uid.equals(currentUid)) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null || !uid.equals(currentUid)) {
                    if (INSTANCE != null) {
                        INSTANCE.close();
                    }
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "pet_database_" + uid + ".db")
                            .fallbackToDestructiveMigration()
                            .build();
                    currentUid = uid;
                }
            }
        }
        return INSTANCE;
    }

    /** Сбросить кэш БД. Вызывать при login и logout. */
    public static synchronized void reset() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
        currentUid = null;
    }

    private static String currentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : "guest";
    }
}