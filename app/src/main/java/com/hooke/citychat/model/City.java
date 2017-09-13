package com.hooke.citychat.model;

import com.google.firebase.database.IgnoreExtraProperties;
/**
 *   Object contains city item
 */
@IgnoreExtraProperties
public class City {
    public String nameCity;
    public long idCity;
    public long payload;
    public String iconPath;

    public City (){
    }

    public City (String nameCity, long idCity, long payload, String iconPath) {
        this.nameCity = nameCity;
        this.idCity = idCity;
        this.payload = payload;
        this.iconPath = iconPath;
    }

}