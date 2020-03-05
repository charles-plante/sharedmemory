package com.mapgears.sharedmemoryen;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;


public class BackgroundLocationService extends Service {

    private static final String TAG = "SharedMemory";
    private final LocationServiceBinder binder = new LocationServiceBinder();
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    public BackgroundLocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {        return binder;    }

    private class LocationListener implements android.location.LocationListener {

        LocationListener() {
            new Location(LocationManager.GPS_PROVIDER);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "LocationChanged: " + location);
            Toast.makeText(BackgroundLocationService.this, "LAT: " + location.getLatitude() + "\n LONG: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startForeground(12345678, getNotification());
        startTracking();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            try{
                mLocationManager.removeUpdates(mLocationListener);
            } catch (Exception ignored) {}
        }

    }

    private void startTracking() {

        initializeLocationManager();
        mLocationListener = new LocationListener();

        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300, 1, mLocationListener);
        } catch (java.lang.SecurityException ex) {
            Log.i("TAG", "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d("TAG", "gps provider does not exist " + ex.getMessage());
        }

    }

    private void initializeLocationManager() {
        if(mLocationManager == null){
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void stopTracking(){
        stopForeground(true);
        this.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification() {

        NotificationChannel channel = new NotificationChannel("channel_01", "GPS Status", NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), "channel_01").setAutoCancel(true);
        return builder.build();
    }

    class LocationServiceBinder extends Binder {
        public BackgroundLocationService getService(){return BackgroundLocationService.this;}
    }
}