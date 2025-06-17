/*package com.damb.myhealthapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.damb.myhealthapp.data.local.entity.SleepSession;

import java.util.List;

@Dao
public interface SleepDao {

    @Insert
    void insert(SleepSession session);

    @Update
    void update(SleepSession session);

    @Query("SELECT * from sleep_sessions_table WHERE id = :key")
    SleepSession get(long key);

    @Query("DELETE FROM sleep_sessions_table")
    void clear();

    @Query("SELECT * FROM sleep_sessions_table ORDER BY id DESC")
    LiveData<List<SleepSession>> getAllSessions();

    @Query("SELECT * FROM sleep_sessions_table ORDER BY id DESC LIMIT 1")
    SleepSession getLatestSession();
}*/