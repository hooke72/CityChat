package com.hooke.citychat.model;

import com.google.firebase.database.IgnoreExtraProperties;
/**
 *   Object contains city item
 */
@IgnoreExtraProperties
public class CatalogItem {
    public String name;
    public long id;
    public long payload;//timeLastReed or numberOfChat
    public String iconPath;

    public CatalogItem() {
    }

    public CatalogItem(String name, long id, long payload, String iconPath) {
        this.name = name;
        this.id = id;
        this.payload = payload;
        this.iconPath = iconPath;
    }

    public void store (CatalogItem item) {
        this.name = item.name;
        this.id = item.id;
        this.payload = item.payload;
        this.iconPath = item.iconPath;
    }

}