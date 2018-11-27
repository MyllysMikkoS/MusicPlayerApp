package com.propro.musicplayerapp.upnp;

import com.propro.musicplayerapp.R;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.MusicTrack;

import java.util.List;

public class ClingAudioItem extends ClingDIDLItem
{
    public ClingAudioItem(AudioItem item)
    {
        super(item);
    }

    @Override
    public String getDataType()
    {
        return "audio/*";
    }

    @Override
    public String getDescription()
    {
        if(item instanceof MusicTrack)
        {
            MusicTrack track = (MusicTrack) item;
            return ( (track.getFirstArtist()!=null && track.getFirstArtist().getName()!=null) ? track.getFirstArtist().getName() : "") +
                    ((track.getAlbum()!=null) ?  (" - " + track.getAlbum()) : "");
        }
        return ((AudioItem) item).getDescription();
    }

    @Override
    public String getCount()
    {
        List<Res> res = item.getResources();
        if(res!=null && res.size()>0)
            return "" + ((res.get(0).getDuration()!=null) ? res.get(0).getDuration().split("\\.")[0] : "");

        return "";
    }

    /*
    @Override
    public int getIcon()
    {
        return R.drawable.ic_action_headphones;
    }
    */
}

