package com.propro.musicplayerapp.upnp;

import org.fourthline.cling.model.meta.LocalDevice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceController extends UpnpServiceController
{
    private static final String TAG = "Cling.ServiceController";

    private final ServiceListener upnpServiceListener;
    private Activity activity = null;

    public ServiceController(Context ctx)
    {
        super();
        upnpServiceListener = new ServiceListener(ctx);
    }

    @Override
    protected void finalize()
    {
        pause();
    }

    @Override
    public ServiceListener getServiceListener()
    {
        return upnpServiceListener;
    }

    @Override
    public void pause()
    {
        super.pause();
        activity.unbindService(upnpServiceListener.getServiceConnexion());
        activity = null;
    }

    @Override
    public void resume(Activity activity)
    {
        super.resume(activity);
        this.activity = activity;

        // This will start the UPnP service if it wasn't already started
        Log.d(TAG, "Start upnp service");
        activity.bindService(new Intent(activity, UpnpService.class), upnpServiceListener.getServiceConnexion(),
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void addDevice(LocalDevice localDevice) {
        upnpServiceListener.getUpnpService().getRegistry().addDevice(localDevice);
    }

    @Override
    public void removeDevice(LocalDevice localDevice) {
        upnpServiceListener.getUpnpService().getRegistry().removeDevice(localDevice);
    }

}
