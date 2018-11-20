package com.propro.musicplayerapp.upnp;

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
            if (Main.upnpServiceController != null && Main.upnpServiceController.getSelectedRenderer() != null)
                return device.equals(Main.upnpServiceController.getSelectedRenderer());

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
            Main.upnpServiceController.setSelectedRenderer(device, force);
        }

        @Override
        protected void removed(IUpnpDevice d)
        {
            if (Main.upnpServiceController != null && Main.upnpServiceController.getSelectedRenderer() != null
                    && d.equals(Main.upnpServiceController.getSelectedRenderer()))
                Main.upnpServiceController.setSelectedRenderer(null);
        }
}
