package com.propro.musicplayerapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class SourcesAdapter extends ArrayAdapter<Source> {

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public SourcesAdapter(Context context, ArrayList<Source> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Source source = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.music_source_item, parent, false);
        }

        // Lookup view for data population
        TextView tvSource = (TextView) convertView.findViewById(R.id.sourcePathTextView);
        ImageView ivDelete = (ImageView) convertView.findViewById(R.id.deleteImage);
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set delete-event
                Log.d("Delete: ", "clicked " + position);
                remove(MusicSources.getInstance().get(position));
                MusicSources.getInstance().remove(position);
                Log.d("ITEMS IN SOURCE LIST: ", "i: " + MusicSources.getInstance().size());
            }
        });

        // Populate the data into the template view using the data object
        try {
            tvSource.setText(source.path);
        } catch (NullPointerException e) {
            Log.d("Error: ", e.toString());
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
