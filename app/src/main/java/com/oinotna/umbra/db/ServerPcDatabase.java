package com.oinotna.umbra.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ServerPc.class}, version = 1, exportSchema = false)
public abstract class ServerPcDatabase extends RoomDatabase {

    public abstract ServerPcDao serverPcDao();
    //The Java volatile keyword guarantees visibility of changes to variables across threads
    private static volatile ServerPcDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);    //da utilizzare per le insert

    static ServerPcDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ServerPcDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ServerPcDatabase.class, "pc_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
