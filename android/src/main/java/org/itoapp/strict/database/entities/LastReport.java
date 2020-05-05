package org.itoapp.strict.database.entities;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(indices = {@Index(value = {"server_url"},
        unique = true)})
public class LastReport {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name="server_url")
    public String serverUrl;

    @ColumnInfo(name="lastcheck")
    public Date lastcheck;

    @ColumnInfo(name="lastReportHash")
    public String lastReportHash;

    @Override
    public String toString() {
        return "LastReport{" +
                "id=" + id +
                ", serverUrl='" + serverUrl + '\'' +
                ", lastcheck=" + lastcheck +
                ", lastReportHash='" + lastReportHash + '\'' +
                '}';
    }
}
