package com.example.meetinghelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarprovidermanager.CalendarProviderManager;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;



public class MeetingItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<MeetingItem> mlist;
    private OnItemClickListener onItemClickListener;
    private ProgressDialog progressDialog;
    private CalendarProviderManager.Builder mBuilder;
    private SQLiteDatabase db;
    private MyDatabaseHelper dbHelper;
    private Context mContext;

    public enum Item_Type{ //item样式
        ITEMVIEW_ITEM_TYPE_1,
        ITEMVIEW_ITEM_TYPE_2
    }

    private Handler handler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg)
        {  //用于与主程序交互
            int position = msg.what;
            notifyItemRemoved(position);
            mlist.remove(position);
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

    public MeetingItemAdapter(List<MeetingItem> list){
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
        TextView location;

        public ViewHolderA(View view){
            super(view);
            roomName = view.findViewById(R.id.meeting_item_room_name);
            time = view.findViewById(R.id.meeting_item_time);
            topic = view.findViewById(R.id.meeting_item_topic);
            location = view.findViewById(R.id.meeting_item_location);
        }
    }

    public class ViewHolderB extends RecyclerView.ViewHolder{
        TextView roomName;
        TextView time;
        TextView topic;
        TextView location;
        TextView desc;
        ImageView meetingAlarm;
        ImageView deleteItem;

        public ViewHolderB(View view){
            super(view);
            roomName = view.findViewById(R.id.detail_room_name);
            time = view.findViewById(R.id.detail_item_time);
            topic = view.findViewById(R.id.detail_item_topic);
            location = view.findViewById(R.id.detail_item_location);
            desc = view.findViewById(R.id.detail_content);
            meetingAlarm = view.findViewById(R.id.meeting_alarm);
            deleteItem = view.findViewById(R.id.deleteItem);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        mBuilder = new CalendarProviderManager.Builder(parent.getContext());
        if(viewType == Item_Type.ITEMVIEW_ITEM_TYPE_1.ordinal())
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meeting_item,parent,false);
            ViewHolderA holder = new ViewHolderA(view);
            return holder;
        }else if(viewType == Item_Type.ITEMVIEW_ITEM_TYPE_2.ordinal()){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_meeting_item,parent,false);
            ViewHolderB holder = new ViewHolderB(view);
            return holder;
        }
        return null;
    }
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        //super.onBindViewHolder(holder, position, payloads);

        final MeetingItem meetingItem = mlist.get(position);
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
        if(holder instanceof ViewHolderA)
        {
            ((ViewHolderA)holder).roomName.setText(meetingItem.getRoom_name());
            ((ViewHolderA)holder).topic.setText(meetingItem.getTopic());
            ((ViewHolderA)holder).location.setText(meetingItem.getRoom_location());
            Date st = new Date(meetingItem.getSttime()*1000), ed = new Date(meetingItem.getEdtime()*1000);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String time = simpleDateFormat.format(st);
            time += "-" + simpleDateFormat.format(ed).substring(11,16);
            ((ViewHolderA)holder).time.setText(time);
        }else if(holder instanceof ViewHolderB){
            ((ViewHolderB)holder).roomName.setText(meetingItem.getRoom_name());
            ((ViewHolderB)holder).topic.setText(meetingItem.getTopic());
            ((ViewHolderB)holder).location.setText(meetingItem.getRoom_location());
            Date st = new Date(meetingItem.getSttime()*1000), ed = new Date(meetingItem.getEdtime()*1000);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String time = simpleDateFormat.format(st);
            time += "-" + simpleDateFormat.format(ed).substring(11,16);
            final int id = meetingItem.getMeetingid();
            ((ViewHolderB)holder).time.setText(time);
            ((ViewHolderB)holder).desc.setText(meetingItem.getRoom_desc());
            ((ViewHolderB)holder).deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    dbHelper = new MyDatabaseHelper(view.getContext(),"MeetingDB.db",null,1);
                    db = dbHelper.getWritableDatabase();
                    AlertDialog alertDialog = new AlertDialog.Builder(view.getContext())
                            .setTitle("删除会议")
                            .setMessage("是否删除会议")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteItem(position,view.getContext(),id);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    return;
                                }
                            }).create();
                    alertDialog.show();
                }
            });
            ((ViewHolderB)holder).meetingAlarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    private void deleteItem(final int position, final Context context, int meeting_id){
        //showProgressDialog(context);
        this.mContext = context;
        String url = context.getString(R.string.host) + "/meeting/" + meeting_id + "/delete";
        HttpUtil.okHttpGet(url, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("abd", "onFailure: delete failure");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                Log.d("abd", "delete ok" + resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String status = jsonObject.getString("status");
                    Log.d("abd", status);
                    if(status.compareTo("ok") == 0)
                    {
                        Log.d("abd", ""+mlist.size());
                        db.delete("meeting_item","meetingid = ?",new String[]{mlist.get(position).getMeetingid() + ""});
                        ContentValues values = getHistoryContentValues(mlist.get(position).getRoom_name(),mlist.get(position).getSttime(),mlist.get(position).getEdtime(),mlist.get(position).getMeetingid(),mlist.get(position).getTopic(),mlist.get(position).getRoom_location(),mlist.get(position).getRoom_desc(),0,1);
                        db.insert("history_meeting",null,values);
                        Looper.prepare();
                        ItemChange itemChange = new ItemChange(position);
                        itemChange.run();
                        Looper.loop();
                        //deleteOK = 1;
                    }else {
                        Log.d("abd", "outpouttuot");
                    }
                } catch (Exception e) {
                    //closeProgressDialog();
                    Log.d("abd", "delete false");
                    e.printStackTrace();
                }
            }
        });
    }
    private ContentValues getHistoryContentValues(String room_name, long sstime, long edtime, int meetingid, String topic, String room_location, String room_desc, int type, int histype){
        ContentValues values = new ContentValues();
        values.put("room_name", room_name);
        values.put("sttime", sstime);
        values.put("edtime",edtime);
        values.put("meetingid",meetingid);
        values.put("topic",topic);
        values.put("room_location", room_location);
        values.put("room_desc", room_desc);
        values.put("type",type);
        values.put("hisType",histype);
        return  values;
    }
    @Override
    public int getItemViewType(int position) {
        //return super.getItemViewType(position);
        if(mlist.get(position).getType() == 0)
        {
            return Item_Type.ITEMVIEW_ITEM_TYPE_1.ordinal();
        }else{
            return Item_Type.ITEMVIEW_ITEM_TYPE_2.ordinal();
        }
    }


}
