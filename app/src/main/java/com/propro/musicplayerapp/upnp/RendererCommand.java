package com.propro.musicplayerapp.upnp;

import com.propro.musicplayerapp.AllSongs;
import com.propro.musicplayerapp.Homescreen;
import com.propro.musicplayerapp.MediaButtons;
import com.propro.musicplayerapp.Queue;
import com.propro.musicplayerapp.QueueSongs;
import com.propro.musicplayerapp.SongInfo;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.PlaylistItem;
import org.fourthline.cling.support.model.item.TextItem;
import org.fourthline.cling.support.model.item.VideoItem;
import org.fourthline.cling.support.renderingcontrol.callback.GetMute;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;
import org.fourthline.cling.support.renderingcontrol.callback.SetMute;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

@SuppressWarnings("rawtypes")
public class RendererCommand implements Runnable, IRendererCommand {

    private static final String TAG = "RendererCommand";

    private final RendererState rendererState;
    private final ControlPoint controlPoint;

    private ArrayList<SongInfo> songHistory;

    public Thread thread;
    boolean pause = false;

    public RendererCommand(ControlPoint controlPoint, RendererState rendererState)
    {
        this.rendererState = rendererState;
        this.controlPoint = controlPoint;

        this.songHistory = new ArrayList<SongInfo>();

        thread = new Thread(this);
        pause = true;
    }

    @Override
    public void finalize()
    {
        this.pause();
    }

    @Override
    public void pause()
    {
        Log.v(TAG, "Interrupt");
        pause = true;
        thread.interrupt();
    }

    @Override
    public void resume()
    {
        Log.v(TAG, "Resume");
        pause = false;
        if (!thread.isAlive())
            thread.start();
        else
            thread.interrupt();
    }

    public static Service getRenderingControlService()
    {
        if (Homescreen.upnpServiceController.getSelectedRenderer() == null)
            return null;

        return ((CDevice) Homescreen.upnpServiceController.getSelectedRenderer()).getDevice().findService(
                new UDAServiceType("RenderingControl"));
    }

    public static Service getAVTransportService()
    {
        if (Homescreen.upnpServiceController.getSelectedRenderer() == null)
            return null;

        return ((CDevice) Homescreen.upnpServiceController.getSelectedRenderer()).getDevice().findService(
                new UDAServiceType("AVTransport"));
    }

