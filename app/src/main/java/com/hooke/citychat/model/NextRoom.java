package com.hooke.citychat.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class NextRoom {
    public long nextIdRoom;

    public NextRoom() {
    }

    public NextRoom(long nextIdRoom) {
        this.nextIdRoom = nextIdRoom;
    }

    public void increase (){
        nextIdRoom++;
    }
}
