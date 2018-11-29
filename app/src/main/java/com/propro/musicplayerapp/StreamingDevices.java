package com.propro.musicplayerapp;

import java.util.ArrayList;

public class StreamingDevices extends ArrayList<DeviceInfo> {

    private static StreamingDevices sSoleInstance;

    // private constructor.
    private StreamingDevices(){
        //Prevent form the reflection api.
        if (sSoleInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
        /*
        add(new DeviceInfo("Computer-X"));
        add(new DeviceInfo("Speakers"));
        add(new DeviceInfo("Samsung TV"));
        */
    }

    // Thread safe implementation
    public synchronized static StreamingDevices getInstance(){
        if (sSoleInstance == null){ //if there is no instance available... create new one
            sSoleInstance = new StreamingDevices();
        }

        return sSoleInstance;
    }
}
