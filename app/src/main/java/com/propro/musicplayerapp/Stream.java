package com.propro.musicplayerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.propro.musicplayerapp.upnp.IDeviceDiscoveryObserver;
import com.propro.musicplayerapp.upnp.IUpnpDevice;

public class Stream extends AppCompatActivity implements Observer, IDeviceDiscoveryObserver {

    Toolbar toolbar;
    ListView devicesListView;

    private static StreamAdapter adapter;
    public static ArrayList<DeviceInfo> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        this.setTitle("Streaming devices");

        // Init views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        devicesListView = findViewById(R.id.devicesListView);

        devices = new ArrayList<DeviceInfo>();
        adapter = new StreamAdapter(this, devices);
        devicesListView.setAdapter(adapter);

        // Listen to renderer change
        if (Homescreen.upnpServiceController != null) {
            Homescreen.upnpServiceController.addSelectedRendererObserver(this);
            Homescreen.upnpServiceController.getRendererDiscovery().addObserver(this);
            Log.d("Stream", "upnpServiceController add Renreder Observer !!!");
        }
        else
            Log.w("Stream", "upnpServiceController was not ready !!!");

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Homescreen.upnpServiceController.getRendererDiscovery().removeObserver(this);
        Homescreen.upnpServiceController.delSelectedRendererObserver(this);
        Log.v("Stream", "onDestroy");
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.streaming_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void update(Observable observable, Object data)
    {
        Log.v("Stream", "Update called");
        IUpnpDevice device = Homescreen.upnpServiceController.getSelectedRenderer();
        if (device != null)
        {
            addedDevice(device);
        }
    }

    @Override
    public void removedDevice(IUpnpDevice device)
    {
        Log.v("Stream", "Device removed : " + device.getFriendlyName());
    }

    @Override
    public void addedDevice(IUpnpDevice device)
    {
        Log.v("Stream", "New device detected : " + device.getDisplayString());
        final DeviceInfo d = new DeviceInfo(device, false);
        try {
            int position = adapter.getPosition(d);
            if (position >= 0)
            {
                // Device already in the list, re-set new value at same position
                adapter.remove(d);
                adapter.insert(d, position);
            }
            else
            {
                adapter.add(d);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}
