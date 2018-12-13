package com.propro.musicplayerapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class Queue extends AppCompatActivity {

    Toolbar toolbar;
    ListView queueListView;
    Button clearQueueBtn;
    Button savePlaylistBtn;

    public static QueueAdapter adapter;
    ArrayList<SongInfo> queueSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        this.setTitle("Queue");

        // Init views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        queueListView = findViewById(R.id.queueListView);
        clearQueueBtn = findViewById(R.id.clearQueueButton);
        savePlaylistBtn = findViewById(R.id.saveQueueButton);

        queueSongs = new ArrayList<SongInfo>();
        adapter = new QueueAdapter(this, queueSongs);
        queueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.getCount() > 0) {
                    adapter.remove(queueSongs.get(position));
                    QueueSongs.getInstance().remove(position);
                }
                Log.d("ITEMS IN QUEUE: ", "i: " + queueSongs.size());
            }
        });
        clearQueueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Homescreen.musicService.isPlaying()){
                    // If player is playing, stop it and clear queue
                    Homescreen.musicService.stopPlaying();
                    Homescreen.musicService.updateProgressBar();
                    QueueSongs.getInstance().clear();
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                }
                else {
                    // If player is paused just clear queue
                    QueueSongs.getInstance().clear();
                    Homescreen.musicService.updateProgressBar();
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                }
                Log.d("ITEMS IN QUEUE: ", "i: " + QueueSongs.getInstance().size());
            }
        });
        savePlaylistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QueueSongs.getInstance().size() > 0){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Queue.this);
                    builder.setTitle("Playlist name:");

                    // Set up the input
                    final EditText input = new EditText(Queue.this);
                    //input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", null);
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog dialog = builder.create();
                            dialog.show();
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String name = input.getText().toString();
                                    boolean alreadyExists = false;
                                    for (PlaylistInfo pl : CustomPlaylists.getInstance()){
                                        if (pl.Name.equals(name)) alreadyExists = true;
                                    }

                                    if (!name.equals("")) {
                                        if (!alreadyExists) {
                                            // Add songs to playlist
                                            ArrayList<Long> songIds = new ArrayList<Long>();
                                            for (SongInfo song : QueueSongs.getInstance()) {
                                                songIds.add(song.Id);
                                            }
                                            PlaylistInfo newPlaylist = new PlaylistInfo(input.getText().toString(), songIds);
                                            CustomPlaylists.getInstance().add(newPlaylist);

                                            // Update playlists
                                            CustomUtilities.updatePlaylists(getApplicationContext());

                                            CustomUtilities.showToast(getApplicationContext(), "Playlist " + input.getText().toString() + " created");

                                            dialog.dismiss();
                                        }
                                        else {
                                            CustomUtilities.showToast(getApplicationContext(), "Playlist with same name already exists");
                                        }
                                    }
                                    else {
                                        CustomUtilities.showToast(getApplicationContext(), "Playlist name cannot be empty");
                                    }
                                }
                            });
                        }
                    });
                }
                else {
                    CustomUtilities.showToast(getBaseContext(), "No songs in queue to create playlist");
                }
            }
        });

        // Show songs
        QueueSongs songs = QueueSongs.getInstance();
        for (SongInfo song : songs) {
            adapter.add(song);
        }
        queueListView.setAdapter(adapter);
    }
}
