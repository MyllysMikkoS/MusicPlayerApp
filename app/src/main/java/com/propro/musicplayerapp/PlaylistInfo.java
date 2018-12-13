package com.propro.musicplayerapp;

import java.util.ArrayList;

public class PlaylistInfo {
    public String Name;
    public ArrayList<Long> SongIds;

    public PlaylistInfo(String name, ArrayList<Long> songIds){
        this.Name = name;
        this.SongIds = songIds;
    }
}
