package com.propro.musicplayerapp.upnp;

import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

public class CRegistryListener extends DefaultRegistryListener {

    private final IRegistryListener registryListener;

    public CRegistryListener(IRegistryListener registryListener)
    {
        this.registryListener = registryListener;
    }

    /* Discovery performance optimization for very slow Android devices! */
    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device)
    {
        registryListener.deviceAdded(new CDevice(device));
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex)
    {
        registryListener.deviceRemoved(new CDevice(device));
    }

    /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device)
    {
        registryListener.deviceAdded(new CDevice(device));
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device)
    {
        registryListener.deviceRemoved(new CDevice(device));
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device)
    {
        registryListener.deviceAdded(new CDevice(device));
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device)
    {
        registryListener.deviceRemoved(new CDevice(device));
    }
}
