package com.propro.musicplayerapp.upnp;

import android.content.Intent;
import android.util.Log;

import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;


public class UpnpService extends AndroidUpnpServiceImpl{
    @Override
    protected AndroidUpnpServiceConfiguration createConfiguration()
    {
        return new AndroidUpnpServiceConfiguration() {

            @Override
            public int getRegistryMaintenanceIntervalMillis()
            {
                return 7000;
            }

        };
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.d(this.getClass().getName(), "Unbind");
        return super.onUnbind(intent);
    }
}
