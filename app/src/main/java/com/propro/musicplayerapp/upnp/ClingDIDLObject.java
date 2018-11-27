package com.propro.musicplayerapp.upnp;

import org.fourthline.cling.support.model.DIDLObject;

public class ClingDIDLObject implements IDIDLObject {

    private static final String TAG = "ClingDIDLObject";

    protected DIDLObject item;

    public ClingDIDLObject(DIDLObject item)
    {
        this.item = item;
    }

    public DIDLObject getObject()
    {
        return item;
    }

    @Override
    public String getDataType()
    {
        return "";
    }

    @Override
    public String getTitle()
    {
        return item.getTitle();
    }

    @Override
    public String getDescription()
    {
        return "";
    }

    @Override
    public String getCount()
    {
        return "";
    }

    @Override
    public int getIcon()
    {
        return android.R.color.transparent;
    }

    @Override
    public String getParentID()
    {
        return item.getParentID();
    }

    @Override
    public String getId()
    {
        return item.getId();
    }
}
