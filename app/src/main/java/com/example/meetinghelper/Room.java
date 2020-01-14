package com.example.meetinghelper;

import java.io.Serializable;

public class Room implements Serializable {
    private int roomid;
    private String name;
    private String location;
    private String desc;
    int type;

    public Room(int room_id, String name, String location, String desc, int type){
        this.roomid = room_id;
        this.name = name;
        this.location = location;
        this.desc = desc;
        this.type = type;
    }

    public int getRoomid() {
        return roomid;
    }

    public void setRoomid(int roomid) {
        this.roomid = roomid;
    }

    public int getRoom_id() {
        return roomid;
    }

    public void setRoom_id(int room_id) {
        this.roomid = room_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean equalTo(Room r){
        if(this.roomid == r.getRoom_id() && this.name == r.getName())
        {
            return true;
        }
        return false;
    }
}
