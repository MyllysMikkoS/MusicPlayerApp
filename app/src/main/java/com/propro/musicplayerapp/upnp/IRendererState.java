package com.propro.musicplayerapp.upnp;

public interface IRendererState {

    // Play state
    public enum State
    {
        PLAY, PAUSE, STOP
    }

    public State getState();

    void setState(State state);

    public int getVolume();

    void setVolume(int volume);

    public boolean isMute();

    void setMute(boolean mute);

    public String getRemainingDuration();

    public String getDuration();

    public String getPosition();

    public int getElapsedPercent();

    public long getDurationSeconds();

    public long getElapsedSeconds();

    public String getTitle();

    public String getArtist();

}
