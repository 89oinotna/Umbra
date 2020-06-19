package com.oinotna.umbra.ui.home;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.oinotna.umbra.R;

import java.util.ArrayList;

public class ServersAdapter extends RecyclerView.Adapter<ServersAdapter.ServerViewHolder> {
    private ArrayList<ServerPc> servers_list;


    public class ServerViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.pc_name);
        }
    }

    public ServersAdapter (ArrayList<ServerPc> servers_list){
        this.servers_list=servers_list;
    }

    @NonNull
    @Override
    public ServersAdapter.ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.pc_card_view, parent,false);
        return new ServerViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
        holder.name.setText(servers_list.get(position).getFullName());
    }

    @Override
    public int getItemCount() {
        return servers_list.size();
    }
}
