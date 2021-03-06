package com.propro.musicplayerapp;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

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
        ImageView ivSongOptions = (ImageView) convertView.findViewById(R.id.songOptionsImageButton);
        ivSongOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getContext(), v);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.action_play:
                                // Set play-event
                                Log.d("Song Play: ", "clicked " + position);
                                if (Homescreen.musicService.isPlaying()) Homescreen.musicService.stopPlaying();
                                QueueSongs.getInstance().add(0, AllSongs.getInstance().get(position));
                                Homescreen.musicService.playSong();
                                return true;

                            case R.id.action_add_to_queue:
                                // Set add-event
                                Log.d("Song Add to queue: ", "clicked " + position);
                                QueueSongs.getInstance().add(AllSongs.getInstance().get(position));
                                CustomUtilities.showToast(getContext(), AllSongs.getInstance().get(position).Title + " added to queue");
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.music_item_menu, popup.getMenu());
                popup.show();
            }
        });

        // Populate the data into the template view using the data object
        try {
            tvSongName.setText(songInfo.Title);
        } catch (NullPointerException e) {
            Log.d("Error: ", e.toString());
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
