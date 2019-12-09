package com.example.meetinghelper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MeetingItemAdapter extends RecyclerView.Adapter<MeetingItemAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView roomName;
        private TextView time;
        private TextView topic;

        public ViewHolder(View view) {
            super(view);
            roomName = view.findViewById(R.id.meeting_item_room_name);
            time = view.findViewById(R.id.meeting_item_time);
            topic = view.findViewById(R.id.meeting_item_topic);
        }
    }
    private List<MeetingItem> mlist;
    public MeetingItemAdapter(List<MeetingItem> meetingItemList) {
        mlist = meetingItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meeting_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return  holder;
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MeetingItem meetingItem = mlist.get(position);
        holder.topic.setText("会议主题： " + meetingItem.getTopic());
        holder.roomName.setText("会议室：" + meetingItem.getRoom_name());
        Date st = new Date(meetingItem.getSttime()*1000), ed = new Date(meetingItem.getEdtime()*1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String time = simpleDateFormat.format(st);
        time += "-" + simpleDateFormat.format(ed).substring(11,16);
        holder.time.setText("起始时间：" + time);
    }
}
