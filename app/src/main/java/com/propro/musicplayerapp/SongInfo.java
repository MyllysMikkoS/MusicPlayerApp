package com.propro.musicplayerapp;

public class SongInfo {
    public long Id;
    public String Title;
    public String Artist;
    public int Length; // in milliseconds

    public SongInfo(long id, String title, String artist, int length){
        this.Id = id;
        this.Title = title;
        this.Artist = artist;
        this.Length = length;
    }
}
