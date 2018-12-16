package com.propro.musicplayerapp.upnp;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.propro.musicplayerapp.Homescreen;
import com.propro.musicplayerapp.QueueSongs;
import com.propro.musicplayerapp.SongInfo;

public class MediaServer extends com.propro.musicplayerapp.server.SimpleWebServer
{
    private final static String TAG = "MediaServer";

    private UDN udn = null;
    private LocalDevice localDevice = null;
    private LocalService localService = null;
    private Context ctx = null;
    private String setFilePath = "";
    private static long songId = -1;

    private final static int port = 8192;
    private static InetAddress localAddress;


    public MediaServer(InetAddress localAddress, Context ctx) throws ValidationException
    {
        super(null, port, null, true);

        Log.i(TAG, "Creating media server !");

        udn = UDN.valueOf(new UUID(0,10).toString());
        this.localAddress = localAddress;
        this.ctx = ctx;
        createLocalDevice();

    }

    public void restart()
    {
        Log.d(TAG, "Restart mediaServer");
//		try {
//			stop();
//			createLocalDevice();
//			start();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
    }

    public void createLocalDevice() throws ValidationException
    {
        DeviceDetails details = new DeviceDetails("myfriendlyname");
        DeviceType type = new UDADeviceType("MediaServer", 1);

        localDevice = new LocalDevice(new DeviceIdentity(udn), type, details, localService);
    }


    public LocalDevice getDevice() {
        return localDevice;
    }

    public String getAddress() {
        return localAddress.getHostAddress() + ":" + port;
    }

    public class InvalidIdentificatorException extends java.lang.Exception
    {
        public InvalidIdentificatorException(){super();}
        public InvalidIdentificatorException(String message){super(message);}
    }

    class ServerObject
    {
        ServerObject(String path, String mime)
        {
            this.path = path;
            this.mime = mime;
        }
        public String path;
        public String mime;
    }

    private ServerObject getFileServerObject(String id) throws InvalidIdentificatorException
    {
        try
        {
            Log.i(TAG, "Getting current song from queue");
            SongInfo currSong = QueueSongs.getInstance().get(0);
            String path = currSong.path;
            String mime = currSong.mime_type;

            if(path!=null)
                setFilePath = path;
                songId = currSong.Id;
                return new ServerObject(path, mime);

        }
        catch (Exception e)
        {
            Log.e(TAG, "Error while parsing " + id);
            Log.e(TAG, "exception", e);
        }

        throw new InvalidIdentificatorException(id + " was not found in media database");
    }

    @Override
    public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parms,
                          Map<String, String> files)
    {
        Response res = null;

        Log.i(TAG, "Serve uri : " + uri);

        for(Map.Entry<String, String> entry : header.entrySet())
            Log.d(TAG, "Header : key=" + entry.getKey() + " value=" + entry.getValue());

        for(Map.Entry<String, String> entry : parms.entrySet())
            Log.d(TAG, "Params : key=" + entry.getKey() + " value=" + entry.getValue());

        for(Map.Entry<String, String> entry : files.entrySet())
            Log.d(TAG, "Files : key=" + entry.getKey() + " value=" + entry.getValue());

        try
        {
            try
            {
                Log.i(TAG, "Trying to get fileobject");
                ServerObject obj = getFileServerObject(uri);

                Log.i(TAG, "Will serve " + obj.path);
                res = serveFile(new File(obj.path), obj.mime, header);
            }
            catch(InvalidIdentificatorException e)
            {
                return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Error 404, file not found.");
            }

            if( res != null )
            {
                /*
                String version = "1.0";
                try {
                    version = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "Application version name not found");
                }
                */
                // Some DLNA header option
                res.addHeader("realTimeInfo.dlna.org", "DLNA.ORG_TLAG=*");
                res.addHeader("contentFeatures.dlna.org", "");
                res.addHeader("transferMode.dlna.org", "Streaming");
                //res.addHeader("Server", "DLNADOC/1.50 UPnP/1.0 Cling/2.0 DroidUPnP/"+version +" Android/" + Build.VERSION.RELEASE);
            }

            return res;
        }
        catch(Exception e)
        {
            Log.e(TAG, "Unexpected error while serving file");
            Log.e(TAG, "exception", e);
        }

        return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "INTERNAL ERROR: unexpected error.");
    }

    public long getSongId(){
        return songId;
    }
}

