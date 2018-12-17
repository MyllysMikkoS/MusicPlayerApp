package com.propro.musicplayerapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.propro.musicplayerapp.upnp.ARendererState;
import com.propro.musicplayerapp.upnp.IRendererCommand;
import com.propro.musicplayerapp.upnp.IUpnpDevice;
import com.propro.musicplayerapp.upnp.IUpnpServiceController;
import com.propro.musicplayerapp.upnp.IFactory;
import com.propro.musicplayerapp.upnp.Factory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

public class Homescreen extends AppCompatActivity implements Observer {

    // Views
    Toolbar toolbar;
    RelativeLayout mediaButtonsRelativeLayout;
    MediaButtons mediaButtons;
    TextView songTitle;
    TextView artistName;
    TextView streamingInfo;

    // Player
    public static MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    public static boolean localPlayback = true;

    // Controller for upnp
    public static IUpnpServiceController upnpServiceController = null;
    public static IFactory factory = null;
    private static final String TAG = "HomeScreen";

    private IUpnpDevice device;
    private ARendererState rendererState;
    public static IRendererCommand rendererCommand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_homescreen);

        // Ask for permissions
        PermissionChecks.getInstance().checkPermissionREAD_EXTERNAL_STORAGE(this);
        PermissionChecks.getInstance().checkPermissionWRITE_EXTERNAL_STORAGE(this);
        PermissionChecks.getInstance().checkPermissionWAKE_LOCK(this);

        // Init views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch(NullPointerException e){
            Log.d("Removing title error: ", e.toString());
        }
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        songTitle = findViewById(R.id.songTitleTextView);
        artistName = findViewById(R.id.artistNameTextView);
        streamingInfo = findViewById(R.id.streamingTextView);

        streamingInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (localPlayback) {
                    localPlayback = false;

                    String streamStatus;
                    if (Homescreen.upnpServiceController.getSelectedRenderer() == null)
                    {
                        Log.i(TAG, "No current renderer");
                        streamStatus = "Streaming: Device not selected";
                    }
                    else {
                        streamStatus = "Streaming: " + upnpServiceController.getSelectedRenderer().getFriendlyName();
                    }
                    streamingInfo.setText(streamStatus);

                    startControlPoint();
                }
                else{
                    localPlayback = true;
                    String streamStatus = "Streaming: off";
                    streamingInfo.setText(streamStatus);
                    if (rendererCommand != null)
                        rendererCommand.pause();
                }
            }
        });

        // Create MediaButtonsWidget
        mediaButtonsRelativeLayout = findViewById(R.id.MediaButtonsRelativeLayout);
        mediaButtons = new MediaButtons(this);
        mediaButtons.setOnSliceClickListener(new MediaButtons.OnSliceClickListener() {
            @Override
            public void onSlickClick(int slicePosition, float progress) {
                //String text = String.valueOf(slicePosition) + " " + String.valueOf(progress);
                //songTitle.setText(text);

                // WHEN PLAY/PAUSE BUTTON IS CLICKED
                if (slicePosition == -2 && !MediaButtons.pause) {
                    if (localPlayback) {
                        musicService.continueQueue();
                    }
                    else{
                        if (rendererCommand != null)

                            rendererCommand.prepareNextSong(false);
                            //rendererCommand.commandPlay();
                    }

                }
                else if (slicePosition == -2 && MediaButtons.pause){

                    if (localPlayback) {
                        musicService.pauseQueue();
                    }
                    else{
                        if (rendererCommand != null)
                            rendererCommand.commandToggle();
                    }
                }
                // PRESSING SKIP NEXT
                if (slicePosition == 0){
                    if (localPlayback) {
                        musicService.skipToNext();
                    }

                }
                // PRESSING PREVIOUS SONG
                if (slicePosition == 3){
                    if (localPlayback) {
                        musicService.previousSong();
                    }

                }
                if (slicePosition == -1){
                    if (localPlayback) {
                        musicService.progressBarChange();
                    }

                }
            }
        });
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mediaButtonsRelativeLayout.addView(mediaButtons, params);

        // Read playlists
        CustomUtilities.readPlaylists(this);

        // Read music sources
        CustomUtilities.readMusicSources(this);

        // Use cling factory
        if (factory == null)
            factory = new Factory();

        // Upnp service
        if (upnpServiceController == null)
            upnpServiceController = factory.createUpnpServiceController(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (localPlayback) {
            if (playIntent == null){
                playIntent = new Intent(this, MusicService.class);
                bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
                startService(playIntent);

            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (localPlayback) {
            String streamStatus = "Streaming: off";
            streamingInfo.setText(streamStatus);

            if (musicService != null) {
                musicService.setSongInfo();
            }
        }
        else {

            String streamStatus = "Streaming: " + upnpServiceController.getSelectedRenderer().getFriendlyName();
            streamingInfo.setText(streamStatus);
            if (rendererCommand != null)
                rendererCommand.resume();
            startControlPoint();

            if (rendererCommand != null)
                rendererCommand.resume();
        }
        // added later
        upnpServiceController.resume(this);

        // if sources are empty, move to settings to add sources
        if (MusicSources.getInstance().size() == 0){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No music");

            // Set the test
            builder.setMessage("There is currently no music in the library. Please set music sources from settings to start listening music.");

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent settings = new Intent(getApplicationContext(), Settings.class);
                    startActivity(settings);
                }
            });

            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onPause()
    {
        Log.v(TAG, "Pause activity");
        //upnpServiceController.pause();
        //upnpServiceController.getServiceListener().getServiceConnexion().onServiceDisconnected(null);

        //device = null;
        if (rendererCommand != null)
            rendererCommand.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (rendererCommand != null){
            rendererCommand.commandPause();
            rendererCommand.commandStop();
        }
        upnpServiceController.pause();
        //upnpServiceController.getServiceListener().getServiceConnexion().onServiceDisconnected(null);
        Homescreen.upnpServiceController.delSelectedRendererObserver(this);

        if (musicConnection != null){
            unbindService(musicConnection);
        }
        Log.d("ONDESTROY: ", "CALLED");
        //super.onDestroy();
    }

    // connect to MusicService
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            // get service
            musicService = binder.getService(mediaButtons, songTitle, artistName);
            // pass list
            musicBound = true;
            // Update all info on activity first time
            try {
                musicService.progressBarChange();
            }
            catch (Exception e){
                Log.e("HOMESCREEN: ", "Progress init error", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            musicBound = false;
        }
    };

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent settings = new Intent(this, Settings.class);
                startActivity(settings);
                return true;

            case R.id.action_music:
                // Check that permissions are granted, otherwise go back
                if (PermissionChecks.getInstance().checkPermissionREAD_EXTERNAL_STORAGE(this)){
                    if (PermissionChecks.getInstance().checkPermissionWRITE_EXTERNAL_STORAGE(this)) {
                        if (PermissionChecks.getInstance().checkPermissionWAKE_LOCK(this)){
                            Intent music = new Intent(this, Music.class);
                            startActivity(music);
                        }
                        else {
                            CustomUtilities.showToast(this, "No Wake-Lock Permission");
                        }
                    }
                    else {
                        CustomUtilities.showToast(this, "No Write-External-Storage Permission");
                    }
                }
                else {
                    CustomUtilities.showToast(this, "No Read-External-Storage Permission");
                }
                return true;

            case R.id.action_stream:
                Intent stream = new Intent(this, Stream.class);
                startActivity(stream);
                return true;

            case R.id.action_queue:
                Intent queue = new Intent(this, Queue.class);
                startActivity(queue);
                return true;
        }

        return(super.onOptionsItemSelected(item));
    }

    private static InetAddress getLocalIpAdressFromIntf(String intfName)
    {
        try
        {
            NetworkInterface intf = NetworkInterface.getByName(intfName);
            if(intf.isUp())
            {
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address)
                        return inetAddress;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to get ip adress for interface " + intfName);
        }
        return null;
    }

    public static InetAddress getLocalIpAddress(Context ctx) throws UnknownHostException
    {
        // ctx.getApplicationContext() was previously only ctx
        WifiManager wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        // also added locale
        if(ipAddress!=0)
            return InetAddress.getByName(String.format(Locale.US, "%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff)));

        Log.d(TAG, "No ip adress available throught wifi manager, try to get it manually");

        InetAddress inetAddress;

        inetAddress = getLocalIpAdressFromIntf("wlan0");
        if(inetAddress!=null)
        {
            Log.d(TAG, "Got an ip for interfarce wlan0");
            return inetAddress;
        }

        inetAddress = getLocalIpAdressFromIntf("usb0");
        if(inetAddress!=null)
        {
            Log.d(TAG, "Got an ip for interfarce usb0");
            return inetAddress;
        }

        return InetAddress.getByName("0.0.0.0");
    }

    public void startControlPoint()
    {
        if (Homescreen.upnpServiceController.getSelectedRenderer() == null)
        {
            Log.i(TAG, "Current renderer have been removed");
            return;
        }

        if (device == null || rendererState == null || rendererCommand == null
                || !device.equals(Homescreen.upnpServiceController.getSelectedRenderer()))
        {
            device = Homescreen.upnpServiceController.getSelectedRenderer();

            Log.i(TAG, "Renderer changed !!! " + Homescreen.upnpServiceController.getSelectedRenderer().getDisplayString());

            rendererState = Homescreen.factory.createRendererState();
            rendererCommand = Homescreen.factory.createRendererCommand(rendererState);

            if (rendererState == null || rendererCommand == null)
            {
                Log.e(TAG, "Fail to create renderer command and/or state");
                return;
            }

            rendererCommand.resume();

            rendererState.addObserver(this);
            rendererCommand.updateFull();
        }
        updateRenderer();
    }

    @Override
    public void update(Observable observable, Object data)
    {
        Log.v(TAG, "update method called");
        startControlPoint();
    }

    public void updateRenderer()
    {
        Log.v(TAG, "updateRenderer");

        if (rendererState != null)

        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                //call the invalidate()
                    if (QueueSongs.getInstance().size() > 0) {
                        //------------

                        int seconds = QueueSongs.getInstance().get(0).Length / 1000;

                        // If song length less than minute
                        if (seconds < 0){
                            MediaButtons.songLength = "00:00";
                        }
                        if (seconds < 60 && seconds >= 0){
                            if (seconds < 10) MediaButtons.songLength = "00:0" + seconds;
                            else MediaButtons.songLength = "00:" + seconds;
                        }
                        // Otherwise
                        else {
                            int minutes = seconds / 60;
                            int remainingSeconds = seconds - minutes*60;

                            if (minutes < 10 && remainingSeconds < 10){
                                MediaButtons.songLength = "0" + minutes + ":0" + remainingSeconds;
                            }
                            else if (minutes >= 10 && remainingSeconds < 10){
                                MediaButtons.songLength = minutes + ":0" + remainingSeconds;
                            }
                            else if (minutes < 10){
                                MediaButtons.songLength = "0" + minutes + ":" + remainingSeconds;
                            }
                            else {
                                MediaButtons.songLength = minutes + ":" + remainingSeconds;
                            }
                        }

                        //--------
                        updateCurrentTime();
                        songTitle.setText(QueueSongs.getInstance().get(0).Title);
                        artistName.setText(QueueSongs.getInstance().get(0).Artist);

                        mediaButtons.invalidate();


                    }
                    else {
                        // set titles "no song" and times 00:00
                        songTitle.setText("NO SONG");
                        artistName.setText("NO ARTIST");
                        MediaButtons.currentTime = "00:00";
                        MediaButtons.songLength = "00:00";
                        mediaButtons.invalidate();
                    }
                }
            });
            Log.v(TAG, rendererState.toString());
        }
    }

    public void updateCurrentTime(){
        // Set current time
        try {
            //int currentProgressInSeconds;
            int currentProgressInSeconds = (int)rendererState.getElapsedSeconds();
            int durationSeconds = (int)rendererState.getDurationSeconds();
            //Log.d(TAG, "elapsed time " + String.valueOf(currentProgressInSeconds) + " duration " + String.valueOf(durationSeconds));
            // should
            /*
            if (elapsedSeconds < durationSeconds) {
                //float curr = player.getCurrentPosition();
                float divider = 1000;
                float num = Math.round(elapsedSeconds / divider);
                currentProgressInSeconds = Math.round(num);
            }
            else currentProgressInSeconds = 0;
            */
            if (currentProgressInSeconds < 0){
                MediaButtons.songLength = "00:00";
            }
            if (currentProgressInSeconds < 60 && currentProgressInSeconds >= 0){
                if (currentProgressInSeconds < 10) MediaButtons.currentTime = "00:0" + currentProgressInSeconds;
                else MediaButtons.currentTime = "00:" + currentProgressInSeconds;
            }
            else {
                int mins = currentProgressInSeconds / 60;
                int secs = currentProgressInSeconds - mins*60;

                if (mins < 10 && secs < 10){
                    MediaButtons.currentTime = "0" + mins + ":0" + secs;
                }
                else if (mins >= 10 && secs < 10){
                    MediaButtons.currentTime = mins + ":0" + secs;
                }
                else if (mins < 10){
                    MediaButtons.currentTime = "0" + mins + ":" + secs;
                }
                else {
                    MediaButtons.currentTime = mins + ":" + secs;
                }
            }
            //mediaButtons.invalidate();
        }
        catch (Exception e){
            Log.d("MUSIC SERVICE: ", "No player prepared, default time is 00:00");
            MediaButtons.currentTime = "00:00";
            //mediaButtons.invalidate();
        }
    }
    /**/

}
