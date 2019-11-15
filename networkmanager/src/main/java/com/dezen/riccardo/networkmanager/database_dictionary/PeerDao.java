package com.dezen.riccardo.networkmanager.database_dictionary;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface PeerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void add(PeerEntity... peerEntities);
    @Delete
    public void remove(PeerEntity... peerEntities);

    @Query("SELECT * FROM peerentity")
    public PeerEntity[] getAll();

    @Query("SELECT 1 FROM peerentity WHERE address=:address")
    public boolean contains(String address);
}
