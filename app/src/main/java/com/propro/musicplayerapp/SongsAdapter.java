package com.propro.musicplayerapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class SongsAdapter extends ArrayAdapter<SongInfo> {

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public SongsAdapter(Context context, ArrayList<SongInfo> songs) {
        super(context, 0, songs);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        SongInfo songInfo = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.songs_item, parent, false);
        }

        // Lookup view for data population
        TextView tvSongName = (TextView) convertView.findViewById(R.id.songNameTextView);
        ImageView ivPlay = (ImageView) convertView.findViewById(R.id.playSongImage);
        ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set play-event
                Log.d("Song Play: ", "clicked " + position);
            }
        });
        ImageView ivAdd = (ImageView) convertView.findViewById(R.id.addSongToQueueImage);
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set add-event
                Log.d("Song Add to queue: ", "clicked " + position);
            }
        });
        ImageView ivDelete = (ImageView) convertView.findViewById(R.id.deleteSongImage);
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set delete-event
                Log.d("Song Delete: ", "clicked " + position);
                remove(AllSongs.getInstance().get(position));
                AllSongs.getInstance().remove(position);
                // TODO: DELETE ALSO FROM PHONE
                Log.d("ITEMS IN SONGS: ", "i: " + AllSongs.getInstance().size());
            }
        });

        // Populate the data into the template view using the data object
        try {
            tvSongName.setText(songInfo.Name);
        } catch (NullPointerException e) {
            Log.d("Error: ", e.toString());
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
