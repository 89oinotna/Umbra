package com.oinotna.umbra.ui.home;

public class ServerPc {
    private String name;
    private String ip;

    public ServerPc(String name, String ip){
        this.name=name;
        this.ip=ip;
    }

    public String getFullName() {
        return name+":"+ip;
    }
}
