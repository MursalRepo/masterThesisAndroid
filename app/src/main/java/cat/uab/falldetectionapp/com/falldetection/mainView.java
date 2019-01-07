package cat.uab.falldetectionapp.com.falldetection;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cat.uab.falldetectionapp.com.falldetection.listeners.NotifyListener;
import cat.uab.falldetectionapp.com.falldetection.model.BatteryInfo;
import cat.uab.falldetectionapp.com.falldetection.model.UserInfo;
public class mainView extends AppCompatActivity implements AdapterView.OnItemClickListener {
    Button b_on, b_off, b_list, b_disc;
    ListView list, lvNewDevices, list_paired;
    TextView status;
    BluetoothAdapter bluetoothAdapter;

    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;
    private MiBand miband;
    private static final int REQUEST_ENABLE = 0;
    private static final int REQUEST_DISCOVERABLE = 0;
    private static final String TAG = "MainActivity";
    private BluetoothGattCallback miBandGattCallBack;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService variableService;
    private SharedPreferences sharedPreferences;
    private Map<UUID, String> deviceInfoMap;
    private final Object object = new Object();

    BluetoothAdapter mBluetoothAdapter;
    Button btnEnableDisable_Discoverable;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    public DeviceListAdapter mDeviceListAdapter, pairedListAdapter;

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    /**
     * Broadcast Receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire.
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };




    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                Boolean is_discovering = mBluetoothAdapter.isDiscovering();
                System.out.println("status + + "+ is_discovering.toString());
                //Toast.makeText(this, "status "+is_discovering.toString(), Toast.LENGTH_SHORT).show();

                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };



    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
//        unregisterReceiver(mBroadcastReceiver1);
//        unregisterReceiver(mBroadcastReceiver2);
//        unregisterReceiver(mBroadcastReceiver3);
//        unregisterReceiver(mBroadcastReceiver4);
        //mBluetoothAdapter.cancelDiscovery();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);
        Intent intent = this.getIntent();
        miband = new MiBand(this);
        final BluetoothDevice device = intent.getParcelableExtra("device");
        if(device != null){
            status = findViewById(R.id.status);
            status.setText(R.string.connecting);
            miband.connect(device, new ActionCallback() {
                @Override
                public void onSuccess(Object data) {
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED){
                    }else {
                        device.createBond();
                    }
                    setStatus(R.string.connected);
                    miband.setDisconnectedListener(new NotifyListener() {
                        @Override
                        public void onNotify(byte[] data) {
                            setStatus(R.string.disconnected);
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

        lvNewDevices = findViewById(R.id.list_new);
        mBTDevices = new ArrayList<>();

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        lvNewDevices.setOnItemClickListener(mainView.this);
        b_on = (Button) findViewById(R.id.b_on);
        b_off = (Button) findViewById(R.id.b_off);
        b_list = (Button) findViewById(R.id.b_list);
        b_disc = (Button) findViewById(R.id.b_disc);

        list = (ListView) findViewById(R.id.list_new);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter== null){
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
        }

        b_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE);
            }
        });

        b_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            bluetoothAdapter.disable();
            }
        });
//
        b_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
                if(mBluetoothAdapter.isDiscovering()){
                    mBTDevices.clear();
                    System.out.println("is discovering");
                    mBluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "btnDiscover: Canceling discovery.");

                    //check BT permissions in manifest
                    checkBTPermissions();

                    mBluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                }
                if(!mBluetoothAdapter.isDiscovering()){
                    mBTDevices.clear();
                    System.out.println("is not discovering");
                    //check BT permissions in manifest
                    checkBTPermissions();

                    mBluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                }
            }
        });

        b_disc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (!bluetoothAdapter.isDiscovering()){
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(intent, REQUEST_DISCOVERABLE);
            }
            }
        });

        Button DiscoverButton = findViewById(R.id.discoverButton);
        DiscoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(mainView.this, DiscoverDevice.class);
            startActivity(intent);
            mainView.this.finish();
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

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();
        System.out.println("onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();
        System.out.println(deviceName+" "+deviceAddress);
        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        System.out.println(Build.VERSION.SDK_INT);
        System.out.println(Build.VERSION_CODES.JELLY_BEAN_MR2);
        System.out.println("Trying to pair with " + deviceName);
        System.out.println("bond state > "+ mBTDevices.get(i).getBondState());
        System.out.println("this > "+ BluetoothDevice.BOND_BONDED);
        if (mBTDevices.get(i).getBondState() == BluetoothDevice.BOND_BONDED){
            Toast.makeText(this, "already connected", Toast.LENGTH_LONG).show();
        }else {
            mBTDevices.get(i).createBond();
            System.out.println("connected");
        }
        System.out.println("starting intitialviews");
        initialiseViewsAndComponents();
        System.out.println("starting callback");
        bluetoothGatt = mBTDevices.get(i).connectGatt(getApplicationContext(), true, miBandGattCallBack);
//        ArrayList<String> pairedDevicesList = new ArrayList<>();
//        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//        for (BluetoothDevice bt: pairedDevices){
//            pairedDevicesList.add(bt.getName());
//        }
    }
    private void handleDeviceInfo(BluetoothGattCharacteristic characteristic) {
        String value = characteristic.getStringValue(0);
        System.out.println("onCharacteristicRead: " + value + " UUID " + characteristic.getUuid().toString());
        synchronized (object) {
            object.notify();
        }
        //deviceInfoMap.put(characteristic.getUuid(), value);
    }
    private void handleHeartRateData(final BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        System.out.println(characteristic.getValue());
        System.out.println("length > " + data.length);
        for (byte i: data){
            System.out.println("hearth data "+ i);
        }
        System.out.println("heart > "+characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0).toString());
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                heartRate.setText(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0).toString());
//            }
//        });

    }
    private void authoriseMiBand() {
        System.out.println("authorizing mi band ....");
        BluetoothGattService service = bluetoothGatt.getService(UUIDs.CUSTOM_SERVICE_FEE1);
        System.out.println(service);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUIDs.CUSTOM_SERVICE_AUTH_CHARACTERISTIC);
        bluetoothGatt.setCharacteristicNotification(characteristic, true);
        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
            if (descriptor.getUuid().equals(UUIDs.CUSTOM_SERVICE_AUTH_DESCRIPTOR)) {
                System.out.println("Found NOTIFICATION BluetoothGattDescriptor: " + descriptor.getUuid().toString());
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }
        }

        characteristic.setValue(new byte[]{0x01, 0x8, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45});
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    private void initialiseViewsAndComponents() {
        System.out.println("Initial methods");
        sharedPreferences = getSharedPreferences("MiBandConnectPreferences", Context.MODE_PRIVATE);
        Button get_info = (Button) findViewById(R.id.get_info);

        miBandGattCallBack = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                System.out.println("new state");
                System.out.println(newState);
                switch (newState) {
                    case BluetoothGatt.STATE_DISCONNECTED:
                        System.out.println("Device disconnected");

                        break;
                    case BluetoothGatt.STATE_CONNECTED: {
                        System.out.println("Connected with device");
                        System.out.println("Discovering services");
                        gatt.discoverServices();
                    }
                    break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                System.out.println("this method called");
                if (!sharedPreferences.getBoolean("isAuthenticated", false)) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isAuthenticated", true);
                    editor.apply();
                    authoriseMiBand();
                } else
                    System.out.println("Already authenticated");
                    //getDeviceInformation();
                    authoriseMiBand();

            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                System.out.println("onCharacteristicRead");
                System.out.println("Characteristic UUID "+ characteristic.getService().getUuid().toString());
                switch (characteristic.getService().getUuid().toString()) {
                    case UUIDs.DEVICE_INFORMATION_SERVICE_STRING:
                        handleDeviceInfo(characteristic);
                        break;
//                    case UUIDs.GENERIC_ACCESS_SERVICE_STRING:
//                        handleGenericAccess(characteristic);
//                        break;
//                    case UUIDs.GENERIC_ATTRIBUTE_SERVICE_STRING:
//                        handleGenericAttribute(characteristic);
//                        break;
//                    case UUIDs.ALERT_NOTIFICATION_SERVICE_STRING:
//                        handleAlertNotification(characteristic);
//                        break;
//                    case UUIDs.IMMEDIATE_ALERT_SERVICE_STRING:
//                        handleImmediateAlert(characteristic);
//                        break;
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                System.out.println("onCharacteristicWrite");
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                System.out.println("onCharacteristicChanged");
                System.out.println("onCharacteristicChanged UUID "+ characteristic.getService().getUuid().toString());
                switch (characteristic.getUuid().toString()) {
                    case UUIDs.HEART_RATE_MEASUREMENT_CHARACTERISTIC_STRING:
                        handleHeartRateData(characteristic);
                        break;
                }
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                System.out.println("Descriptor " + descriptor.getUuid().toString() + " Read");
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                System.out.println("Descriptor " + descriptor.getUuid().toString() + " Written");
            }
        };

        get_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceInformation();
                getHeartRate();
                batteryTestInfo();
            }
        });
        System.out.println("djshgd");
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
    private void getHeartRate() {
        System.out.println("getting hearth rate");
        variableService = bluetoothGatt.getService(UUIDs.HEART_RATE_SERVICE);
        System.out.println(variableService);
        BluetoothGattCharacteristic heartRateCharacteristic = variableService.getCharacteristic(UUIDs.HEART_RATE_MEASUREMENT_CHARACTERISTIC);
        BluetoothGattDescriptor heartRateDescriptor = heartRateCharacteristic.getDescriptor(UUIDs.HEART_RATE_MEASURMENT_DESCRIPTOR);

        bluetoothGatt.setCharacteristicNotification(heartRateCharacteristic, true);
        heartRateDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(heartRateDescriptor);
    }
    private void batteryTestInfo() {
        System.out.println("getting battery info");
        variableService = bluetoothGatt.getService(UUIDs.UUID_BATTERY_SERVICE);
        System.out.println(variableService);
        variableService = bluetoothGatt.getService(UUIDs.HEART_RATE_SERVICE);
        System.out.println(variableService);
        variableService = bluetoothGatt.getService(UUIDs.GENERIC_ACCESS_SERVICE);
        System.out.println(variableService);
//        BluetoothGattCharacteristic BatCharacteristic = variableService.getCharacteristic(UUIDs.HEART_RATE_MEASUREMENT_CHARACTERISTIC);
//        System.out.println(BatCharacteristic);
//        BluetoothGattCharacteristic BatCharacteristic2 = variableService.getCharacteristic(UUIDs.UUID_CHARACTERISTIC_BATTERY_INFO);
//        System.out.println(BatCharacteristic2);
//        BluetoothGattCharacteristic BatCharacteristic3 = variableService.getCharacteristic(UUIDs.UUID_TEST_DESCR);
//        System.out.println(BatCharacteristic3);
//        BluetoothGattCharacteristic BatCharacteristic4 = variableService.getCharacteristic(UUIDs.testBattery);
//        System.out.println(BatCharacteristic4);
    }
    private void getBatteryInfo() {
        System.out.println("getting battery");
        BluetoothGattCharacteristic BatCharacteristic = variableService.getCharacteristic(UUIDs.HEART_RATE_MEASUREMENT_CHARACTERISTIC);
        System.out.println(BatCharacteristic);
        BluetoothGattCharacteristic BatCharacteristic2 = variableService.getCharacteristic(UUIDs.UUID_CHARACTERISTIC_BATTERY_INFO);
        System.out.println(BatCharacteristic2);
        BluetoothGattCharacteristic BatCharacteristic3 = variableService.getCharacteristic(UUIDs.UUID_TEST_DESCR);
        System.out.println(BatCharacteristic3);
        BluetoothGattCharacteristic BatCharacteristic4 = variableService.getCharacteristic(UUIDs.testBattery);
        System.out.println(BatCharacteristic4);
//        BluetoothGattDescriptor BatDescriptor = BatCharacteristic.getDescriptor(UUIDs.UUID_TEST_DESCR);
//        System.out.println(BatDescriptor);
//        bluetoothGatt.setCharacteristicNotification(BatCharacteristic, true);
//        BatDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        bluetoothGatt.writeDescriptor(BatDescriptor);
    }
//    private void requestBatteryInfo(TransactionBuilder builder) {
//        System.out.println("Requesting Battery Info!");
//        BluetoothGattCharacteristic characteristic = getCharacteristic(HuamiService.UUID_CHARACTERISTIC_6_BATTERY_INFO);
//        builder.read(characteristic);
//        return this;
//    }
//    private void requestBatteryInfo(BluetoothGattDescriptor bluetoothGattDescriptor) {
//        System.out.println("Requesting Battery Info!");
//        UUID CHARACTERISTICUUID = bluetoothGattDescriptor.getCharacteristic().getUuid();
//        System.out.println(CHARACTERISTICUUID);
//        variableService = bluetoothGatt.getService(UUIDs.UUID_CHARACTERISTIC_6_BATTERY_INFO);
//        System.out.println(variableService);
//        //        BluetoothGattCharacteristic heartRateCharacteristic = variableService.getCharacteristic(UUIDs.HEART_RATE_MEASUREMENT_CHARACTERISTIC);
////        BluetoothGattDescriptor heartRateDescriptor = heartRateCharacteristic.getDescriptor(UUIDs.HEART_RATE_MEASURMENT_DESCRIPTOR);
////
////        bluetoothGatt.setCharacteristicNotification(heartRateCharacteristic, true);
////        heartRateDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
////        bluetoothGatt.writeDescriptor(heartRateDescriptor);
////        BluetoothGattCharacteristic characteristic = getCharacteristic(HuamiService.UUID_CHARACTERISTIC_6_BATTERY_INFO);
////        builder.read(characteristic);
////        return this;
//    }
    public void requestBatteryInfo() {
        System.out.println("* Getting gatt service, UUID:" + UUIDs.UUID_SERVICE_MIBAND_SERVICE);
        BluetoothGattService myGatService = bluetoothGatt.getService(UUIDs.UUID_SERVICE_MIBAND_SERVICE);
        if (myGatService != null) {
            System.out.println("* Getting gatt Characteristic. UUID: " + UUIDs.UUID_CHARACTERISTIC_6_BATTERY_INFO);
            BluetoothGattCharacteristic myGatChar = myGatService.getCharacteristic(UUIDs.UUID_CHARACTERISTIC_6_BATTERY_INFO);
            if (myGatChar != null) {
                System.out.println("* Statring listening");
                // second parametes is for starting\stopping the listener.
                boolean status =  bluetoothGatt.setCharacteristicNotification(myGatChar, true);
                System.out.println("* Set notification status :" + status);
            }else{
                System.out.println("myGatt ch is null");
            }
        }else{
            System.out.println("myGatt serivce is null");
        }
    }
}
