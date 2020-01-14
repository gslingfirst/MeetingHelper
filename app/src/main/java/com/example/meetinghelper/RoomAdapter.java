package com.example.meetinghelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class RoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Room> mlist;
    private RoomAdapter.OnItemClickListener onItemClickListener;
    private ProgressDialog progressDialog;
    private SQLiteDatabase db;
    private MyDatabaseHelper dbHelper;

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

    public enum Item_Type{ //item样式
        ITEMVIEW_ITEM_TYPE_1,
        ITEMVIEW_ITEM_TYPE_2
    }

    public RoomAdapter(List<Room> list){
        mlist = list;
    }

    public interface OnItemClickListener{ //定义点击事件接口
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }
    public void setOnItemClickListener(RoomAdapter.OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    @Override
    public int getItemCount() {
        return mlist.size();
    }

    public class ViewHolderA extends RecyclerView.ViewHolder{
        TextView roomName;
        TextView location;

        public ViewHolderA(View view){
            super(view);
            roomName = view.findViewById(R.id.room_name);
            location = view.findViewById(R.id.room_location);
        }
    }

    public class ViewHolderB extends RecyclerView.ViewHolder{
        TextView roomName;
        TextView location;
        TextView desc;
        ImageView modifyInfo;
        ImageView deleteItem;

        public ViewHolderB(View view){
            super(view);
            roomName = view.findViewById(R.id.admin_detail_room_name);
            location = view.findViewById(R.id.detail_room_location);
            desc = view.findViewById(R.id.detail_room_desc);
            modifyInfo = view.findViewById(R.id.admin_modifyItem);
            deleteItem = view.findViewById(R.id.admin_deleteItem);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == RoomAdapter.Item_Type.ITEMVIEW_ITEM_TYPE_1.ordinal())
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_item,parent,false);
            RoomAdapter.ViewHolderA holder = new RoomAdapter.ViewHolderA(view);
            return holder;
        }else if(viewType == RoomAdapter.Item_Type.ITEMVIEW_ITEM_TYPE_2.ordinal()){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_room_item,parent,false);
            RoomAdapter.ViewHolderB holder = new RoomAdapter.ViewHolderB(view);
            return holder;
        }
        return null;
    }
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        //super.onBindViewHolder(holder, position, payloads);
        final Room roomItem = mlist.get(position);
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
        if(holder instanceof RoomAdapter.ViewHolderA)
        {
            ((RoomAdapter.ViewHolderA)holder).roomName.setText(roomItem.getName());
            ((RoomAdapter.ViewHolderA)holder).location.setText(roomItem.getLocation());
        }else if(holder instanceof RoomAdapter.ViewHolderB){
            ((RoomAdapter.ViewHolderB)holder).roomName.setText(roomItem.getName());
            ((RoomAdapter.ViewHolderB)holder).location.setText(roomItem.getLocation());
            final int id = roomItem.getRoom_id();
            ((RoomAdapter.ViewHolderB)holder).desc.setText(roomItem.getDesc());
            ((RoomAdapter.ViewHolderB)holder).deleteItem.setOnClickListener(new View.OnClickListener() {
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
                    //notifyDataSetChanged();
                }
            });
            ((RoomAdapter.ViewHolderB)holder).modifyInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(),CreateRoomActivity.class);
                    intent.putExtra("type",position);
                    intent.putExtra("roomInfo",roomItem);
                    //clearType();
                    view.getContext().startActivity(intent);
                }
            });
        }
    }

    private void deleteItem(final int position, final Context context, int room_id){
        //showProgressDialog(context);
        String url = context.getString(R.string.host) + "/room/" + room_id + "/delete";
        HttpUtil.okHttpGet(url, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("abd", "room onFailure: delete failure");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                Log.d("zjj", resp);
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String status = jsonObject.getString("status");
                    if(status.compareTo("ok") == 0)
                    {
                        //LitePal.deleteAll(MeetingItem.class,"roomid = ?", iTS(mlist.get(position).getRoom_id())); //更新数据库
                        //mlist.remove(position);
                        db.delete("room","roomid = ?", new String[]{mlist.get(position).getRoom_id() + ""});
                        Looper.prepare();
                        ItemChange itemChange = new ItemChange(position);
                        itemChange.run();
                        Looper.loop();
                        Log.d("abd", "onSuccess: delete ok");
                    }
                } catch (Exception e) {
                    Log.d("abd", "onSuccess: delete exception");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        //return super.getItemViewType(position);
        if(mlist.get(position).getType() == 0)
        {
            return RoomAdapter.Item_Type.ITEMVIEW_ITEM_TYPE_1.ordinal();
        }else{
            return RoomAdapter.Item_Type.ITEMVIEW_ITEM_TYPE_2.ordinal();
        }
    }
}
