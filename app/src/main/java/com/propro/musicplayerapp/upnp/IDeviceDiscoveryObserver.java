package com.propro.musicplayerapp.upnp;

public interface IDeviceDiscoveryObserver {

    public void addedDevice(IUpnpDevice device);
    public void removedDevice(IUpnpDevice device);
}
