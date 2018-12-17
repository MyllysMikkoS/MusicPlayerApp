package com.propro.musicplayerapp.upnp;

import android.content.Context;

public interface IFactory {

    public IUpnpServiceController createUpnpServiceController(Context ctx);

    public ARendererState createRendererState();

    public IRendererCommand createRendererCommand(IRendererState rs);
}
