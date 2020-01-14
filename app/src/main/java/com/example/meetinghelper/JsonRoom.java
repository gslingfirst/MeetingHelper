package com.example.meetinghelper;

public class JsonRoom{
    public int room_id;
    public String name;
    public String location;
    public String desc;

    public JsonRoom(int room_id, String name, String location, String desc, int type){
        this.room_id = room_id;
        this.name = name;
        this.location = location;
        this.desc = desc;
    }


    public boolean equalTo(Room r){
        if(this.room_id == r.getRoom_id() && this.name.compareTo(r.getName()) == 0 )
        {
            return true;
        }
        return false;
    }
}
