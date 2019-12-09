package com.example.meetinghelper;

import java.io.Serializable;

public class MeetingItem implements Serializable {

    public MeetingItem(String room_name, long sttime, long edtime, int id, String topic, String room_location, String room_desc) {
        this.room_name = room_name;
        this.sttime = sttime;
        this.edtime = edtime;
        this.id = id;
        this.topic = topic;
        this.room_location = room_location;
        this.room_desc = room_desc;
    }

    public String getRoom_name() {
        return room_name;
    }

    public long getSttime() {
        return sttime;
    }

    public long getEdtime() {
        return edtime;
    }

    public long getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getRoom_location() {
        return room_location;
    }

    public String getRoom_desc() {
        return room_desc;
    }

    private String room_name;
    private long sttime;
    private long edtime;
    private int id;
    private String topic;
    private String room_location;
    private String room_desc;
}
