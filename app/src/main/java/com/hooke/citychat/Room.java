package com.hooke.citychat;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Object contains room item
 */
@IgnoreExtraProperties
public class Room {

    public String nameCity;
    public long idCity;
    public String nameRoom;
    public long idRoom;

    public Room (){

    }

    public Room (String nameCity, long idCity, String nameRoom, long idRoom) {

        this.nameCity = nameCity;
        this.idCity = idCity;
        this.nameRoom = nameRoom;
        this.idRoom = idRoom;
    }

}
