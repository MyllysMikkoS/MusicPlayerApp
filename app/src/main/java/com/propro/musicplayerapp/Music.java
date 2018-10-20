package com.propro.musicplayerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class Music extends AppCompatActivity {

    Toolbar toolbar;
    Button songButton;
    Button playlistButton;
    ListView musicListView;

    private static PlaylistsAdapter playlistsAdapter;
    ArrayList<PlaylistInfo> playlists;
    private static SongsAdapter songsAdapter;
    ArrayList<SongInfo> allSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        this.setTitle("Music");

        // Init views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        songButton = findViewById(R.id.songsButton);
        songButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songsAdapter.clear();
                AllSongs allSgs = AllSongs.getInstance();
                for (SongInfo song : allSgs) {
                    songsAdapter.add(song);
                }
                musicListView.setAdapter(songsAdapter);
            }
        });
        playlistButton = findViewById(R.id.playlistsButton);
        playlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlistsAdapter.clear();
                CustomPlaylists pls = CustomPlaylists.getInstance();
                for (PlaylistInfo pl : pls) {
                    playlistsAdapter.add(pl);
                }
                musicListView.setAdapter(playlistsAdapter);
            }
        });
        musicListView = findViewById(R.id.musicListView);

        playlists = new ArrayList<PlaylistInfo>();
        playlistsAdapter = new PlaylistsAdapter(this, playlists);
        allSongs = new ArrayList<SongInfo>();
        songsAdapter = new SongsAdapter(this, allSongs);

        // TESTING --
        CustomPlaylists pls = CustomPlaylists.getInstance();
        for (PlaylistInfo pl : pls) {
            playlistsAdapter.add(pl);
        }
        // TESTING --

        musicListView.setAdapter(playlistsAdapter);
    }
}
