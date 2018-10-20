package com.propro.musicplayerapp;

import java.util.ArrayList;

public class CustomPlaylists extends ArrayList<PlaylistInfo> {

    private static CustomPlaylists sSoleInstance;

    // private constructor.
    private CustomPlaylists(){
        //Prevent form the reflection api.
        if (sSoleInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

        add(new PlaylistInfo("Playlist1"));
        add(new PlaylistInfo("Playlist2"));
        add(new PlaylistInfo("Playlist3"));
        add(new PlaylistInfo("Playlist4"));
    }

    // Thread safe implementation
    public synchronized static CustomPlaylists getInstance(){
        if (sSoleInstance == null){ //if there is no instance available... create new one
            sSoleInstance = new CustomPlaylists();
        }

        return sSoleInstance;
    }
}
