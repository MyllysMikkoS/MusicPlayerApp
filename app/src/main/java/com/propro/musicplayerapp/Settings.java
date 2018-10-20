package com.propro.musicplayerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class Settings extends AppCompatActivity {

    Toolbar toolbar;
    ListView settingsListView;

    private static SourcesAdapter adapter;
    ArrayList<Source> paths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.setTitle("Settings");

        // Init views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        settingsListView = findViewById(R.id.queueListView);

        paths = new ArrayList<Source>();
        adapter = new SourcesAdapter(this, paths);

        // TESTING --
        MusicSources sources = MusicSources.getInstance();
        for (Source source : sources) {
            adapter.add(source);
        }
        // TESTING --

        settingsListView.setAdapter(adapter);
    }
}
