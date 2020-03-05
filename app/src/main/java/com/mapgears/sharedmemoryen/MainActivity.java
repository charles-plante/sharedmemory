package com.mapgears.sharedmemoryen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int ACCES_FINE_LOCATION_REQUEST = 0;
    private static final String TAG = "SharedMemory";
    private BackgroundLocationService gpsService;
    private boolean isTracking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Inside onCreateActivity");
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeToggleServiceButton();
    }

    private void initializeToggleServiceButton() {
        final Button startService = findViewById(R.id.start_service_button);
        if (isServiceRunning())
            startService.setText(R.string.stop_gps);
        else
            startService.setText(R.string.start_gps);

        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTracking) {
                    startTracking();
                    startService.setText(R.string.stop_gps);
                }
                else{
                    startService.setText(R.string.start_gps);
                    stopTracking();
                }
            }
        });
    }

    private void startTracking(){
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            final Intent intent = new Intent( this.getApplication(), BackgroundLocationService.class);
            this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            this.getApplication().startService(intent);
            isTracking = true;

        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCES_FINE_LOCATION_REQUEST);
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if( name.getClassName().endsWith("BackgroundLocationService")){
                gpsService = ((BackgroundLocationService.LocationServiceBinder)service).getService();
                TextView gpsStatusLabel = findViewById(R.id.l_appStatus);
                gpsStatusLabel.setText(R.string.gps_service_status);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (name.getClassName().endsWith("BackgroundLocationService")){
                gpsService = null;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCES_FINE_LOCATION_REQUEST){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startTracking();
            }
        }
    }

    private void stopTracking(){
        gpsService.stopTracking();
        isTracking = false;
    }
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.mapgears.sharedmemoryen.BackgroundLocationService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
