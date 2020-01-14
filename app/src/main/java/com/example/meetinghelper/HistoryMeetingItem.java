package com.example.meetinghelper;

public class HistoryMeetingItem{
    private String room_name;
    private long sttime;
    private long edtime;
    private int meetingid;
    private String topic;
    private String room_location;
    private String room_desc;
    private int type;
    private int hisType; // 0为结束，1为删除

    public HistoryMeetingItem(MeetingItem meetingItem, int htype) {
        this.room_name = meetingItem.getRoom_name();
        this.sttime = meetingItem.getSttime();
        this.edtime = meetingItem.getEdtime();
        this.meetingid = meetingItem.getMeetingid();
        this.topic = meetingItem.getTopic();
        this.room_location = meetingItem.getRoom_location();
        this.room_desc = meetingItem.getRoom_desc();
        this.type = meetingItem.getType();
        this.hisType = htype;
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

    public void setSttime(long sttime) {
        this.sttime = sttime;
    }

    public long getEdtime() {
        return edtime;
    }

    public void setEdtime(long edtime) {
        this.edtime = edtime;
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

    public boolean equalTo(MeetingItem mt){
        return this.sttime == mt.getSttime() && this.meetingid == mt.getMeetingid();
    }

    public int getHisType(){
        return hisType;
    }
    public void setHisType(int type){
        hisType = type;
    }

    public MeetingItem toMeetingItem(){
        MeetingItem meetingItem = new MeetingItem(this.room_name,this.sttime,this.edtime,this.meetingid,this.topic,this.room_location,this.room_desc);
        return meetingItem;
    }
}
