package com.propro.musicplayerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

public class Homescreen extends AppCompatActivity {

    // Views
    android.support.v7.widget.Toolbar toolbar;
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
                artistName.setText("settings");
                return true;

            case R.id.action_music:
                artistName.setText("music");
                return true;

            case R.id.action_stream:
                artistName.setText("stream");
                return true;

            case R.id.action_queue:
                artistName.setText("queue");
                return true;
        }

        return(super.onOptionsItemSelected(item));
    }
}
