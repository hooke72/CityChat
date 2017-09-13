package com.hooke.citychat.model;

import com.google.firebase.database.IgnoreExtraProperties;
import com.hooke.citychat.model.CatalogItem;

/**
 * Object contains room item
 */
@IgnoreExtraProperties
public class Room {

    public String nameCity;
    public long idCity;
    public String nameRoom;
    public long idRoom;
    public long timeLastRead;
    public String iconPath;

    public Room (){
    }

    public Room (String nameCity, long idCity, String nameRoom, long idRoom, long timeLastRead, String iconPath) {
        this.nameCity = nameCity;
        this.idCity = idCity;
        this.nameRoom = nameRoom;
        this.idRoom = idRoom;
        this.timeLastRead = timeLastRead;
        this.iconPath = iconPath;
    }

    public Room (CatalogItem choicedCity, CatalogItem choicedRoom){
        this.nameCity = choicedCity.name;
        this.idCity = choicedCity.id;
        this.nameRoom = choicedRoom.name;
        this.idRoom = choicedRoom.id;
        this.timeLastRead = choicedRoom.payload;
        this.iconPath = choicedCity.iconPath;
    }
}
