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

public class PlaylistsAdapter extends ArrayAdapter<PlaylistInfo> {

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public PlaylistsAdapter(Context context, ArrayList<PlaylistInfo> playlists) {
        super(context, 0, playlists);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        PlaylistInfo playlistInfo = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.playlists_item, parent, false);
        }

        // Lookup view for data population
        TextView tvPlaylistName = (TextView) convertView.findViewById(R.id.playlistNameTextView);
        ImageView ivPlay = (ImageView) convertView.findViewById(R.id.playPlaylistImage);
        ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set play-event
                Log.d("Playlist Play: ", "clicked " + position);
            }
        });
        ImageView ivAdd = (ImageView) convertView.findViewById(R.id.addPlaylistToQueueImage);
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set add-event
                Log.d("Playlist Add to queue: ", "clicked " + position);
            }
        });
        ImageView ivDelete = (ImageView) convertView.findViewById(R.id.deletePlaylistImage);
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set delete-event
                Log.d("Playlist Delete: ", "clicked " + position);
                remove(CustomPlaylists.getInstance().get(position));
                CustomPlaylists.getInstance().remove(position);
                Log.d("ITEMS IN PLAYLISTS: ", "i: " + QueueSongs.getInstance().size());
            }
        });

        // Populate the data into the template view using the data object
        try {
            tvPlaylistName.setText(playlistInfo.Name);
        } catch (NullPointerException e) {
            Log.d("Error: ", e.toString());
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
