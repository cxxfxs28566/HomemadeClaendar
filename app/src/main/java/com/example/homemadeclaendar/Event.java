package com.example.homemadeclaendar;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity
public class Event {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Event_Id")
    public Integer eid;

    @ColumnInfo(name = "Event_Date")
    public Date eventDate;

    @ColumnInfo(name = "Start_Time")
    public String startTime;

    @ColumnInfo(name = "End_Time")
    public String endTime;

    @ColumnInfo(name = "Event_Name")
    public String eventName;

    @ColumnInfo(name = "Event_Location")
    public String eventLocation;

    @ColumnInfo(name = "Event_Description")
    public String eventDescription;

    @ColumnInfo(name = "Event_Type")
    public String eventType;


    public Event(Date eventDate, String startTime, String endTime,String eventName,String eventLocation, String eventDescription,String eventType ){
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventName = eventName;
        this.eventLocation = eventLocation;
        this.eventDescription = eventDescription;
        this.eventType = eventType;
    }
    public Integer getEid(){
        return eid;
    }

    public Date getEventDate(){
        return eventDate;
    }
    public void setEventDate(Date eventDate){
        this.eventDate = eventDate;
    }

    public String getStartTime(){
        return startTime;
    }
    public void setStartTime(String startTime){
        this.startTime = startTime;
    }

    public String getEndTime(){
        return endTime;
    }
    public void setEndTime(String endTime){
        this.endTime = endTime;
    }

    public String getEventName(){
        return eventName;
    }
    public void setEventName(String eventName){
        this.eventName = eventName;
    }

    public String getEventLocation(){
        return eventLocation;
    }
    public void setEventLocation(String eventLocation){
        this.eventLocation = eventLocation;
    }

    public String getEventDescription(){
        return eventDescription;
    }
    public void setEventDescription(String eventDescription){
        this.eventDescription = eventDescription;
    }

    public String getEventType(){
        return eventType;
    }
    public void setEventType(String eventType){
        this.eventType = eventType;
    }
}
