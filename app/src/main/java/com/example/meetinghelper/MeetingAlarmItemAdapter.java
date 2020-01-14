package com.example.meetinghelper;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarprovidermanager.CalendarProviderManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;



public class MeetingAlarmItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<MeetingAlarmItem> mlist;
    private OnItemClickListener onItemClickListener;
    private ProgressDialog progressDialog;
    private CalendarProviderManager.Builder mBuilder;

    public enum Item_Type{ //item样式
        ITEMVIEW_ITEM_TYPE_1,
        ITEMVIEW_ITEM_TYPE_2
    }

    private Handler handler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg)
        {  //用于与主程序交互
            //int position = msg.what;
            //notifyItemRemoved(position);
            //mlist.remove(position);
        }
    };
    class ItemChange implements Runnable{   //自定义线程定时获取网络数据
        private int position;

        ItemChange(int position){this.position = position;}
        @Override
        public void run() {
            //Log.d("abd", "run: " + type);
            Message message = new Message();
            message.what = position;
            handler.sendMessage(message);
        }
    }

    public MeetingAlarmItemAdapter(List<MeetingAlarmItem> list){
        mlist = list;
    }

    public interface OnItemClickListener{ //定义点击事件接口
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    @Override
    public int getItemCount() {
        return mlist.size();
    }

    public class ViewHolderA extends RecyclerView.ViewHolder{
        TextView roomName;
        TextView time;
        TextView topic;
        TextView alarm_start_time;
        TextView alarm_last_time;

        public ViewHolderA(View view){
            super(view);
            roomName = view.findViewById(R.id.alarm_room_name);
            time = view.findViewById(R.id.alarm_meeting_item_time);
            topic = view.findViewById(R.id.alarm_meeting_topic);
            alarm_start_time = view.findViewById(R.id.alarm_start_time);
            alarm_last_time = view.findViewById(R.id.alarm_last_time);
        }
    }

    public class ViewHolderB extends RecyclerView.ViewHolder{
        TextView roomName;
        TextView time;
        TextView topic;
        TextView alarm_start_time;
        TextView alarm_last_time;

        public ViewHolderB(View view){
            super(view);
            roomName = view.findViewById(R.id.alarm_room_name_unable);
            time = view.findViewById(R.id.alarm_meeting_item_time_unable);
            topic = view.findViewById(R.id.alarm_meeting_topic_unable);
            alarm_start_time = view.findViewById(R.id.alarm_start_time_unable);
            alarm_last_time = view.findViewById(R.id.alarm_last_time_unable);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        mBuilder = new CalendarProviderManager.Builder(parent.getContext());
        if(viewType == Item_Type.ITEMVIEW_ITEM_TYPE_1.ordinal())
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_item,parent,false);
            ViewHolderA holder = new ViewHolderA(view);
            return holder;
        }else if(viewType == Item_Type.ITEMVIEW_ITEM_TYPE_2.ordinal()){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_item_unable,parent,false);
            ViewHolderB holder = new ViewHolderB(view);
            return holder;
        }
        return null;
    }
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        //super.onBindViewHolder(holder, position, payloads);

        final MeetingAlarmItem meetingAlarmItem = mlist.get(position);
        if(onItemClickListener != null)
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = holder.getLayoutPosition();
                    onItemClickListener.onItemClick(holder.itemView, pos);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int pos = holder.getLayoutPosition();
                    onItemClickListener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
        if(holder instanceof ViewHolderA){
            ((ViewHolderA)holder).roomName.setText(meetingAlarmItem.getRoom_name());
            ((ViewHolderA)holder).topic.setText(meetingAlarmItem.getMeeting_topic());
            Date st = new Date(meetingAlarmItem.getSttime()*1000), ed = new Date(meetingAlarmItem.getEdtime()*1000);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date ast = new Date(meetingAlarmItem.getStart_time()*1000);
            String time = simpleDateFormat.format(st);
            time += "-" + simpleDateFormat.format(ed).substring(11,16);
            String alarm_time = simpleDateFormat.format(ast);
            ((ViewHolderA)holder).time.setText(time);
            ((ViewHolderA)holder).alarm_start_time.setText(alarm_time);
            ((ViewHolderA)holder).alarm_last_time.setText(meetingAlarmItem.getLast_time() + "分钟");
        }else{
            ((ViewHolderB)holder).roomName.setText(meetingAlarmItem.getRoom_name());
            ((ViewHolderB)holder).topic.setText(meetingAlarmItem.getMeeting_topic());
            Date st = new Date(meetingAlarmItem.getSttime()*1000), ed = new Date(meetingAlarmItem.getEdtime()*1000);
            Date ast = new Date(meetingAlarmItem.getStart_time()*1000);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String time = simpleDateFormat.format(st);
            time += "-" + simpleDateFormat.format(ed).substring(11,16);
            String alarm_time = simpleDateFormat.format(ast);
            ((ViewHolderB)holder).time.setText(time);
            ((ViewHolderB)holder).alarm_start_time.setText(alarm_time);
            ((ViewHolderB)holder).alarm_last_time.setText(meetingAlarmItem.getLast_time() + "分钟");
        }
    }

    @Override
    public int getItemViewType(int position) {
        //return super.getItemViewType(position);
        if(mlist.get(position).getEnable_clock() == 1)
        {
            return Item_Type.ITEMVIEW_ITEM_TYPE_1.ordinal();
        }else{
            return Item_Type.ITEMVIEW_ITEM_TYPE_2.ordinal();
        }
    }

}
