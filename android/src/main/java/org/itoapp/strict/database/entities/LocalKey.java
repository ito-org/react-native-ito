package org.itoapp.strict.database.entities;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LocalKey {

    @PrimaryKey()
    @NonNull
    public String rak;

    @ColumnInfo(name = "currentTCKpos")
    public int currentTCKpos;

    @ColumnInfo(name = "last_generated")
    public Date lastGenerated;

    @Override
    public String toString() {
        return "LocalKey{" +
                "rak='" + rak + '\'' +
                ", currentTCKpos=" + currentTCKpos +
                ", lastGenerated=" + lastGenerated +
                '}';
    }
}
