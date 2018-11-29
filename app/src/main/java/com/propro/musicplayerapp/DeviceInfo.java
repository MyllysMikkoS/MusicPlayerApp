package com.propro.musicplayerapp;

import com.propro.musicplayerapp.upnp.IUpnpDevice;

public class DeviceInfo {
    /*
    // original implementation
    public String Name;

    public DeviceInfo(String name){this.Name = name;}
    */
    private final IUpnpDevice device;
    private final boolean extendedInformation;

    public DeviceInfo(IUpnpDevice device, boolean extendedInformation)
    {
        this.device = device;
        this.extendedInformation = extendedInformation;
    }

    public DeviceInfo(IUpnpDevice device)
    {
        this(device, false);
    }

    public IUpnpDevice getDevice()
    {
        return device;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DeviceInfo that = (DeviceInfo) o;
        return device.equals(that.device);
    }

    @Override
    public int hashCode()
    {
        if (device == null)
            return 0;

        return device.hashCode();
    }

    @Override
    public String toString()
    {
        if (device == null)
            return "";

        String name = getDevice().getFriendlyName();

        if (extendedInformation)
            name += getDevice().getExtendedInformation();

        return name;
    }

}
