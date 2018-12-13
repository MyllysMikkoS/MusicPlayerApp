package com.propro.musicplayerapp;

import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Homescreen extends AppCompatActivity {

    // Views
    Toolbar toolbar;
    RelativeLayout mediaButtonsRelativeLayout;
    MediaButtons mediaButtons;
    TextView songTitle;
    TextView artistName;
    TextView streamingInfo;

    // Player
    public static MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_homescreen);

        // Ask for permissions
        PermissionChecks.getInstance().checkPermissionREAD_EXTERNAL_STORAGE(this);
        PermissionChecks.getInstance().checkPermissionWRITE_EXTERNAL_STORAGE(this);
        PermissionChecks.getInstance().checkPermissionWAKE_LOCK(this);

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
                //String text = String.valueOf(slicePosition) + " " + String.valueOf(progress);
                //songTitle.setText(text);

                // WHEN PLAY/PAUSE BUTTON IS CLICKED
                if (slicePosition == -2 && !MediaButtons.pause) {
                    musicService.continueQueue();
                }
                else if (slicePosition == -2 && MediaButtons.pause){
                    musicService.pauseQueue();
                }
                // PRESSING SKIP NEXT
                if (slicePosition == 0){
                    musicService.skipToNext();
                }
                // PRESSING PREVIOUS SONG
                if (slicePosition == 3){
                    musicService.previousSong();
                }
                if (slicePosition == -1){
                    musicService.progressBarChange();
                }
            }
        });
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mediaButtonsRelativeLayout.addView(mediaButtons, params);

        // Read playlists
        CustomUtilities.readPlaylists(this);

        // Read music sources
        CustomUtilities.readMusicSources(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicService != null) {
            musicService.setSongInfo();
        }

        // if sources are empty, move to settings to add sources
        if (MusicSources.getInstance().size() == 0){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No music");

            // Set the test
            builder.setMessage("There is currently no music in the library. Please set music sources from settings to start listening music.");

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent settings = new Intent(getApplicationContext(), Settings.class);
                    startActivity(settings);
                }
            });

            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (musicConnection != null){
            unbindService(musicConnection);
        }
        Log.d("ONDESTROY: ", "CALLED");
    }

    // connect to MusicService
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            // get service
            musicService = binder.getService(mediaButtons, songTitle, artistName);
            // pass list
            musicBound = true;
            // Update all info on activity first time
            try {
                musicService.progressBarChange();
            }
            catch (Exception e){
                Log.e("HOMESCREEN: ", "Progress init error", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            musicBound = false;
        }
    };

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
                // Check that permissions are granted, otherwise go back
                if (PermissionChecks.getInstance().checkPermissionREAD_EXTERNAL_STORAGE(this)){
                    if (PermissionChecks.getInstance().checkPermissionWRITE_EXTERNAL_STORAGE(this)) {
                        if (PermissionChecks.getInstance().checkPermissionWAKE_LOCK(this)){
                            Intent music = new Intent(this, Music.class);
                            startActivity(music);
                        }
                        else {
                            CustomUtilities.showToast(this, "No Wake-Lock Permission");
                        }
                    }
                    else {
                        CustomUtilities.showToast(this, "No Write-External-Storage Permission");
                    }
                }
                else {
                    CustomUtilities.showToast(this, "No Read-External-Storage Permission");
                }
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
