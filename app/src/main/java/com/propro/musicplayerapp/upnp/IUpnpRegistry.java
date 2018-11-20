package com.propro.musicplayerapp.upnp;

import java.util.Collection;

public interface IUpnpRegistry {
    public Collection<IUpnpDevice> getDevicesList();

    public void addListener(IRegistryListener r);

    public void removeListener(IRegistryListener r);
}
