package com.propro.musicplayerapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MusicService extends Service {

    private static MusicService sSoleInstance;

    public MusicService() {
        //Prevent form the reflection api.
        if (sSoleInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    // Thread safe implementation
    public synchronized static MusicService getInstance(){
        if (sSoleInstance == null){ //if there is no instance available... create new one
            sSoleInstance = new MusicService();
        }

        return sSoleInstance;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
