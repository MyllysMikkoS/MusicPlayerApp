package com.propro.musicplayerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class Queue extends AppCompatActivity {

    Toolbar toolbar;
    ListView queueListView;

    private static QueueAdapter adapter;
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

        // TESTING --
        QueueSongs songs = QueueSongs.getInstance();
        for (SongInfo song : songs) {
            adapter.add(song);
        }
        // TESTING --

        queueListView.setAdapter(adapter);
    }
}
