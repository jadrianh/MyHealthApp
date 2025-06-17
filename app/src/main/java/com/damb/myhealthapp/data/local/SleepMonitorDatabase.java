/*package com.damb.myhealthapp.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.damb.myhealthapp.data.local.dao.SleepDao;
import com.damb.myhealthapp.data.local.entity.SleepSession;

@Database(entities = {SleepSession.class}, version = 1, exportSchema = false)
public abstract class SleepMonitorDatabase extends RoomDatabase {

    public abstract SleepDao sleepDao();

    private static volatile SleepMonitorDatabase INSTANCE;

    public static SleepMonitorDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SleepMonitorDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SleepMonitorDatabase.class, "sleep_monitor_database").build();
                }
            }
        }
        return INSTANCE;
    }
}*/
