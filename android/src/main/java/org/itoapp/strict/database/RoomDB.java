package org.itoapp.strict.database;


import org.itoapp.strict.database.dao.LastReportDao;
import org.itoapp.strict.database.entities.LastReport;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;


@Database(entities = {LastReport.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class RoomDB extends RoomDatabase {

    public abstract LastReportDao lastReportDao();

    public static RoomDB db;
}
