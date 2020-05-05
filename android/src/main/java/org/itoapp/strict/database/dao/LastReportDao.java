package org.itoapp.strict.database.dao;

import org.itoapp.strict.database.entities.LastReport;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface LastReportDao {
    @Query("SELECT * FROM LastReport WHERE server_url=:serverUrl")
    public LastReport getLastReportHashForServer(String serverUrl);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void saveOrUpdate(LastReport lr);
}
