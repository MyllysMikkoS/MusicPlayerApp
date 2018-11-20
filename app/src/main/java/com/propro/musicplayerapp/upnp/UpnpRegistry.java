package com.propro.musicplayerapp.upnp;

import java.util.ArrayList;
import java.util.Collection;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.registry.RegistryImpl;

@SuppressWarnings("rawtypes")
public class UpnpRegistry implements IUpnpRegistry {

    RegistryImpl clingRegistry;

    @Override
    public Collection<IUpnpDevice> getDevicesList()
    {
        Collection<IUpnpDevice> devices = new ArrayList<IUpnpDevice>();
        for (Device d : clingRegistry.getDevices())
            devices.add(new CDevice(d));

        return devices;
    }

    @Override
    public void addListener(IRegistryListener r)
    {
        clingRegistry.addListener((CRegistryListener) r);
    }

    @Override
    public void removeListener(IRegistryListener r)
    {
        clingRegistry.removeListener((CRegistryListener) r);
    }

}
