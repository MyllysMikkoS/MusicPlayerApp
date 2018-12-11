package com.propro.musicplayerapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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
        ImageView ivDelete = (ImageView) convertView.findViewById(R.id.deletePlaylistImage);
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getContext(), v);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.action_play_playlist:
                                // Clear old queue
                                if (Homescreen.musicService.isPlaying()){
                                    // If player is playing, stop it and clear queue
                                    Homescreen.musicService.stopPlaying();
                                    Homescreen.musicService.updateProgressBar();
                                    QueueSongs.getInstance().clear();
                                }
                                else {
                                    // If player is paused just clear queue
                                    if (Homescreen.musicService.isPrepared()) {
                                        try {
                                            Homescreen.musicService.stopPlaying();
                                        } catch (Exception e) {
                                            Log.d("MUSIC SERVICE: ", e.toString());
                                        }
                                    }
                                    QueueSongs.getInstance().clear();
                                    Homescreen.musicService.updateProgressBar();
                                }
                                Log.d("ITEMS IN QUEUE: ", "i: " + QueueSongs.getInstance().size());

                                // Add playlist items into queue
                                int missingSongs1 = 0;
                                for (Long id : CustomPlaylists.getInstance().get(position).SongIds){
                                    boolean songExistsInPaths = false;
                                    for (SongInfo song : AllSongs.getInstance()){
                                        if (id == song.Id){
                                            songExistsInPaths = true;
                                            QueueSongs.getInstance().add(song);
                                        }
                                    }
                                    if (!songExistsInPaths){
                                        missingSongs1++;
                                    }
                                }
                                if (missingSongs1 > 0) {
                                    CustomUtilities.showLongToast(getContext(), "Playlist is missing songs, check your music sources settings");
                                }

                                // Play queue automatically
                                Homescreen.musicService.continueQueueAfterPlaylistAdd();

                                return true;

                            case R.id.action_add_playlist_to_queue:
                                // Add playlist items into queue
                                int missingSongs2 = 0;
                                for (Long id : CustomPlaylists.getInstance().get(position).SongIds){
                                    boolean songExistsInPaths = false;
                                    for (SongInfo song : AllSongs.getInstance()){
                                        if (id == song.Id){
                                            songExistsInPaths = true;
                                            QueueSongs.getInstance().add(song);
                                        }
                                    }
                                    if (!songExistsInPaths){
                                        missingSongs2++;
                                    }
                                }
                                if (missingSongs2 > 0) {
                                    CustomUtilities.showLongToast(getContext(), "Playlist is missing " + missingSongs2 + " songs, check your music sources settings");
                                }

                                return true;

                            case R.id.action_delete_playlist:
                                // Set delete-event
                                Log.d("Playlist Delete: ", "clicked " + position);
                                remove(CustomPlaylists.getInstance().get(position));
                                CustomPlaylists.getInstance().remove(position);
                                CustomUtilities.updatePlaylists(getContext());
                                Log.d("ITEMS IN PLAYLISTS: ", "i: " + CustomPlaylists.getInstance().size());
                                notifyDataSetChanged();
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.playlist_item_menu, popup.getMenu());
                popup.show();
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
