package cat.uab.falldetectionapp.com.falldetection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DiscoverDevice extends AppCompatActivity {
    public ListView DevicesList;
    public TextView DiscoverText;
    private MiBand miband;
    HashMap<String, BluetoothDevice> devices = new HashMap<String, BluetoothDevice>();
    private static final int REQUEST_ENABLE = 0;
    private static final int REQUEST_DISCOVERABLE = 0;
    public static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.discoverTitle);
        setContentView(R.layout.activity_discover_device);
        initialize();
    }

    private void initialize(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        DiscoverText = findViewById(R.id.DiscoverText);
        if (mBluetoothAdapter == null) {
            Toast.makeText(DiscoverDevice.this, "Your device does not support Bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                controlBluetoothStatus();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                Toast.makeText(DiscoverDevice.this, "Bluetooth is Disabled", Toast.LENGTH_LONG).show();
                                DiscoverText.setText(R.string.enableBT);
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Bluetooth is disabled, enable?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
            }else{
                controlBluetoothStatus();
            }
        }
    }

    private void controlBluetoothStatus(){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println(requestCode);
        showPairedAndNewDevices();
    }

    private void showPairedAndNewDevices(){
        DiscoverText.setText(R.string.discovering);
        DevicesList = findViewById(R.id.DevicesList);
        final ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.item, new ArrayList<String>());
        BluetoothAdapter adapterBL = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = adapterBL.getBondedDevices();
        for(BluetoothDevice bt : pairedDevices){
            String item = bt.getName() + ": " + bt.getAddress() + " paired";
            devices.put(item, bt);
            adapter.add(item);
        }
        DevicesList.setAdapter(adapter);

        final ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                System.out.println("name:" + device.getName() + ",uuid:"
                        + device.getUuids() + ",add:"
                        + device.getAddress() + ",type:"
                        + device.getType() + ",bondState:"
                        + device.getBondState() + ",rssi:" + result.getRssi());
                if (device.getName() != null) {
                    String item = device.getName() + ": " + device.getAddress() + " rssi: " + result.getRssi()+ " new";
                    String[] bits = item.split(" rssi:");
                    String uniqueKey = bits[0];
                    Boolean addNew = true;
                    for (String key : devices.keySet()) {
                        if(key.startsWith(uniqueKey)){
                             addNew = false;
                         }
                    }
                    if(addNew){
                        devices.put(item, device);
                        adapter.add(item);
                    }
                }
            }
        };
        MiBand.startScan(scanCallback);
        DevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();
                if (devices.containsKey(item)) {
                    MiBand.stopScan(scanCallback);
                    BluetoothDevice device = devices.get(item);
                    Intent intent = new Intent();
                    intent.putExtra("device", device);
                    intent.setClass(DiscoverDevice.this, mainView.class);
                    DiscoverDevice.this.startActivity(intent);
                    DiscoverDevice.this.finish();
                }
            }
        });
    }

}
