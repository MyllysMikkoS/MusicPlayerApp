package com.propro.musicplayerapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class StreamAdapter extends ArrayAdapter<DeviceInfo> {

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public StreamAdapter(Context context, ArrayList<DeviceInfo> devices) {
        super(context, 0, devices);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        DeviceInfo device = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.streaming_devices_item, parent, false);
        }

        // Lookup view for data population
        TextView tvDevice = (TextView) convertView.findViewById(R.id.deviceNameTextView);
        ImageView ivConnect = (ImageView) convertView.findViewById(R.id.connectDeviceImage);
        ivConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set delete-event
                // select(list.getItem(position).getDevice());
                StreamingDevices streamingdevice = StreamingDevices.getInstance();
                //streamingdevice.add()
                boolean force = false;
                Homescreen.upnpServiceController.setSelectedRenderer(Stream.devices.get(position).getDevice(), force);
                Homescreen.localPlayback = false;
                Log.d("Connect: ", "clicked " + position);
                Log.d("Connected device: ", Stream.devices.get(position).toString());

            }
        });

        // Populate the data into the template view using the data object
        try {
            tvDevice.setText(device.toString());
        } catch (NullPointerException e) {
            Log.d("Error: ", e.toString());
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
