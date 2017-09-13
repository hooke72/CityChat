package com.hooke.citychat.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Object contains chat item
 */

@IgnoreExtraProperties
public class Chat  {

    public String author;
    public String text;
    public long timestamp;
    public String uid;
    public boolean crypted;

    public Chat (){
    }

    public Chat (String author, String text, long timestamp, String uid, boolean crypted){
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
        this.uid = uid;
        this.crypted = crypted;
    }
}

