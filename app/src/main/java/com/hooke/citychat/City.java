package com.hooke.citychat;

import com.google.firebase.database.IgnoreExtraProperties;
/**
*   Object contains city item
*/
@IgnoreExtraProperties
class City {
    public String nameCity;
    public long idCity;

    public City (){

    }

    public City (String nameCity, long idCity) {

        this.nameCity = nameCity;
        this.idCity = idCity;

    }

}

