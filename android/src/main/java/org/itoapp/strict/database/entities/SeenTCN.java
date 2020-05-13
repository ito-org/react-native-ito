package org.itoapp.strict.database.entities;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"tcn"},
        unique = true)})
public class SeenTCN {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "tcn")
    public String tcn;

    @ColumnInfo(name = "last_seen")
    public Date lastSeen;

    @ColumnInfo(name = "total_duration")
    public long duration;

    @ColumnInfo(name = "proximity")
    public long proximity;

    @ColumnInfo(name = "reportedSick")
    public boolean reportedSick = false;

    public SeenTCN() {
    }

    public SeenTCN(String tcn, Date lastSeen, long proximity, long duration) {
        this.tcn = tcn;
        this.lastSeen = lastSeen;
        this.duration = duration;
        this.proximity = proximity;
    }

    @Override
    public String toString() {
        return "SeenTCN{" +
                "id=" + id +
                ", tcn='" + tcn + '\'' +
                ", lastSeen=" + lastSeen +
                ", duration=" + duration +
                ", proximity=" + proximity +
                ", reportedSick=" + reportedSick +
                '}';
    }
}
