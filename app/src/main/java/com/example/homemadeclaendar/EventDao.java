package com.example.homemadeclaendar;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.Date;
import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface EventDao {

    @Query("SELECT * FROM Event")
    List<Event> getAll();

    @Query("SELECT * FROM Event WHERE Event_Id =:eventId  LIMIT 1")
    Event findById(int eventId);

    @Query("SELECT * FROM Event WHERE Event_Date = :eventDate")
    List<Event> findByDate(Date eventDate);

    @Query("DELETE FROM Event WHERE Event_Id = :eid")
    int deleteByEid(int eid);

    @Insert
    void insertAll(Event... event);

    @Insert long insert(Event event);

    @Delete
    void delete(Event event);

    @Update(onConflict = REPLACE)
    public void updateEvents(Event... events);

    @Query("DELETE FROM event")
    void deleteAll();



}
