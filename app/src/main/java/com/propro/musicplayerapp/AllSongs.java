package com.propro.musicplayerapp;

import java.util.ArrayList;

public class AllSongs extends ArrayList<SongInfo> {

    private static AllSongs sSoleInstance;

    // private constructor.
    private AllSongs(){
        //Prevent form the reflection api.
        if (sSoleInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    // Thread safe implementation
    public synchronized static AllSongs getInstance(){
        if (sSoleInstance == null){ //if there is no instance available... create new one
            sSoleInstance = new AllSongs();
        }

        return sSoleInstance;
    }
}
