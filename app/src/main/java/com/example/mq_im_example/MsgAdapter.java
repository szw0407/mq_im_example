package com.example.mq_im_example;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.MsgViewHolder> {
    private List<MsgBean> data;
    public MsgAdapter(List<MsgBean> data) { this.data = data; }
    @NonNull
    @Override
    public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_left, parent, false);
        return new MsgViewHolder(v);
    }    @Override
    public void onBindViewHolder(@NonNull MsgViewHolder holder, int position) {
        MsgBean msg = data.get(position);
        holder.tvUser.setText(msg.user + ":");
        holder.tvContent.setText(msg.msg);
        holder.tvContent.setBackgroundResource(R.drawable.bg_msg_left);
    }
    @Override
    public int getItemCount() { return data.size(); }
    public void addMsg(MsgBean msg) {
        data.add(msg);
        notifyItemInserted(data.size() - 1);
    }
    static class MsgViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvContent;
        MsgViewHolder(View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
