package org.itoapp.strict.database.dao;

import org.itoapp.strict.database.entities.SeenTCN;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface SeenTCNDao {

    @Query("SELECT * FROM SeenTCN WHERE reportedSick=1")
    public List<SeenTCN> findSickTCNs();

    @Query("SELECT * FROM SeenTCN WHERE tcn=:tcn")
    public SeenTCN findSeenTCNByHash(String tcn);

    @Insert
    public void insert(SeenTCN seenTCN);

    @Update
    public void update(SeenTCN seenTCN);

}
