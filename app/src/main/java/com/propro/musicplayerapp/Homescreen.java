package com.propro.musicplayerapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Homescreen extends AppCompatActivity {

    // Views
    Toolbar toolbar;
    RelativeLayout mediaButtonsRelativeLayout;
    MediaButtons mediaButtons;
    TextView songTitle;
    TextView artistName;
    TextView streamingInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_homescreen);

        // Init views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch(NullPointerException e){
            Log.d("Removing title error: ", e.toString());
        }
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        songTitle = findViewById(R.id.songTitleTextView);
        artistName = findViewById(R.id.artistNameTextView);
        streamingInfo = findViewById(R.id.streamingTextView);

        // Create MediaButtonsWidget
        mediaButtonsRelativeLayout = findViewById(R.id.MediaButtonsRelativeLayout);
        mediaButtons = new MediaButtons(this);
        mediaButtons.setOnSliceClickListener(new MediaButtons.OnSliceClickListener() {
            @Override
            public void onSlickClick(int slicePosition, float progress) {
                String text = String.valueOf(slicePosition) + " " + String.valueOf(progress);
                songTitle.setText(text);
            }
        });
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mediaButtonsRelativeLayout.addView(mediaButtons, params);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent settings = new Intent(this, Settings.class);
                startActivity(settings);
                return true;

            case R.id.action_music:
                Intent music = new Intent(this, Music.class);
                startActivity(music);
                return true;

            case R.id.action_stream:
                Intent stream = new Intent(this, Stream.class);
                startActivity(stream);
                return true;

            case R.id.action_queue:
                Intent queue = new Intent(this, Queue.class);
                startActivity(queue);
                return true;
        }

        return(super.onOptionsItemSelected(item));
    }
}
