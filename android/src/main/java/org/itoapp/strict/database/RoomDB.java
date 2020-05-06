package org.itoapp.strict.database;


import org.itoapp.strict.database.dao.LastReportDao;
import org.itoapp.strict.database.dao.SeenTCNDao;
import org.itoapp.strict.database.entities.LastReport;
import org.itoapp.strict.database.entities.SeenTCN;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;


@Database(entities = {LastReport.class, SeenTCN.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class RoomDB extends RoomDatabase {

    public abstract LastReportDao lastReportDao();

    public abstract SeenTCNDao seenTCNDao();

    public static RoomDB db;
}
