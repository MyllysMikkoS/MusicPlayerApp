package com.propro.musicplayerapp.upnp;

import java.util.ArrayList;
import java.util.Collection;

import android.util.Log;

public abstract class DeviceDiscovery {

    protected static final String TAG = "DeviceDiscovery";

    private final BrowsingRegistryListener browsingRegistryListener;

    protected boolean extendedInformation;

    private final ArrayList<IDeviceDiscoveryObserver> observerList;

    public DeviceDiscovery(IServiceListener serviceListener, boolean extendedInformation)
    {
        browsingRegistryListener = new BrowsingRegistryListener();
        this.extendedInformation = extendedInformation;
        observerList = new ArrayList<IDeviceDiscoveryObserver>();
    }

    public DeviceDiscovery(IServiceListener serviceListener)
    {
        this(serviceListener, false);
    }

    public void resume(IServiceListener serviceListener)
    {
        serviceListener.addListener(browsingRegistryListener);
    }

    public void pause(IServiceListener serviceListener)
    {
        serviceListener.removeListener(browsingRegistryListener);
    }

    public class BrowsingRegistryListener implements IRegistryListener {

        @Override
        public void deviceAdded(final IUpnpDevice device)
        {
            Log.v(TAG, "New device detected : " + device.getDisplayString());

            if (device.isFullyHydrated() && filter(device))
            {
                if (isSelected(device))
                {
                    Log.i(TAG, "Reselect device to refresh it");
                    select(device, true);
                }

                notifyAdded(device);
            }
        }

        @Override
        public void deviceRemoved(final IUpnpDevice device)
        {
            Log.v(TAG, "Device removed : " + device.getFriendlyName());

            if (filter(device))
            {
                if (isSelected(device))
                {
                    Log.i(TAG, "Selected device have been removed");
                    removed(device);
                }

                notifyRemoved(device);
            }
        }
    }

    public void addObserver(IDeviceDiscoveryObserver o)
    {
        observerList.add(o);

        final Collection<IUpnpDevice> upnpDevices = Main.upnpServiceController.getServiceListener()
                .getFilteredDeviceList(getCallableFilter());
        for (IUpnpDevice d : upnpDevices)
            o.addedDevice(d);
    }

    public void removeObserver(IDeviceDiscoveryObserver o)
    {
        observerList.remove(o);
    }

    public void notifyAdded(IUpnpDevice device)
    {
        for (IDeviceDiscoveryObserver o : observerList)
            o.addedDevice(device);
    }

    public void notifyRemoved(IUpnpDevice device)
    {
        for (IDeviceDiscoveryObserver o : observerList)
            o.removedDevice(device);
    }

    /**
     * Filter device you want to add to this device list fragment
     *
     * @param device
     *            the device to test
     * @return add it or not
     * @throws Exception
     */
    protected boolean filter(IUpnpDevice device)
    {
        ICallableFilter filter = getCallableFilter();
        filter.setDevice(device);
        try
        {
            return filter.call();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get a callable device filter
     *
     * @return
     */
    protected abstract ICallableFilter getCallableFilter();

    /**
     * Filter to know if device is selected
     *
     * @param d
     * @return
     */
    protected abstract boolean isSelected(IUpnpDevice d);

    /**
     * Select a device
     *
     * @param device
     */
    protected abstract void select(IUpnpDevice device);

    /**
     * Select a device
     *
     * @param device
     * @param force
     */
    protected abstract void select(IUpnpDevice device, boolean force);

    /**
     * Callback when device removed
     *
     * @param d
     */
    protected abstract void removed(IUpnpDevice d);
}
