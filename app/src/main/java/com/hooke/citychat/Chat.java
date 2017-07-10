package com.hooke.citychat;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Object contains chat item
 */

@IgnoreExtraProperties
public class Chat  {

    public String author;
    public String text;
    public long timestamp;

    public Chat (){

    }

    public Chat (String author, String text, long timestamp){
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;

    }
}
