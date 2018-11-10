package com.propro.musicplayerapp;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
                    new String[]{"%" + path + "%"},
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
                //add songs to list
                do {
                    long thisId = musicCursor.getLong(idColumn);
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);

                    // Add song if not duplicate
                    boolean isDuplicate = false;
                    for (SongInfo song : AllSongs.getInstance()){
                        if (song.Id == thisId){
                            isDuplicate = true;
                        }
                    }
                    if (!isDuplicate){
                        Log.d("SONG ID: ", String.valueOf(thisId));
                        AllSongs.getInstance().add(new SongInfo(thisId, thisTitle, thisArtist));
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
