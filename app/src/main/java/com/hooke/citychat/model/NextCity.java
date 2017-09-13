package com.hooke.citychat.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class NextCity {
    public long nextIdCity;

    public NextCity() {
    }

    public NextCity(long nextIdCity) {
        this.nextIdCity = nextIdCity;
    }

    public void increase (){
        nextIdCity++;
    }
}