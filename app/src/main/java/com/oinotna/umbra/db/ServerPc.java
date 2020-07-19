package com.oinotna.umbra.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "pc_table")
public class ServerPc {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @NonNull
    @ColumnInfo(name = "ip")
    private String ip;

    @ColumnInfo(name = "password")
    private String password;

    public ServerPc(String name, String ip){
        this.name=name;
        this.ip=ip;
    }

    public String getFullName() {
        return name+"/"+ip;
    }

    @NotNull
    public String getIp() {
        return ip;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password){
        this.password=password;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj==null) return false;
        return name.equals(((ServerPc)obj).getName());
    }
}
