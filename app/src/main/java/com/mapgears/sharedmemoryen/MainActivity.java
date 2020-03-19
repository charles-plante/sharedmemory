package com.mapgears.sharedmemoryen;
/*
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
*/
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private boolean bluetoothSet = false;
    private ConnectThread client;
    private AcceptThread serverThread;
    private BluetoothAdapter bluetoothAdapter;
    private final static int REQUEST_ENABLE_BT = 1;
    private IntentFilter filter;
    public UUID MY_UUID = UUID.fromString("98d3e17c-ce65-46d8-9f16-26722d5ac30f");
    public String NAME = "BlueCom" ;
    Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }


    public void displayToast(String text){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void blueToothManager(View view) {
        Switch simpleSwitch = (Switch) findViewById(R.id.switchBluetooth);
        if (simpleSwitch.isChecked()){
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                this.displayToast("Bluetooth not supported");
            }
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            bluetoothSet = true;
        }
        else {
            bluetoothAdapter.disable();
            bluetoothSet = false;
        }
    }

    public void listPairedDevices(View view) {
        if (!bluetoothSet){
            displayToast("Bluetooth not set");
            return;
        }
        pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            Log.i("DIM", "-----------------------");
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.i("DIM", deviceName);
                Log.i("DIM", deviceHardwareAddress);
                Log.i("DIM", "-----------------------");
            }
        }
    }

    public void makeDeviceVisible(View view) {
        if (!bluetoothSet){
            displayToast("Bluetooth not set");
            return;
        }
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30);
        startActivity(discoverableIntent);
    }


    public void serverLauncher(View view) {
        if (!bluetoothSet){
            displayToast("Bluetooth not set");
            return;
        }
        //Button btn = (Button) findViewById(R.id.btnServer);
        //btn.setEnabled(false);
        serverThread = new AcceptThread(bluetoothAdapter, NAME, MY_UUID);
        serverThread.setContext(getApplicationContext());
        serverThread.start();
        Log.i("DIM", "Lancement du serveur...");
    }

    public void clientLauncher(View view) {
        if (!bluetoothSet){
            displayToast("Bluetooth not set");
            return;
        }
        pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                TextView deviceSelected = (TextView) findViewById(R.id.deviceAddress);
                String MAC = deviceSelected.getText().toString();
                if(device.getAddress().equals(MAC)){
                    client = new ConnectThread(device, bluetoothAdapter, NAME, MY_UUID);
                    client.start();
                    Log.i("DIM", "Demande de connexion...");
                    break;
                }
                else{
                    this.displayToast("Device non trouve");
                }
            }
        }
    }

    public void closeConnec(View view) {
        if (!bluetoothSet){
            displayToast("Bluetooth not set");
            return;
        }
        try {
            client.interrupt();
            Log.i("DIM", "Connection interrompue");
        }
        catch (Exception e){
            Log.i("DIM", "Aucune connection");
        }
    }

    public void closeServ(View view) {
        if (!bluetoothSet){
            displayToast("Bluetooth not set");
            return;
        }
        try {
            Log.i("DIM", "Serveur ferme");
            serverThread.interrupt();

        } catch (Exception e) {
            Log.i("DIM", "Serveur non lance");
        }
    }
}


