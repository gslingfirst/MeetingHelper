package com.example.meetinghelper;

public class JsonMeetingItem {
    public int id;
    public String topic;
    public long sttime;
    public long edtime;
    public String room_name;
    public String room_location;
    public String room_desc;

    JsonMeetingItem(int id, String topic, long sttime, long edtime, String room_name, String room_location, String room_desc)
    {
        this.id = id;
        this.topic = topic;
        this.sttime = sttime;
        this.edtime = edtime;
        this.room_location = room_location;
        this.room_name = room_name;
        this.room_desc = room_desc;
    }

    public boolean equalToMeeting(MeetingItem meetingItem){
        if(id == meetingItem.getMeetingid() && topic.compareTo(meetingItem.getTopic())== 0)
        {
            return true;
        }
        return false;
    }
}
