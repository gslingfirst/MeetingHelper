package com.example.meetinghelper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ComingMeetingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coming_meeting);
        Intent intent = getIntent();
        MeetingItem meetingItem = (MeetingItem) intent.getSerializableExtra("comingMeeting");
        TextView textView = findViewById(R.id.coming_meeting_name);
        textView.setText(meetingItem.getRoom_name());
    }
}
