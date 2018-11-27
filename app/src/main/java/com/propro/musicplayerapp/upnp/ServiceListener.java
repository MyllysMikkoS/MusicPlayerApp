package com.propro.musicplayerapp.upnp;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

@SuppressWarnings("rawtypes")
public class ServiceListener implements IServiceListener
{
    private static final String TAG = "Cling.ServiceListener";

    protected AndroidUpnpService upnpService;
    protected ArrayList<IRegistryListener> waitingListener;

    private MediaServer mediaServer = null;
    private Context ctx = null;

    public ServiceListener(Context ctx)
    {
        waitingListener = new ArrayList<IRegistryListener>();
        this.ctx = ctx;
    }

    @Override
    public void refresh()
    {
        upnpService.getControlPoint().search();
    }

    @Override
    public Collection<IUpnpDevice> getDeviceList()
    {
        ArrayList<IUpnpDevice> deviceList = new ArrayList<IUpnpDevice>();
        if(upnpService != null && upnpService.getRegistry() != null) {
            for (Device device : upnpService.getRegistry().getDevices()) {
                deviceList.add(new CDevice(device));
            }
        }
        return deviceList;
    }

    @Override
    public Collection<IUpnpDevice> getFilteredDeviceList(ICallableFilter filter)
    {
        ArrayList<IUpnpDevice> deviceList = new ArrayList<IUpnpDevice>();
        try
        {
            if(upnpService != null && upnpService.getRegistry() != null) {
                for (Device device : upnpService.getRegistry().getDevices()) {
                    IUpnpDevice upnpDevice = new CDevice(device);
                    filter.setDevice(upnpDevice);

                    if (filter.call())
                        deviceList.add(upnpDevice);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return deviceList;
    }
    // OWN COMMENT

    protected ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            Log.i(TAG, "Service connexion");
            upnpService = (AndroidUpnpService) service;

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
            /*
            if(sharedPref.getBoolean(SettingsActivity.CONTENTDIRECTORY_SERVICE, true))
            {
                try
                {
                    // Local content directory
                    if(mediaServer == null)
                    {
                        mediaServer = new MediaServer(Main.getLocalIpAddress(ctx), ctx);
                        mediaServer.start();
                    }
                    else
                    {
                        mediaServer.restart();
                    }
                    upnpService.getRegistry().addDevice(mediaServer.getDevice());
                }
                catch (UnknownHostException e1)
                {
                    Log.e(TAG, "Creating demo device failed");
                    Log.e(TAG, "exception", e1);
                }
                catch (ValidationException e2)
                {
                    Log.e(TAG, "Creating demo device failed");
                    Log.e(TAG, "exception", e2);
                }
                catch (IOException e3)
                {
                    Log.e(TAG, "Starting http server failed");
                    Log.e(TAG, "exception", e3);
                }
            }
            else if(mediaServer != null)
            {
                mediaServer.stop();
                mediaServer = null;
            }
            */

            for (IRegistryListener registryListener : waitingListener)
            {
                addListenerSafe(registryListener);
            }

            // Search asynchronously for all devices, they will respond soon
            upnpService.getControlPoint().search();
        }

        @Override
        public void onServiceDisconnected(ComponentName className)
        {
            Log.i(TAG, "Service disconnected");
            upnpService = null;
        }
    };


    @Override
    public ServiceConnection getServiceConnexion()
    {
        return serviceConnection;
    }

    public AndroidUpnpService getUpnpService()
    {
        return upnpService;
    }

    @Override
    public void addListener(IRegistryListener registryListener)
    {
        Log.d(TAG, "Add Listener !");
        if (upnpService != null)
            addListenerSafe(registryListener);
        else
            waitingListener.add(registryListener);
    }

    private void addListenerSafe(IRegistryListener registryListener)
    {
        assert upnpService != null;
        Log.d(TAG, "Add Listener Safe !");

        // Get ready for future device advertisements
        upnpService.getRegistry().addListener(new CRegistryListener(registryListener));

        // Now add all devices to the list we already know about
        for (Device device : upnpService.getRegistry().getDevices())
        {
            registryListener.deviceAdded(new CDevice(device));
        }
    }

    @Override
    public void removeListener(IRegistryListener registryListener)
    {
        Log.d(TAG, "remove listener");
        if (upnpService != null)
            removeListenerSafe(registryListener);
        else
            waitingListener.remove(registryListener);
    }

    private void removeListenerSafe(IRegistryListener registryListener)
    {
        assert upnpService != null;
        Log.d(TAG, "remove listener Safe");
        upnpService.getRegistry().removeListener(new CRegistryListener(registryListener));
    }

    @Override
    public void clearListener()
    {
        waitingListener.clear();
    }
}
