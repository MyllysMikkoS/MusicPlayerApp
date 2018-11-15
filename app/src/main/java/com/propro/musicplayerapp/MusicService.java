package com.propro.musicplayerapp;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;

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
    private TextView titleView;
    private TextView artistView;
    private ArrayList<SongInfo> songHistory;
    private int previousSwapTimeInMillis = 1000;

    // flag that should be set true if handler should stop
    boolean mStopHandler = false;
    // Handler for runnable
    Handler mHandler = new Handler();
    // Progressbar updater runnable
    Runnable uiUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mStopHandler) {
                if (player.isPlaying()) {
                    Log.d("PROGRESSBAR UPDATE: ", "Called");
                    updateProgressBar();
                }
                mHandler.postDelayed(this, 1000);
            }
        }
    };

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

        try {
            //Set song info
            setSongInfo();
        } catch (Exception e){
            Log.d("MUSIC SERVICE: ", "SONG INFO ERROR ON COMPLETION: " + e.toString());
        }

        mStopHandler = true;
        MediaButtons.progress = 0;
        updateProgressBar();
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
        // Set song info
        setSongInfo();

        // start playback
        if (!isPrepared){
            mp.start();
            isPrepared = true;
            setSongInfo();

            // Start progressBar animation
            mStopHandler = false;
            mHandler.removeCallbacksAndMessages(null);
            mHandler.post(uiUpdateRunnable);
        }
    }

    public class MusicBinder extends Binder {
        MusicService getService(MediaButtons mediaButtonsView, TextView title, TextView artist) {
            mediaButtons = mediaButtonsView;
            titleView = title;
            artistView = artist;
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
                    if (!player.isPlaying()) {
                        player.start();
                        mStopHandler = false;
                        mHandler.removeCallbacksAndMessages(null);
                        mHandler.post(uiUpdateRunnable);
                    }
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
                if (player.isPlaying()) {
                    player.pause();
                    mStopHandler = true;
                }
            }
            else {
                CustomUtilities.showToast(this, "No songs in queue");
            }
            //Set song info
            setSongInfo();
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

            //Set song info
            setSongInfo();
            updateProgressBar();
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
                    MediaButtons.progress = 0;
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
            //Set song info
            setSongInfo();
            updateProgressBar();
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

    public void setSongInfo(){
        if (QueueSongs.getInstance().size() > 0) {
            int seconds = QueueSongs.getInstance().get(0).Length / 1000;

            // If song length less than minute
            if (seconds < 0){
                MediaButtons.songLength = "00:00";
            }
            if (seconds < 60 && seconds >= 0){
                if (seconds < 10) MediaButtons.songLength = "00:0" + seconds;
                else MediaButtons.songLength = "00:" + seconds;
            }
            // Otherwise
            else {
                int minutes = seconds / 60;
                int remainingSeconds = seconds - minutes*60;

                if (minutes < 10 && remainingSeconds < 10){
                    MediaButtons.songLength = "0" + minutes + ":0" + remainingSeconds;
                }
                else if (minutes >= 10 && remainingSeconds < 10){
                    MediaButtons.songLength = minutes + ":0" + remainingSeconds;
                }
                else if (minutes < 10){
                    MediaButtons.songLength = "0" + minutes + ":" + remainingSeconds;
                }
                else {
                    MediaButtons.songLength = minutes + ":" + remainingSeconds;
                }
            }

            // Set current time
            updateCurrentTime();

            // Set title and artist
            titleView.setText(QueueSongs.getInstance().get(0).Title);
            artistView.setText(QueueSongs.getInstance().get(0).Artist);

            mediaButtons.invalidate();
        }
        else {
            // set titles "no song" and times 00:00
            titleView.setText("NO SONG");
            artistView.setText("NO ARTIST");
            MediaButtons.currentTime = "00:00";
            MediaButtons.songLength = "00:00";
            mediaButtons.invalidate();
        }
    }

    public void progressBarChange(){
        try {
            if (QueueSongs.getInstance().size() > 0 && isPrepared) {
                int newProgress = Math.round(player.getDuration() * (MediaButtons.progress / 100));
                player.seekTo(newProgress);
                setSongInfo();
            }
            else {
                MediaButtons.progress = 0;
                setSongInfo();
            }
        }
        catch (Exception e){
            Log.d("MUSIC SERVICE: ", "Progress bar error: " + e.toString());
            player.seekTo(0);
            MediaButtons.progress = 0;
            //setSongInfo();
        }
    }

    public void updateCurrentTime(){
        // Set current time
        try {
            int currentProgressInSeconds;
            if (isPrepared) {
                float curr = player.getCurrentPosition();
                float divider = 1000;
                float num = Math.round(curr / divider);
                currentProgressInSeconds = Math.round(num);
            }
            else currentProgressInSeconds = 0;

            if (currentProgressInSeconds < 0){
                MediaButtons.songLength = "00:00";
            }
            if (currentProgressInSeconds < 60 && currentProgressInSeconds >= 0){
                if (currentProgressInSeconds < 10) MediaButtons.currentTime = "00:0" + currentProgressInSeconds;
                else MediaButtons.currentTime = "00:" + currentProgressInSeconds;
            }
            else {
                int mins = currentProgressInSeconds / 60;
                int secs = currentProgressInSeconds - mins*60;

                if (mins < 10 && secs < 10){
                    MediaButtons.currentTime = "0" + mins + ":0" + secs;
                }
                else if (mins >= 10 && secs < 10){
                    MediaButtons.currentTime = mins + ":0" + secs;
                }
                else if (mins < 10){
                    MediaButtons.currentTime = "0" + mins + ":" + secs;
                }
                else {
                    MediaButtons.currentTime = mins + ":" + secs;
                }
            }
            mediaButtons.invalidate();
        }
        catch (Exception e){
            Log.d("MUSIC SERVICE: ", "No player prepared, default time is 00:00");
            MediaButtons.currentTime = "00:00";
            mediaButtons.invalidate();
        }
    }

    public void updateProgressBar(){
        try {
            if (player.isPlaying()) {
                float seconds = player.getCurrentPosition();
                float secondsOverall = player.getDuration();
                float progress = (seconds / secondsOverall) * 100f;
                Log.d("OVERALL PROGRESS: ", progress + "% = " + seconds + " seconds, current: " + player.getCurrentPosition());
                MediaButtons.progress = progress;
                mediaButtons.invalidate();
                updateCurrentTime();
            }
            else if (!isPrepared){
                MediaButtons.progress = 0;
                mediaButtons.invalidate();
            }
        } catch (Exception e){
            Log.d("MUSIC SERVICE: ", "Progressbar update error: " + e.toString());
        }
    }
}
