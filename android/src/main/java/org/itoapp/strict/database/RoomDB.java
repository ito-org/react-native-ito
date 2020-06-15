package org.itoapp.strict.database;


import android.content.Context;

import org.itoapp.strict.database.dao.LastReportDao;
import org.itoapp.strict.database.dao.LocalKeyDao;
import org.itoapp.strict.database.dao.SeenTCNDao;
import org.itoapp.strict.database.entities.LastReport;
import org.itoapp.strict.database.entities.LocalKey;
import org.itoapp.strict.database.entities.SeenTCN;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;


@Database(entities = {LastReport.class, SeenTCN.class, LocalKey.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class RoomDB extends RoomDatabase {

    public abstract LastReportDao lastReportDao();

    public abstract SeenTCNDao seenTCNDao();

    public abstract LocalKeyDao localKeyDao();

    private static RoomDB db;

    public static RoomDB getInstance(Context context) {
        if (db == null)
            db = Room.databaseBuilder(context,
                    RoomDB.class, "ito.room.db").allowMainThreadQueries().build();
        return db;
    }
}
