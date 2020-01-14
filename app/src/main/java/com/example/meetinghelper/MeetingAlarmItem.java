package com.example.meetinghelper;

import java.io.Serializable;

public class MeetingAlarmItem implements Serializable {
    private long eventId;
    private String room_name;
    private long sttime;
    private long edtime;
    private String meeting_topic;
    private int enable_clock; // 0为关闭，1为打开
    private long start_time;
    private int last_time; //闹钟持续时间

    public MeetingAlarmItem(long id, String room_name, long sttime, long edtime,String meeting_topic,long start_time,int last_time) {
        this.room_name = room_name;
        this.eventId = id;
        this.sttime = sttime;
        this.edtime = edtime;
        this.meeting_topic = meeting_topic;
        this.enable_clock = 0;
        this.start_time = start_time;
        this.last_time = last_time;
    }
    public MeetingAlarmItem(MeetingAlarmItem meetingAlarmItem) {
        this.room_name = meetingAlarmItem.getRoom_name();
        this.eventId = meetingAlarmItem.getEventId();
        this.sttime = meetingAlarmItem.getSttime();
        this.edtime = meetingAlarmItem.getEdtime();
        this.meeting_topic = meetingAlarmItem.getMeeting_topic();
        this.enable_clock = meetingAlarmItem.getEnable_clock();
        this.start_time = meetingAlarmItem.getStart_time();
        this.last_time = meetingAlarmItem.getLast_time();
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public long getEdtime() {
        return edtime;
    }

    public void setEdtime(long edtime) {
        this.edtime = edtime;
    }

    public long getSttime() {
        return sttime;
    }

    public void setSttime(long sttime) {
        this.sttime = sttime;
    }

    public String getMeeting_topic() {
        return meeting_topic;
    }

    public void setMeeting_topic(String meeting_topic) {
        this.meeting_topic = meeting_topic;
    }

    public int getEnable_clock() {
        return enable_clock;
    }

    public void setEnable_clock(int enable_clock) {
        this.enable_clock = enable_clock;
    }

    public long getStart_time() {
        return start_time;
    }

    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }

    public int getLast_time() {
        return last_time;
    }

    public void setLast_time(int last_time) {
        this.last_time = last_time;
    }

    public MeetingItem toMeetingItem(){
        MeetingItem meetingItem = new MeetingItem(this.room_name,this.sttime,this.edtime,-1,"","","");
        return meetingItem;
    }
}

