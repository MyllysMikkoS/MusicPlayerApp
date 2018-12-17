package com.propro.musicplayerapp.upnp;

import android.app.Activity;
import android.util.Log;

import java.util.Observer;

public abstract class UpnpServiceController implements IUpnpServiceController {

    private static final String TAG = "UpnpServiceController";

    protected IUpnpDevice renderer;
    protected IUpnpDevice contentDirectory;

    protected CObservable rendererObservable;
    protected CObservable contentDirectoryObservable;

    private final RendererDiscovery rendererDiscovery;

    @Override
    public RendererDiscovery getRendererDiscovery()
    {
        return rendererDiscovery;
    }

    protected UpnpServiceController()
    {
        rendererObservable = new CObservable();
        rendererDiscovery = new RendererDiscovery(getServiceListener());
    }

    @Override
    public void setSelectedRenderer(IUpnpDevice renderer)
    {
        setSelectedRenderer(renderer, false);
    }

    @Override
    public void setSelectedRenderer(IUpnpDevice renderer, boolean force)
    {
        // Skip if no change and no force
        if (!force && renderer != null && this.renderer != null && this.renderer.equals(renderer))
            return;

        this.renderer = renderer;
        rendererObservable.notifyAllObservers();
    }

    @Override
    public IUpnpDevice getSelectedRenderer()
    {
        return renderer;
    }

    @Override
    public void addSelectedRendererObserver(Observer o)
    {
        Log.i(TAG, "New SelectedRendererObserver");
        rendererObservable.addObserver(o);
    }

    @Override
    public void delSelectedRendererObserver(Observer o)
    {
        rendererObservable.deleteObserver(o);
    }

    // Pause the service
    @Override
    public void pause()
    {
        rendererDiscovery.pause(getServiceListener());
    }

    // Resume the service
    @Override
    public void resume(Activity activity)
    {
        rendererDiscovery.resume(getServiceListener());
    }

}
