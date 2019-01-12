package cat.uab.falldetectionapp.com.falldetection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import cat.uab.falldetectionapp.com.falldetection.listeners.NotifyListener;
import cat.uab.falldetectionapp.com.falldetection.model.BatteryInfo;

public class mainView extends AppCompatActivity{
    Button authentication_btn, DiscoverButton, show_info;
    ListView list, lvNewDevices, list_paired;
    TextView status, batteryPercent;
    BluetoothAdapter bluetoothAdapter;

    private DrawerLayout dl;
    RelativeLayout postConnectionLayout;
    private ActionBarDrawerToggle t;
    private NavigationView nv;
    private MiBand miband;
    private boolean connectionChecker = false;
    private static final int REQUEST_ENABLE = 0;
    private static final int REQUEST_DISCOVERABLE = 0;
    private static final String TAG = "MainActivity";
    private BluetoothGattCallback miBandGattCallBack;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService variableService;
    private SharedPreferences sharedPreferences;
    private Map<UUID, String> deviceInfoMap;
    private final Object object = new Object();
    public DeviceListAdapter mDeviceListAdapter, pairedListAdapter;


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);
        Intent intent = this.getIntent();
        postConnectionLayout = findViewById(R.id.postConnectionLayout);
        postConnectionLayout.setVisibility(View.GONE);
        miband = new MiBand(this);
        final BluetoothDevice device = intent.getParcelableExtra("device");
        if(device != null){
            status = findViewById(R.id.status);
            status.setText(R.string.connecting);
            miband.connect(device, new ActionCallback() {
                @Override
                public void onSuccess(Object data) {
                    if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                        System.out.println("already paired");
                    }else {
                        device.createBond();
                    }
                    setStatus(R.string.connected);
                    connectionChecker = true;
                    miband.setDisconnectedListener(new NotifyListener() {
                        @Override
                        public void onNotify(byte[] data) {
                            setStatus(R.string.disconnected);
                            connectionChecker = false;
                        }
                    });
                }
                @Override
                public void onFail(int errorCode, String msg) {
                    System.out.println("connect fail, code:" + errorCode + ",mgs:" + msg);
                }
            });
        }

        dl = findViewById(R.id.activity_main);
        t = new ActionBarDrawerToggle(this, dl,R.string.Open, R.string.Close);
        dl.addDrawerListener(t);
        t.syncState();
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){
            System.out.println(e);
        }
        nv = findViewById(R.id.nv);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.account:
                        Toast.makeText(mainView.this, "Developer Info",Toast.LENGTH_SHORT).show();
                    default:
                        return true;
                }

            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter== null){
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
        }
        DiscoverButton = findViewById(R.id.discoverButton);
        DiscoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(mainView.this, DiscoverDevice.class);
            startActivity(intent);
            mainView.this.finish();
            }
        });
        authentication_btn = findViewById(R.id.authentication_btn);
        authentication_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(connectionChecker){
                    miband.authorizeMiBand(new ActionCallback() {
                        @Override
                        public void onSuccess(Object data) {
                            System.out.println("success");
                            showDeviceInfos();
                        }
                        @Override
                        public void onFail(int errorCode, String msg) {
                            System.out.println("auth fail");
                        }
                    });
                }else{
                    Toast.makeText(mainView.this,"Please connect to device first", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void showDeviceInfos(){
        miband.getBatteryInfo(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                BatteryInfo info = (BatteryInfo) data;
                System.out.println(String.valueOf(info.getLevel())+"%");
                setBattery(String.valueOf(info.getLevel())+"%");
            }

            @Override
            public void onFail(int errorCode, String msg) {
                System.out.println("getBatteryInfo fail");
            }
        });
    }

    private void setBattery(final String battery){
        batteryPercent = findViewById(R.id.batteryPercent);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                batteryPercent.setText(battery);
                postConnectionLayout.setVisibility(View.VISIBLE);
            }
        });
    }





    private void setStatus(final int statustext){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText(statustext);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(t.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }


    private void handleDeviceInfo(BluetoothGattCharacteristic characteristic) {
        String value = characteristic.getStringValue(0);
        System.out.println("onCharacteristicRead: " + value + " UUID " + characteristic.getUuid().toString());
        synchronized (object) {
            object.notify();
        }
    }
    private void handleHeartRateData(final BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        System.out.println(characteristic.getValue());
        System.out.println("length > " + data.length);
        for (byte i: data){
            System.out.println("hearth data "+ i);
        }
        System.out.println("heart > "+characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0).toString());

    }

    private void getDeviceInformation() {
        variableService = bluetoothGatt.getService(UUIDs.DEVICE_INFORMATION_SERVICE);
        try {
            for (BluetoothGattCharacteristic characteristic : variableService.getCharacteristics()) {
                bluetoothGatt.setCharacteristicNotification(characteristic, true);
                bluetoothGatt.readCharacteristic(characteristic);
                synchronized (object) {
                    object.wait(2000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
