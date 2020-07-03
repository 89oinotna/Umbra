package com.oinotna.umbra.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface ServerPcDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ServerPc pc);

    @Query("SELECT * FROM pc_table WHERE name=:name")
    LiveData<ServerPc> getPc(String name);
}
