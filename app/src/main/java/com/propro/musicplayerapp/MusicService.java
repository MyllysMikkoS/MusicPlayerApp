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

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    private boolean isPrepared;
    private MediaButtons mediaButtons;
    private ArrayList<SongInfo> songHistory;
    private int previousSwapTimeInMillis = 1000;

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
        // initialize song history
        songHistory = new ArrayList<SongInfo>();
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
        if (!MediaButtons.repeat) {
            Log.d("MUSIC SERVICE: ", "Song completed");
            isPrepared = false;
            // Add to song history and remove from QueueSongs
            if (QueueSongs.getInstance().size() > 0) {
                // If adapter is initialized then remove from queue listview
                if (Queue.adapter != null) Queue.adapter.remove(QueueSongs.getInstance().get(0));
                songHistory.add(0, QueueSongs.getInstance().get(0));
                QueueSongs.getInstance().remove(0);
            }

            // If shuffle is on then swap random song from queue to index 0
            if (MediaButtons.shuffle && QueueSongs.getInstance().size() > 1){
                shuffleSong();
            }

            // Continue queue if songs left in queue
            if (QueueSongs.getInstance().size() > 0) {
                // Play song again if not paused
                if (!MediaButtons.pause) playSong();
            } else {
                MediaButtons.pause = true;
                mediaButtons.invalidate();
                CustomUtilities.showToast(this, "No songs in queue");
            }
        }
        else {
            Log.d("MUSIC SERVICE: ", "Player is looping");
            isPrepared = false;
            // Play song again if not paused
            if (!MediaButtons.pause) playSong();
        }
    }

    public void shuffleSong(){
        // Get random index from song queue and swap song in that index to be the first one
        int arraySize = QueueSongs.getInstance().size();
        int randomIndex = new Random().nextInt(arraySize);
        Log.d("MUSIC SERVICE: ", "RANDOM INDEX: " + randomIndex + " ARRAYSIZE: " + arraySize);
        SongInfo swappedSong = QueueSongs.getInstance().get(randomIndex);
        QueueSongs.getInstance().remove(randomIndex);
        QueueSongs.getInstance().add(0, swappedSong);

        // After shuffle recreate adapter list
        if (Queue.adapter != null) {
            Queue.adapter.clear();
            QueueSongs songs = QueueSongs.getInstance();
            for (SongInfo song : songs) {
                Queue.adapter.add(song);
            }
        }
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
            if (QueueSongs.getInstance().size() > 0) {
                Log.d("METHOD: ", "playSong called");
                // set mediabuttons
                MediaButtons.pause = false;
                mediaButtons.invalidate();
                // reset player
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
                CustomUtilities.showToast(this, "No songs in queue");
            }
        }
        catch (Exception e){
            Log.e("MUSIC SERVICE: ", "Error playing the song", e);
        }
    }

    public void continueQueue(){
        try {
            if (QueueSongs.getInstance().size() > 0) {
                Log.d("Player is prepared: ", String.valueOf(isPrepared));
                if (isPrepared) {
                    if (!player.isPlaying()) player.start();
                } else {
                    playSong();
                }
            }
            else {
                CustomUtilities.showToast(this, "No songs in queue");
            }
        } catch (Exception e) {
            Log.e("MUSIC SERVICE: ", "Error continuing queue", e);
        }
    }

    public void pauseQueue(){
        try {
            Log.d("Player: ", "PAUSE CALLED");
            if (QueueSongs.getInstance().size() > 0) {
                if (player.isPlaying()) player.pause();
            }
            else {
                CustomUtilities.showToast(this, "No songs in queue");
            }
        } catch (Exception e) {
            Log.e("MUSIC SERVICE: ", "Error pausing queue", e);
        }
    }

    public void skipToNext(){
        try {
            if (QueueSongs.getInstance().size() > 1){
                // if player was playing before skip then automatically continue
                Boolean isPlaying = player.isPlaying();
                stopPlaying();

                // If shuffle is on then swap random song from queue to index 0
                if (MediaButtons.shuffle && QueueSongs.getInstance().size() > 1){
                    shuffleSong();
                }

                if (isPlaying) playSong();
            }
            else if (QueueSongs.getInstance().size() == 1) {
                stopPlaying();
                CustomUtilities.showToast(this, "No songs in queue");
            }
            else if (QueueSongs.getInstance().size() == 0){
                CustomUtilities.showToast(this, "No songs in queue");
            }
        } catch (Exception e) {
            Log.e("MUSIC SERVICE: ", "Error skipping song", e);
        }
    }

    public void previousSong(){
        try {
            Log.d("PLAYER POSITION: ", isPrepared + " " + QueueSongs.getInstance().size() + " " + songHistory.size());
            if (QueueSongs.getInstance().size() == 0 && songHistory.size() == 0){
                CustomUtilities.showToast(this, "No previously played songs");
            }
            else if (QueueSongs.getInstance().size() == 0 && songHistory.size() > 0){
                // Continue automatically if player was playing before
                Boolean wasPLaying = player.isPlaying();
                QueueSongs.getInstance().add(0, songHistory.get(0));
                songHistory.remove(0);
                if (wasPLaying) playSong();
            }
            else if (QueueSongs.getInstance().size() > 0 && songHistory.size() == 0){
                if (isPrepared && player.getCurrentPosition() > previousSwapTimeInMillis){
                    player.seekTo(0);
                }
                else {
                    CustomUtilities.showToast(this, "No previously played songs");
                }
            }
            else if (QueueSongs.getInstance().size() > 0 && songHistory.size() > 0){
                if (isPrepared && player.getCurrentPosition() > previousSwapTimeInMillis){
                    player.seekTo(0);
                }
                else {
                    // Continue automatically if player was playing before
                    Boolean wasPLaying = player.isPlaying();
                    stopPlayingGoToPrevious();
                    QueueSongs.getInstance().add(0, songHistory.get(0));
                    songHistory.remove(0);
                    if (wasPLaying) playSong();
                }
            }
        } catch (Exception e) {
            Log.e("MUSIC SERVICE: ", "Previous song error", e);
        }
    }

    public void stopPlaying(){
        try {
            MediaButtons.pause = true;
            if (isPrepared){
                player.stop();
                isPrepared = false;
            }
            // Add to song history
            songHistory.add(0, QueueSongs.getInstance().get(0));
            // Remove song from QueueSongs
            QueueSongs.getInstance().remove(0);
            mediaButtons.invalidate();
        } catch (Exception e) {
            Log.e("MUSIC SERVICE: ", "Error stopping song", e);
        }
    }

    public void stopPlayingGoToPrevious(){
        try {
            MediaButtons.pause = true;
            if (isPrepared){
                player.stop();
                isPrepared = false;
            }
            mediaButtons.invalidate();
        } catch (Exception e) {
            Log.e("MUSIC SERVICE: ", "Error stopping song", e);
        }
    }

    public Boolean isPlaying(){
        return player.isPlaying();
    }
}
