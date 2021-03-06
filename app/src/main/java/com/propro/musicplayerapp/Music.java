package com.propro.musicplayerapp;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Music extends AppCompatActivity {

    Toolbar toolbar;
    Button songButton;
    Button playlistButton;
    ListView musicListView;

    private static PlaylistsAdapter playlistsAdapter;
    ArrayList<PlaylistInfo> playlists;
    private static SongsAdapter songsAdapter;
    ArrayList<SongInfo> allSongs;

    // Music content
    ContentResolver musicResolver;
    Uri musicUri;
    Cursor musicCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        this.setTitle("Music");

        getSongList();

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
                songButton.setBackgroundColor(Color.WHITE);
                songButton.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.button_color));
                playlistButton.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.button_color));
                playlistButton.setTextColor(Color.WHITE);
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
                songButton.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.button_color));
                songButton.setTextColor(Color.WHITE);
                playlistButton.setBackgroundColor(Color.WHITE);
                playlistButton.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.button_color));
            }
        });
        musicListView = findViewById(R.id.musicListView);

        playlists = new ArrayList<PlaylistInfo>();
        playlistsAdapter = new PlaylistsAdapter(this, playlists);
        allSongs = new ArrayList<SongInfo>();
        songsAdapter = new SongsAdapter(this, allSongs);

        // Set songs to be viewed by default
        songsAdapter.clear();
        AllSongs allSgs = AllSongs.getInstance();
        for (SongInfo song : allSgs) {
            songsAdapter.add(song);
        }
        musicListView.setAdapter(songsAdapter);
        songButton.setBackgroundColor(Color.WHITE);
        songButton.setTextColor(ContextCompat.getColor(this, R.color.button_color));

        // Update playlists
        CustomUtilities.readPlaylists(getApplicationContext());
    }

    public void getSongList(){

        // Clear old list
        AllSongs.getInstance().clear();

        // Get songs from every path
        for ( Source source : MusicSources.getInstance()) {
            musicResolver = getContentResolver();
            String path = source.path;
            musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Log.d("PATH: ", path);
            musicCursor = musicResolver.query(
                    musicUri,
                    null,
                    MediaStore.Audio.Media.DATA + " like ? ",
                    new String[]{"%" + path + "/%"},
                    null);

            // Get local music
            if (musicCursor != null && musicCursor.moveToFirst()) {
                //get columns
                int titleColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.TITLE);
                int idColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media._ID);
                int artistColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.ARTIST);
                int songLength = musicCursor.getColumnIndex
                        (MediaStore.Audio.Media.DURATION);
                //add songs to list
                do {
                    long thisId = musicCursor.getLong(idColumn);
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);
                    int length = musicCursor.getInt(songLength);

                    // Add song if not duplicate
                    boolean isDuplicate = false;
                    for (SongInfo song : AllSongs.getInstance()){
                        if (song.Id == thisId){
                            isDuplicate = true;
                        }
                    }
                    if (!isDuplicate){
                        Log.d("SONG ID: ", String.valueOf(thisId));
                        AllSongs.getInstance().add(new SongInfo(thisId, thisTitle, thisArtist, length));
                    }
                    else {
                        Log.d("SONG ID DUPLICATE: ", String.valueOf(thisId));
                    }
                }
                while (musicCursor.moveToNext());
            }
        }

        // Sort list alphabetically
        Collections.sort(AllSongs.getInstance(), new Comparator<SongInfo>() {
            @Override
            public int compare(SongInfo a, SongInfo b) {
                return a.Title.compareTo(b.Title);
            }
        });
    }
}
