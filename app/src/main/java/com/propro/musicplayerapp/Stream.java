package com.propro.musicplayerapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

public class Stream extends AppCompatActivity {

    Toolbar toolbar;
    ListView devicesListView;

    private static StreamAdapter adapter;
    ArrayList<DeviceInfo> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        this.setTitle("Streaming devices");

        // Init views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        devicesListView = findViewById(R.id.devicesListView);

        // Later the paths are loaded from static container that holds all paths
        devices = new ArrayList<DeviceInfo>();
        adapter = new StreamAdapter(this, devices);

        // TESTING --
        StreamingDevices streamingDevices = StreamingDevices.getInstance();
        for (DeviceInfo deviceInfo : streamingDevices) {
            adapter.add(deviceInfo);
        }
        // TESTING --

        devicesListView.setAdapter(adapter);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.streaming_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_refresh:
                Log.d("Refresh: ", "CALLED");
                return true;
        }

        return(super.onOptionsItemSelected(item));
    }
}
