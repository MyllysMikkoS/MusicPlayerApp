package com.propro.musicplayerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
        settingsListView = findViewById(R.id.settingsListView);

        // Later the path are loaded from static container that holds all paths
        paths = new ArrayList<Source>();
        adapter = new SourcesAdapter(this, paths);
        settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.getCount() > 0) {
                    adapter.remove(paths.get(position));
                    MusicSources.getInstance().remove(position);
                }
                Log.d("ITEMS IN SOURCE LIST: ", "i: " + paths.size());
            }
        });

        // TESTING --
        MusicSources sources = MusicSources.getInstance();
        for (Source source : sources) {
            adapter.add(source);
        }
        // TESTING --

        settingsListView.setAdapter(adapter);
    }
}
