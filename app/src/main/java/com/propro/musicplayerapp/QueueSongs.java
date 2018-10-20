package com.propro.musicplayerapp;

import java.util.ArrayList;

public class QueueSongs extends ArrayList<SongInfo> {

    private static QueueSongs sSoleInstance;

    // private constructor.
    private QueueSongs(){
        //Prevent form the reflection api.
        if (sSoleInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

        add(new SongInfo("song1", "artist1"));
        add(new SongInfo("song2", "artist2"));
        add(new SongInfo("song3", "artist3"));
        add(new SongInfo("song4", "artist4"));
    }

    // Thread safe implementation
    public synchronized static QueueSongs getInstance(){
        if (sSoleInstance == null){ //if there is no instance available... create new one
            sSoleInstance = new QueueSongs();
        }

        return sSoleInstance;
    }
}
