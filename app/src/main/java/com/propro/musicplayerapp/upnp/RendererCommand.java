package com.propro.musicplayerapp.upnp;

/*
import org.droidupnp.Main;
import org.droidupnp.model.cling.CDevice;
import org.droidupnp.model.cling.RendererState;
import org.droidupnp.model.cling.TrackMetadata;
import org.droidupnp.model.cling.didl.ClingDIDLItem;
import org.droidupnp.model.upnp.IRendererCommand;
import org.droidupnp.model.upnp.didl.IDIDLItem;
*/

import com.propro.musicplayerapp.AllSongs;
import com.propro.musicplayerapp.Homescreen;
import com.propro.musicplayerapp.QueueSongs;

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

@SuppressWarnings("rawtypes")
public class RendererCommand implements Runnable, IRendererCommand {

    private static final String TAG = "RendererCommand";

    private final RendererState rendererState;
    private final ControlPoint controlPoint;

    public Thread thread;
    boolean pause = false;

    public RendererCommand(ControlPoint controlPoint, RendererState rendererState)
    {
        this.rendererState = rendererState;
        this.controlPoint = controlPoint;

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
                        //Log.d(TAG, "Update state !");

                        count++;

                        updatePositionInfo();

                        if ((count % 3) == 0)
                        {
                            updateVolume();
                            updateMute();
                            updateTransportInfo();
                        }

                        if ((count % 6) == 0)
                        {
                            long songid = Homescreen.upnpServiceController.getServiceListener().getMediaServer().getSongId();
                            Log.v(TAG, "server song id: " + String.valueOf(songid));
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
    public void prepareNextSong()
    {
        if (QueueSongs.getInstance().size() > 0) {
            Log.d("METHOD: ", "prepareNextSong called");
            // set mediabuttons states
            //MediaButtons.pause = false;
            //mediaButtons.invalidate();
            // reset player
            //player.reset();

            int position = 0;

            long currentSongId = QueueSongs.getInstance().get(position).Id;

            Log.v("RendererCommand: ", "Comparing items: " + String.valueOf(currentSongId) + " : " + String.valueOf(Homescreen.upnpServiceController.getServiceListener().getMediaServer().getSongId()));
            // check if selected song is already served
            if (Homescreen.upnpServiceController.getServiceListener().getMediaServer().getSongId() == currentSongId){
                Log.v("RendererCommand: ", "Toggling command");
                commandToggle();
            }
            else {
                try {
                    ClingDIDLItem cling_item = new ClingDIDLItem(AllSongs.getInstance().get(position).musicTrack);
                    Homescreen.rendererCommand.launchItem(cling_item);
                    Log.v("RendererCommand: ", "Served new item");
                } catch (Exception e) {
                    Log.e("RendererCommand: ", "Error slaunching item", e);
                    // should skip to next and continue queue
                }
            }


        }

    }
}