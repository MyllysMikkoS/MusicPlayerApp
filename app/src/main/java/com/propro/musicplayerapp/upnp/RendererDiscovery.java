package com.propro.musicplayerapp.upnp;

import com.propro.musicplayerapp.Homescreen;

public class RendererDiscovery extends DeviceDiscovery {

        protected static final String TAG = "RendererDeviceFragment";

        public RendererDiscovery(IServiceListener serviceListener)
        {
            super(serviceListener);
        }

        @Override
        protected ICallableFilter getCallableFilter()
        {
            return new CallableRendererFilter();
        }

        @Override
        protected boolean isSelected(IUpnpDevice device)
        {
            if (Homescreen.upnpServiceController != null && Homescreen.upnpServiceController.getSelectedRenderer() != null)
                return device.equals(Homescreen.upnpServiceController.getSelectedRenderer());

            return false;
        }

        @Override
        protected void select(IUpnpDevice device)
        {
            select(device, false);
        }

        @Override
        protected void select(IUpnpDevice device, boolean force)
        {
            Homescreen.upnpServiceController.setSelectedRenderer(device, force);
        }

        @Override
        protected void removed(IUpnpDevice d)
        {
            if (Homescreen.upnpServiceController != null && Homescreen.upnpServiceController.getSelectedRenderer() != null
                    && d.equals(Homescreen.upnpServiceController.getSelectedRenderer()))
                Homescreen.upnpServiceController.setSelectedRenderer(null);
        }
}
