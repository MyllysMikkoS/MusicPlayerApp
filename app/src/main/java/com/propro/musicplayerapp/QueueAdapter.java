package com.propro.musicplayerapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class QueueAdapter extends ArrayAdapter<SongInfo> {

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public QueueAdapter(Context context, ArrayList<SongInfo> queueSongs) {
        super(context, 0, queueSongs);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        SongInfo songInfo = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.queue_item, parent, false);
        }

        // Lookup view for data population
        TextView tvSongName = (TextView) convertView.findViewById(R.id.queueSongNameTextView);
        TextView tvArtistName = (TextView) convertView.findViewById(R.id.queueArtistTextView);
        ImageView ivDelete = (ImageView) convertView.findViewById(R.id.queueDeleteImage);
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set delete-event
                Log.d("Delete: ", "clicked " + position);

                if (position == 0 && Homescreen.musicService.isPlaying()){
                    // Remove song from adapter
                    remove(QueueSongs.getInstance().get(position));
                    // skip to next song
                    Homescreen.musicService.skipToNext();
                }
                else {
                    // Remove song from adapter
                    remove(QueueSongs.getInstance().get(position));
                    QueueSongs.getInstance().remove(position);
                }
                Log.d("ITEMS IN QUEUE: ", "i: " + QueueSongs.getInstance().size());
            }
        });

        // Populate the data into the template view using the data object
        try {
            tvSongName.setText(songInfo.Title);
            tvArtistName.setText(songInfo.Artist);
        } catch (NullPointerException e) {
            Log.d("Error: ", e.toString());
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
