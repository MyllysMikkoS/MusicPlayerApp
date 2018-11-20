package com.propro.musicplayerapp.upnp;

public interface IRegistryListener {
    public void deviceAdded(final IUpnpDevice device);

    public void deviceRemoved(final IUpnpDevice device);
}
