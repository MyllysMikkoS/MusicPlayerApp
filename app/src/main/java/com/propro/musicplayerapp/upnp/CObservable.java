package com.propro.musicplayerapp.upnp;

import java.util.Observable;

public class CObservable extends Observable {

    public void notifyAllObservers()
    {
        setChanged();
        notifyObservers();
    }
}
