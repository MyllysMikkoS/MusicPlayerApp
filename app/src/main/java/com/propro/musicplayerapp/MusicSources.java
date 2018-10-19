package com.propro.musicplayerapp;

import java.util.ArrayList;

public class MusicSources extends ArrayList<Source> {

    private static MusicSources sSoleInstance;

    // private constructor.
    private MusicSources(){
        //Prevent form the reflection api.
        if (sSoleInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

        add(new Source("music/own_music/very_long_path/lets_see_if_it/fits"));
        add(new Source("downloads"));
        add(new Source("tmp/tmp_music"));
        add(new Source("own/music"));
    }

    // Thread safe implementation
    public synchronized static MusicSources getInstance(){
        if (sSoleInstance == null){ //if there is no instance available... create new one
            sSoleInstance = new MusicSources();
        }

        return sSoleInstance;
    }
}
