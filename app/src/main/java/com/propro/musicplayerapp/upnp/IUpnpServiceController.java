package com.propro.musicplayerapp.upnp;

import java.util.Observer;

import org.fourthline.cling.model.meta.LocalDevice;

import android.app.Activity;

public interface IUpnpServiceController {
    public void setSelectedRenderer(IUpnpDevice renderer);

    public void setSelectedRenderer(IUpnpDevice renderer, boolean force);

    public void setSelectedContentDirectory(IUpnpDevice contentDirectory);

    public void setSelectedContentDirectory(IUpnpDevice contentDirectory, boolean force);

    public IUpnpDevice getSelectedRenderer();

    public IUpnpDevice getSelectedContentDirectory();

    public void addSelectedRendererObserver(Observer o);

    public void delSelectedRendererObserver(Observer o);

    public void addSelectedContentDirectoryObserver(Observer o);

    public void delSelectedContentDirectoryObserver(Observer o);

    public IServiceListener getServiceListener();

    //public ContentDirectoryDiscovery getContentDirectoryDiscovery();

    public RendererDiscovery getRendererDiscovery();

    // Pause the service
    public void pause();

    // Resume the service
    public void resume(Activity activity);

    public void addDevice(LocalDevice localDevice);
    public void removeDevice(LocalDevice localDevice);

}
