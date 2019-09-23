package com.example.homemadeclaendar;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {Event.class}, version = 4, exportSchema = false)
@TypeConverters({TimestampConverter.class})

public abstract class EventDatabase extends RoomDatabase {
    public abstract EventDao eventDao();
    private static volatile EventDatabase INSTANCE;
    static EventDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (EventDatabase.class) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(), EventDatabase.class, "event_database") .build();
                }
            }
        } return INSTANCE;
    }
}
