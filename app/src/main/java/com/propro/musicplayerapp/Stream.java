package com.propro.musicplayerapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.propro.musicplayerapp.Homescreen;
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
        /*
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //if (adapter.getCount() > 0) {
                    //adapter.remove(queueSongs.get(position));
                    //QueueSongs.getInstance().remove(position);
                //}
                Log.v("STREAM: ", "i: " + devices.size());
            }
        });*/

        //------renderer observable test
        // Listen to renderer change
        if (Homescreen.upnpServiceController != null) {
            Homescreen.upnpServiceController.addSelectedRendererObserver(this);
            Homescreen.upnpServiceController.getRendererDiscovery().addObserver(this);
            Log.d("Stream", "upnpServiceController add Renreder Observer !!!");
        }
        else
            Log.w("Stream", "upnpServiceController was not ready !!!");
        //---------
        /*
        devices = new ArrayList<DeviceInfo>();
        adapter = new StreamAdapter(this, devices);
        devicesListView.setAdapter(adapter);
        // TESTING --

        StreamingDevices streamingDevices = StreamingDevices.getInstance();
        for (DeviceInfo deviceInfo : streamingDevices) {
            adapter.add(deviceInfo);
        }
        // TESTING --

        devicesListView.setAdapter(adapter);
        */
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
        switch (item.getItemId()) {

            case R.id.action_refresh:
                Log.d("Refresh: ", "CALLED");
                return true;
        }

        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void update(Observable observable, Object data)
    {
        Log.v("Stream", "Update called");
        //startControlPoint();
        IUpnpDevice device = Homescreen.upnpServiceController.getSelectedRenderer();
        if (device == null)
        {
            // Uncheck device
            //getListView().clearChoices();
            //devices.notifyDataSetChanged();
            //getViewModelStore().clear();
            //devices.notifyDataSetChanged();
            //addedDevice(device);
            Log.v("Stream", "IF LOOP");
        }
        else
        {
            addedDevice(device);
        }
    }

    public void startControlPoint()
    {
        Log.e("startControlPoint", "Called start controlpoint point in stream activity");
        /*
        if (Main.upnpServiceController.getSelectedRenderer() == null)
        {
            if (device != null)
            {
                Log.i(TAG, "Current renderer have been removed");
                device = null;

                final Activity a = getActivity();
                if (a == null)
                    return;

                a.runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        try {
                            hide();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            return;
        }
        */

        /*
        if (device == null || rendererState == null || rendererCommand == null
                || !device.equals(Main.upnpServiceController.getSelectedRenderer()))
        {
            device = Main.upnpServiceController.getSelectedRenderer();

            Log.i(TAG, "Renderer changed !!! " + Main.upnpServiceController.getSelectedRenderer().getDisplayString());

            rendererState = Main.factory.createRendererState();
            rendererCommand = Main.factory.createRendererCommand(rendererState);

            if (rendererState == null || rendererCommand == null)
            {
                Log.e(TAG, "Fail to create renderer command and/or state");
                return;
            }

            rendererCommand.resume();

            rendererState.addObserver(this);
            rendererCommand.updateFull();
        }
        updateRenderer();
        */
    }

    @Override
    public void removedDevice(IUpnpDevice device)
    {
        Log.v("Stream", "Device removed : " + device.getFriendlyName());


        /*
        final DeviceDisplay d = new DeviceDisplay(device, extendedInformation);

        if (getActivity() != null) // Visible
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    try {
                        // Remove device from list
                        list.remove(d);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });*/
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
            //devicesListView.setAdapter(adapter);
            Log.v("Stream", "POSITION" + String.valueOf(position));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //adapter.add(d);
        /*

        final DeviceDisplay d = new DeviceDisplay(device, extendedInformation);

        if (getActivity() != null) // Visible
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    try {
                        int position = list.getPosition(d);
                        if (position >= 0)
                        {
                            // Device already in the list, re-set new value at same position
                            list.remove(d);
                            list.insert(d, position);
                        }
                        else
                        {
                            list.add(d);
                        }
                        if (isSelected(d.getDevice()))
                        {
                            position = list.getPosition(d);
                            getListView().setItemChecked(position, true);

                            Log.i(TAG, d.toString() + " is selected at position " + position);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        */
    }


}
