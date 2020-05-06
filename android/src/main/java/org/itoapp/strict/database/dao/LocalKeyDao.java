package org.itoapp.strict.database.dao;

import org.itoapp.strict.database.entities.LocalKey;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface LocalKeyDao {

    @Query("SELECT * FROM LocalKey")
    public List<LocalKey> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void saveOrUpdate(LocalKey localKey);

    @Query("DELETE FROM LocalKey")
    public void deleteAll();
}
