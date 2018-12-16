package com.propro.musicplayerapp.upnp;

import java.util.Collection;

import android.content.ServiceConnection;

public interface IServiceListener {

    public void addListener(IRegistryListener registryListener);

    public void removeListener(IRegistryListener registryListener);

    public void clearListener();

    public void refresh();

    public Collection<IUpnpDevice> getDeviceList();

    public Collection<IUpnpDevice> getFilteredDeviceList(ICallableFilter filter);

    public ServiceConnection getServiceConnexion();

    MediaServer getMediaServer();
}
