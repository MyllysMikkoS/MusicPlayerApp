package com.propro.musicplayerapp.upnp;

import com.propro.musicplayerapp.R;
import org.fourthline.cling.support.model.item.Item;

import android.util.Log;

import java.util.List;

public class ClingDIDLItem extends ClingDIDLObject implements IDIDLItem {

    private static final String TAG = "ClingDIDLItem";

    public ClingDIDLItem(Item item)
    {
        super(item);
    }

    /*
    @Override
    public int getIcon()
    {
        return R.drawable.ic_file;
    }
    */
    @Override
    public String getURI()
    {
        if (item != null)
        {
            Log.d(TAG, "Item : " + item.getFirstResource().getValue());
            if (item.getFirstResource() != null && item.getFirstResource().getValue() != null)
                return item.getFirstResource().getValue();
        }
        return null;
    }
}
