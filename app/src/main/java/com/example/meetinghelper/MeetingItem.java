package com.example.meetinghelper;

import java.io.Serializable;

public class MeetingItem implements Serializable {

    private String room_name;
    private long sttime;
    private long edtime;
    private int meetingid;
    private String topic;
    private String room_location;
    private String room_desc;
    private int type;
    private int alarm_clock;

    public MeetingItem(String room_name, long sttime, long edtime, int id, String topic, String room_location, String room_desc) {
        this.room_name = room_name;
        this.sttime = sttime;
        this.edtime = edtime;
        this.meetingid = id;
        this.topic = topic;
        this.room_location = room_location;
        this.room_desc = room_desc;
        this.type = 0;
        this.alarm_clock = 0;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public long getSttime() {
        return sttime;
    }

    public void setEdtime(long edtime) {
        this.edtime = edtime;
    }

    public long getEdtime() {
        return edtime;
    }

    public void setSttime(long sttime) {
        this.sttime = sttime;
    }

    public int getMeetingid() {
        return meetingid;
    }

    public void setMeetingid(int meetingid) {
        this.meetingid = meetingid;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getRoom_location() {
        return room_location;
    }

    public void setRoom_location(String room_location) {
        this.room_location = room_location;
    }

    public String getRoom_desc() {
        return room_desc;
    }

    public void setRoom_desc(String room_desc) {
        this.room_desc = room_desc;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getAlarm_clock() {
        return alarm_clock;
    }

    public void setAlarm_clock(int alarm_clock) {
        this.alarm_clock = alarm_clock;
    }

    public boolean equalTo(MeetingItem mt){
        return this.sttime == mt.getSttime() && this.meetingid == mt.getMeetingid();
    }

    public boolean equalTo(MeetingAlarmItem mt){
        return (this.room_name.compareTo(mt.getRoom_name()) == 0) && (this.sttime == mt.getSttime());
    }
}
