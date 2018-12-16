package com.propro.musicplayerapp.upnp;

public interface IRendererCommand {

    // Pause/resume backgroud state update
    public void pause();

    public void resume();

    // / Status
    public void commandPlay();

    public void commandStop();

    public void commandPause();

    public void commandToggle();

    public void updateStatus();

    // / Position
    public void commandSeek(String relativeTimeTarget);

    public void updatePosition();

    // / Volume
    public void setVolume(final int volume);

    public void setMute(final boolean mute);

    public void toggleMute();

    public void updateVolume();

    // / URI
    public void launchItem(final IDIDLItem uri);

    // / Full
    public void updateFull();

    public void prepareNextSong(boolean forceStartAgain);

    public void skipToNextSong();

    //public void toPreviousSong();
}
