package com.hooke.citychat;

import com.google.firebase.database.FirebaseDatabase;
/**
 * Just make firebase cashed
 */
public class DatabaseUtil {

    private static FirebaseDatabase mDatabase;

    public static void setPersistenceEnabled() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
    }
}