    @Override
    public void commandPlay()
    {
        if (getAVTransportService() == null)
            return;

        controlPoint.execute(new Play(getAVTransportService()) {
            @Override
            public void success(ActionInvocation invocation)
            {
                Log.v(TAG, "Success playing ! ");
                // TODO update player state
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
            {
                Log.w(TAG, "Fail to play ! " + arg2);
            }
        });
    }

    @Override
    public void commandStop()
    {
        if (getAVTransportService() == null)
            return;

        controlPoint.execute(new Stop(getAVTransportService()) {
            @Override
            public void success(ActionInvocation invocation)
            {
                Log.v(TAG, "Success stopping ! ");
                // TODO update player state
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
            {
                Log.w(TAG, "Fail to stop ! " + arg2);
            }
        });
    }

    @Override
    public void commandPause()
    {
        if (getAVTransportService() == null)
            return;

        controlPoint.execute(new Pause(getAVTransportService()) {
            @Override
            public void success(ActionInvocation invocation)
            {
                Log.v(TAG, "Success pausing ! ");
                // TODO update player state
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
            {
                Log.w(TAG, "Fail to pause ! " + arg2);
            }
        });
    }

    @Override
    public void commandToggle()
    {
        RendererState.State state = rendererState.getState();
        if (state == RendererState.State.PLAY)
        {
            commandPause();
        }
        else
        {
            commandPlay();
        }
    }

    @Override
    public void commandSeek(String relativeTimeTarget)
    {
        if (getAVTransportService() == null)
            return;

        controlPoint.execute(new Seek(getAVTransportService(), relativeTimeTarget) {
            // TODO fix it, what is relativeTimeTarget ? :)

            @Override
            public void success(ActionInvocation invocation)
            {
                Log.v(TAG, "Success seeking !");
                // TODO update player state
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
            {
                Log.w(TAG, "Fail to seek ! " + arg2);
            }
        });
    }

    @Override
    public void setVolume(final int volume)
    {
        if (getRenderingControlService() == null)
            return;

        controlPoint.execute(new SetVolume(getRenderingControlService(), volume) {
            @Override
            public void success(ActionInvocation invocation)
            {
                super.success(invocation);
                Log.v(TAG, "Success to set volume");
                rendererState.setVolume(volume);
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
            {
                Log.w(TAG, "Fail to set volume ! " + arg2);
            }
        });
    }

    @Override
    public void setMute(final boolean mute)
    {
        if (getRenderingControlService() == null)
            return;

        controlPoint.execute(new SetMute(getRenderingControlService(), mute) {
            @Override
            public void success(ActionInvocation invocation)
            {
                Log.v(TAG, "Success setting mute status ! ");
                rendererState.setMute(mute);
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
            {
                Log.w(TAG, "Fail to set mute status ! " + arg2);
            }
        });
    }

    @Override
    public void toggleMute()
    {
        setMute(!rendererState.isMute());
    }

    public void setURI(String uri, TrackMetadata trackMetadata)
    {
        Log.i(TAG, "Set uri to " + uri);

        controlPoint.execute(new SetAVTransportURI(getAVTransportService(), uri, trackMetadata.getXML()) {

            @Override
            public void success(ActionInvocation invocation)
            {
                super.success(invocation);
                Log.i(TAG, "URI successfully set !");
                commandPlay();
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
            {
                Log.w(TAG, "Fail to set URI ! " + arg2);
            }
        });
    }

    @Override
    public void launchItem(final IDIDLItem item)
    {
        if (getAVTransportService() == null)
            return;

        DIDLObject obj = ((ClingDIDLItem) item).getObject();
        if (!(obj instanceof Item))
            return;

        Item upnpItem = (Item) obj;

        String type = "";
        if (upnpItem instanceof AudioItem)
            type = "audioItem";
        else if (upnpItem instanceof VideoItem)
            type = "videoItem";
        else if (upnpItem instanceof ImageItem)
            type = "imageItem";
        else if (upnpItem instanceof PlaylistItem)
            type = "playlistItem";
        else if (upnpItem instanceof TextItem)
            type = "textItem";

        // TODO genre && artURI
        final TrackMetadata trackMetadata = new TrackMetadata(upnpItem.getId(), upnpItem.getTitle(),
                upnpItem.getCreator(), "", "", upnpItem.getFirstResource().getValue(),
                "object.item." + type);

        Log.i(TAG, "TrackMetadata : "+trackMetadata.toString());

        // Stop playback before setting URI
        controlPoint.execute(new Stop(getAVTransportService()) {
            @Override
            public void success(ActionInvocation invocation)
            {
                Log.v(TAG, "Success stopping ! ");
                callback();
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
            {
                Log.w(TAG, "Fail to stop ! " + arg2);
                callback();
            }

            public void callback()
            {
                setURI(item.getURI(), trackMetadata);
            }
        });

    }

    // Update

    public void updateMediaInfo()
    {
        if (getAVTransportService() == null)
            return;

        controlPoint.execute(new GetMediaInfo(getAVTransportService()) {
            @Override
            public void received(ActionInvocation arg0, MediaInfo arg1)
            {
                Log.d(TAG, "Receive media info ! " + arg1);
                rendererState.setMediaInfo(arg1);
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
            {
                Log.w(TAG, "Fail to get media info ! " + arg2);
            }
        });
    }

    public void updatePositionInfo()
    {
        if (getAVTransportService() == null)
            return;

        controlPoint.execute(new GetPositionInfo(getAVTransportService()) {
            @Override
            public void received(ActionInvocation arg0, PositionInfo arg1)
            {
                Log.d(TAG, "Receive position info ! " + arg1);
                rendererState.setPositionInfo(arg1);
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
            {
                Log.w(TAG, "Fail to get position info ! " + arg2);
            }
        });
    }

    public void updateTransportInfo()
    {
        if (getAVTransportService() == null)
            return;

        controlPoint.execute(new GetTransportInfo(getAVTransportService()) {
            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
            {
                Log.w(TAG, "Fail to get position info ! " + arg2);
            }

            @Override
            public void received(ActionInvocation arg0, TransportInfo arg1)
            {
                Log.d(TAG, "Receive position info ! " + arg1);
                rendererState.setTransportInfo(arg1);
            }
        });
    }

    @Override
    public void updateVolume()
    {
        if (getRenderingControlService() == null)
            return;

        controlPoint.execute(new GetVolume(getRenderingControlService()) {
            @Override
            public void received(ActionInvocation arg0, int arg1)
            {
                Log.d(TAG, "Receive volume ! " + arg1);
                rendererState.setVolume(arg1);
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
            {
                Log.w(TAG, "Fail to get volume ! " + arg2);
            }
        });
    }

    public void updateMute()
    {
        if (getRenderingControlService() == null)
            return;

        controlPoint.execute(new GetMute(getRenderingControlService()) {
            @Override
            public void received(ActionInvocation arg0, boolean arg1)
            {
                Log.d(TAG, "Receive mute status ! " + arg1);
                rendererState.setMute(arg1);
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2)
            {
                Log.w(TAG, "Fail to get mute status ! " + arg2);
            }
        });
    }

    @Override
    public void updateFull()
    {
        updateMediaInfo();
        updatePositionInfo();
        updateVolume();
        updateMute();
        updateTransportInfo();
    }

    @Override
    public void run()
    {

        while (true)
            try
            {
                int count = 0;
                while (true)
                {
                    if (!pause)
                    {
                        Log.d(TAG, "Update state !");

                        count++;

                        updatePositionInfo();



                        if ((count % 3) == 0)
                        {
                            updateVolume();
                            updateMute();
                            updateTransportInfo();

                            if (rendererState.getState() == RendererState.State.STOP && !Homescreen.localPlayback && !MediaButtons.pause) {
                                Log.d(TAG, "Changing status");

                                if (QueueSongs.getInstance().size() > 0) {
                                    if (Homescreen.upnpServiceController.getServiceListener().getMediaServer().getSongId() == QueueSongs.getInstance().get(0).Id) {
                                        // If adapter is initialized then remove from queue listview
                                        if (Queue.adapter != null)
                                            Queue.adapter.remove(QueueSongs.getInstance().get(0));
                                        QueueSongs.getInstance().remove(0);
                                        MediaButtons.pause = true;
                                    }

                                }
                            }
                        }

                        if ((count % 6) == 0)
                        {
                            //long songid = Homescreen.upnpServiceController.getServiceListener().getMediaServer().getSongId();
                            //Log.v(TAG, "server song id: " + String.valueOf(songid));
                            updateMediaInfo();
                        }
                    }
                    Thread.sleep(1000);
                }
            }
            catch (InterruptedException e)
            {
                Log.i(TAG, "State updater interrupt, new state " + ((pause) ? "pause" : "running"));
            }
    }

    @Override
    public void updateStatus()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void updatePosition()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void prepareNextSong(boolean forceStartAgain)
    {
        if (QueueSongs.getInstance().size() > 0) {
            Log.d("METHOD: ", "prepareNextSong called");
            // set mediabuttons states
            MediaButtons.pause = false;
            //mediaButtons.invalidate();

            int position = 0;

            long currentSongId = QueueSongs.getInstance().get(position).Id;

            Log.v("RendererCommand: ", "Comparing items: " + String.valueOf(currentSongId) + " : " + String.valueOf(Homescreen.upnpServiceController.getServiceListener().getMediaServer().getSongId()));
            // check if selected song is already served
            if (Homescreen.upnpServiceController.getServiceListener().getMediaServer().getSongId() == currentSongId && !forceStartAgain){
                Log.v("RendererCommand: ", "Toggling command");
                commandToggle();
            }
            else {
                try {
                    ClingDIDLItem cling_item = new ClingDIDLItem(AllSongs.getInstance().get(position).musicTrack);
                    Homescreen.rendererCommand.launchItem(cling_item);
                    Log.v("RendererCommand: ", "Served new item");
                    rendererState.setState(RendererState.State.PLAY);
                } catch (Exception e) {
                    Log.e("RendererCommand: ", "Error slaunching item", e);
                    // should skip to next and continue queue
                }
            }


        }

    }

    public void skipToNextSong(){
        completeSong();
        /*
        try {
            if (QueueSongs.getInstance().size() > 1){
                // if player was playing before skip then automatically continue
                boolean is_playing = false;
                RendererState.State state = rendererState.getState();
                if (state == RendererState.State.PLAY){
                    is_playing = true;
                    //commandPause();
                    stopPlaying();
                    Log.v("RendererCommand: ", "Stopped");
                }

                // If shuffle is on then swap random song from queue to index 0
                if (MediaButtons.shuffle && QueueSongs.getInstance().size() > 1){
                    Log.v("RendererCommand: ", "Shuffling");
                    shuffleSong();
                }

                if (is_playing) {
                    Log.v("RendererCommand: ", "Preparing next song");
                    prepareNextSong(true);
                }
            }
            else if (QueueSongs.getInstance().size() == 1) {
                Log.v("RendererCommand: ", "Stopping when only song");
                stopPlaying();
                //CustomUtilities.showToast(this, "No songs in queue");
            }
            else if (QueueSongs.getInstance().size() == 0){
                try {Log.v("RendererCommand: ", "Stopping when no songs");
                    stopPlaying();
                } catch (Exception e){
                    e.printStackTrace();
                }
                //CustomUtilities.showToast(Homescreen.getContext(), "No songs in queue");
            }

            //Set song info
            //setSongInfo();
            //updateProgressBar();
        } catch (Exception e) {
            Log.e("MUSIC SERVICE: ", "Error skipping song", e);
        }*/
    }
    /*
    public void toPreviousSong() {
        try {
            //Log.d("PLAYER POSITION: ", isPrepared + " " + QueueSongs.getInstance().size() + " " + songHistory.size());
            boolean isPrepared = false;
            if (Homescreen.upnpServiceController.getServiceListener().getMediaServer().getSongId() != -1) {
                isPrepared = true;
            }
            if (QueueSongs.getInstance().size() == 0 && songHistory.size() == 0){
                //CustomUtilities.showToast(this, "No previously played songs");
                Log.d(TAG, "No previously played songs");
            }
            else if (QueueSongs.getInstance().size() == 0 && songHistory.size() > 0){
                // Continue automatically if player was playing before
                Boolean wasPLaying = player.isPlaying();
                QueueSongs.getInstance().add(0, songHistory.get(0));
                songHistory.remove(0);
                if (wasPLaying) playSong();
            }
            else if (QueueSongs.getInstance().size() > 0 && songHistory.size() == 0){
                // same song again
                //rendererState.getElapsedSeconds()
                if (isPrepared && rendererState.getElapsedSeconds() > 1){
                    player.seekTo(0);
                    MediaButtons.progress = 0;
                }
                else {
                    //CustomUtilities.showToast(this, "No previously played songs");
                    Log.d(TAG, "No previously played songs");
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
    */
    private void shuffleSong(){
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

    private void stopPlaying(){
        try {
            MediaButtons.pause = true;
            RendererState.State state = rendererState.getState();
            if (state == RendererState.State.PLAY){
                commandPause();
            }
            if (QueueSongs.getInstance().size() > 0) {
                // Add to song history
                songHistory.add(0, QueueSongs.getInstance().get(0));
                // Remove song from QueueSongs
                QueueSongs.getInstance().remove(0);
            }

        } catch (Exception e) {
            Log.e("MUSIC SERVICE: ", "Error stopping song", e);
        }
    }

    private void completeSong(){
        boolean isPrepared = false;
        if (Homescreen.upnpServiceController.getServiceListener().getMediaServer().getSongId() != -1) {
            isPrepared = true;
        }
        if (!MediaButtons.repeat) {
            Log.d("MUSIC SERVICE: ", "Song completed");

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
                if (!MediaButtons.pause) prepareNextSong(true);
            } else {
                MediaButtons.pause = true;
                //mediaButtons.invalidate();
                //CustomUtilities.showToast(this, "No songs in queue");
            }
        }
        else {
            Log.d("MUSIC SERVICE: ", "Player is looping");
            isPrepared = false;
            // Play song again if not paused
            if (!MediaButtons.pause) prepareNextSong(true);
        }

    }
}