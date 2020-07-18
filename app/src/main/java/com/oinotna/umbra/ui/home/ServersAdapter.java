package com.oinotna.umbra.ui.home;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.oinotna.umbra.R;
import com.oinotna.umbra.db.ServerPc;

import java.util.ArrayList;

public class ServersAdapter extends RecyclerView.Adapter<ServersAdapter.ServerViewHolder> {
    private ArrayList<ServerPc> serversList;
    private onItemClickListner mListener;
    private onMenuItemClickListener mListenerMenu;

    public interface onItemClickListner {
        void onItemClick(int position);
    }

    public interface onMenuItemClickListener{
        boolean onMenuItemClick(int idemId, int position);
    }

    public void setOnItemClickListner(onItemClickListner mListener) {
        this.mListener=mListener;
    }

    public void setOnMenuItemClickListener(onMenuItemClickListener mListenerMenu){
        this.mListenerMenu=mListenerMenu;
    }


    public class ServerViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        private TextView name;
        private CardView cv;
        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.pc_name);
            cv = itemView.findViewById(R.id.cv_server);
            cv.setOnClickListener(this);
            cv.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick(getAdapterPosition());
        }

        @Override
        public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
            //super.onCreateContextMenu(menu, v, menuInfo);
            MenuItem setPassword = menu.add(Menu.NONE, 1, 1, "Set Password");
            setPassword.setOnMenuItemClickListener(this);
        }


        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return mListenerMenu.onMenuItemClick(item.getItemId(), getAdapterPosition());
        }
    }

    public ServersAdapter (ArrayList<ServerPc> serversList){
        this.serversList=serversList;
    }

    @NonNull
    @Override
    public ServersAdapter.ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.pc_card_view, parent,false);
        return new ServerViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {
        holder.name.setText(serversList.get(position).getFullName());
    }

    @Override
    public int getItemCount() {
        return serversList.size();
    }


}
