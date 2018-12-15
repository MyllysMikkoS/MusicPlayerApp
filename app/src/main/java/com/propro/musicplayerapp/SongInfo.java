package com.propro.musicplayerapp;

import org.fourthline.cling.support.model.item.MusicTrack;

public class SongInfo {
    public long Id;
    public String Title;
    public String Artist;
    public int Length; // in milliseconds
    public MusicTrack musicTrack;
    public String path;
    public String mime_type;

    public SongInfo(long id, String title, String artist, int length){
        this.Id = id;
        this.Title = title;
        this.Artist = artist;
        this.Length = length;
    }

    public SongInfo(long id, String title, String artist, int length, MusicTrack track, String path, String mime){
        this.Id = id;
        this.Title = title;
        this.Artist = artist;
        this.Length = length;
        this.musicTrack = track;
        this.path = path;
        this.mime_type = mime;
    }

}
