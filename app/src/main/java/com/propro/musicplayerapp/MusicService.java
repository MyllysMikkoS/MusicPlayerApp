package com.propro.musicplayerapp;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class MusicService extends Service
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    private boolean isPrepared;
    private MediaButtons mediaButtons;

    @Override
    public void onCreate() {
        // create the service
        super.onCreate();
        // initialize position
        songPosn=0;
        // create player
        player = new MediaPlayer();
        // initialize preparation state
        isPrepared = false;
        // initialize Music Player
        initMusicPlayer();
    }

    public void initMusicPlayer(){
        // set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // set listeners
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        isPrepared = false;
        QueueSongs.getInstance().remove(0);

        // Continue queue if songs left in queue
        if (QueueSongs.getInstance().size() > 0) {
            playSong();
        }
        else {
            MediaButtons.pause = true;
            mediaButtons.invalidate();
            Toast.makeText(this, "No songs in queue",
                    Toast.LENGTH_SHORT).show();
        }

        //TODO: DELETE FROM QUEUE LISTVIEW AND UPDATE IF IN QUEUE ACTIVITY WHILE SONG ENDS/IS SKIPPED
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // start playback
        if (!isPrepared){
            mp.start();
            isPrepared = true;
        }
    }

    public class MusicBinder extends Binder {
        MusicService getService(MediaButtons mediaButtonsView) {
            mediaButtons = mediaButtonsView;
            return MusicService.this;
        }
    }

    public void playSong(){
        try {
            if (QueueSongs.getInstance().get(songPosn) != null) {
                Log.d("METHOD: ", "playSong called");
                // play a song
                player.reset();
                // get song
                SongInfo song = QueueSongs.getInstance().get(songPosn);
                // get id
                long currSong = song.Id;
                // set uri
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
                // try playing the song
                try {
                    player.setDataSource(getApplicationContext(), trackUri);
                } catch (Exception e) {
                    Log.e("MUSIC SERVICE: ", "Error setting data source", e);
                }
                player.prepareAsync();
            } else {
                Log.d("MUSIC SERVICE: ", "No songs in queue");
            }
        }
        catch (Exception e){
            Log.e("MUSIC SERVICE: ", "Error playing the song", e);
        }
    }

    public void continueQueue(){
        try {
            Log.d("Player is prepared: ", String.valueOf(isPrepared));
            if (isPrepared) {
                player.start();
            }
            else {
                playSong();
            }
        } catch (Exception e) {
            Log.e("MUSIC SERVICE: ", "Error continuing queue", e);
        }
    }

    public void pauseQueue(){
        try {
            player.pause();
        } catch (Exception e) {
            Log.e("MUSIC SERVICE: ", "Error pausing queue", e);
        }
    }

    public void skipToNext(){
        try {
            if (QueueSongs.getInstance().size() > 1){
                player.stop();
                QueueSongs.getInstance().remove(0);
                isPrepared = false;
                playSong();
            }
            else {
                stopPlaying();
                Toast.makeText(this, "No songs in queue",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("MUSIC SERVICE: ", "Error skipping song", e);
        }
    }

    public void stopPlaying(){
        try {
            MediaButtons.pause = true;
            isPrepared = false;
            player.stop();
            QueueSongs.getInstance().remove(0);
            mediaButtons.invalidate();
        } catch (Exception e) {
            Log.e("MUSIC SERVICE: ", "Error stopping song", e);
        }
    }

    public Boolean isPlaying(){
        return player.isPlaying();
    }
}
