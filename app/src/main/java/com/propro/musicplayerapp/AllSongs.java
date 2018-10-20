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

        add(new SongInfo("song1", "artist1"));
        add(new SongInfo("song2", "artist2"));
        add(new SongInfo("song3", "artist3"));
        add(new SongInfo("song4", "artist4"));
        add(new SongInfo("song5", "artist5"));
        add(new SongInfo("song6", "artist6"));
    }

    // Thread safe implementation
    public synchronized static AllSongs getInstance(){
        if (sSoleInstance == null){ //if there is no instance available... create new one
            sSoleInstance = new AllSongs();
        }

        return sSoleInstance;
    }
}
