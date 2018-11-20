package com.propro.musicplayerapp.upnp;

public interface IUpnpDevice {
    public String getDisplayString();

    public String getFriendlyName();

    public String getExtendedInformation();

    public String getManufacturer();

    public String getManufacturerURL();

    public String getModelName();

    public String getModelDesc();

    public String getModelNumber();

    public String getModelURL();

    public String getXMLURL();

    public String getPresentationURL();

    public String getSerialNumber();

    public String getUDN();

    public boolean equals(IUpnpDevice otherDevice);

    public String getUID();

    public boolean asService(String service);

    public void printService();

    public boolean isFullyHydrated();

    @Override
    public String toString();
}
