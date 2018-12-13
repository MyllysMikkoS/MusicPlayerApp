package com.propro.musicplayerapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CustomUtilities {

    private static CustomUtilities sSoleInstance;
    private static Toast mToast;

    private CustomUtilities() {
        //Prevent form the reflection api.
        if (sSoleInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    // Thread safe implementation
    public synchronized static CustomUtilities getInstance(){
        if (sSoleInstance == null){ //if there is no instance available... create new one
            sSoleInstance = new CustomUtilities();
        }

        return sSoleInstance;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    try {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    } catch (Exception e) {
                        return Environment.getExternalStorageDirectory() + "";
                    }
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static void showToast(Context context, String message){
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public static void showLongToast(Context context, String message){
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        mToast.show();
    }

    public static void updatePlaylists(Context context){
        // Create JSONArray for playlists
        JSONArray playlists = new JSONArray();
        for (PlaylistInfo pl : CustomPlaylists.getInstance()){
            // Create JSONObject for every playlist
            JSONObject playlist = new JSONObject();
            JSONArray songIds = new JSONArray();
            try {
                // Set name of the playlist
                playlist.put("Name", pl.Name);

                // Set song ids
                for (Long id : pl.SongIds){
                    songIds.put(id);
                }
                playlist.put("SongIds", songIds);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Add playlist JSON to playlists array
            playlists.put(playlist);
        }

        Log.d("PLAYLIST JSON LOOKS: ", playlists.toString());

        // Save playlistJSON string to shared preferences
        try {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("playlists", playlists.toString()).apply();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void readPlaylists(Context context){
        try {
            if (PreferenceManager.getDefaultSharedPreferences(context).contains("playlists")) {
                String playlistString = PreferenceManager.getDefaultSharedPreferences(context).getString("playlists", "NO PLAYLISTS");

                // Parse playlists into PlaylistInfo-objects
                try {
                    JSONArray playlists = new JSONArray(playlistString);

                    // Clear old playlists
                    CustomPlaylists.getInstance().clear();

                    // Get separate playlists
                    for (int i = 0; i < playlists.length(); i++) {
                        JSONObject jsonobject = playlists.getJSONObject(i);
                        String name = jsonobject.getString("Name");
                        ArrayList<Long> ids = new ArrayList<Long>();
                        JSONArray songIds = jsonobject.getJSONArray("SongIds");
                        for (int j = 0; j < songIds.length(); j++) {
                            Long id = songIds.getLong(j);
                            ids.add(id);
                        }

                        // Create new playlists
                        CustomPlaylists.getInstance().add(new PlaylistInfo(name, ids));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void updateMusicSources(Context context){
        // Create JSONArray for music sources
        JSONArray sources = new JSONArray();
        for (Source source : MusicSources.getInstance()){
            try {
                // Put path string to json array
                sources.put(source.path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Log.d("SOURCES JSON LOOKS: ", sources.toString());

        // Save sourcesJSON string to shared preferences
        try {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("sources", sources.toString()).apply();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        Log.d("MUSIC SOURCES: ", MusicSources.getInstance().size() + " music source/sources updated");
    }

    public static void readMusicSources(Context context){
        try {
            if (PreferenceManager.getDefaultSharedPreferences(context).contains("sources")) {
                String sourcesString = PreferenceManager.getDefaultSharedPreferences(context).getString("sources", "NO SOURCES");

                // Parse sources into MusicSources-singleton
                try {
                    JSONArray sources = new JSONArray(sourcesString);

                    // Clear old sources
                    MusicSources.getInstance().clear();

                    // Get separate sources
                    for (int i = 0; i < sources.length(); i++) {
                        String path = sources.getString(i);

                        // Create new source object into MusicSources
                        MusicSources.getInstance().add(new Source(path));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        Log.d("MUSIC SOURCES: ", MusicSources.getInstance().size() + " music source/sources read");
    }
}
