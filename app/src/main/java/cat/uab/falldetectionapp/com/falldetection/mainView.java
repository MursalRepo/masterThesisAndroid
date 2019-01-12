package cat.uab.falldetectionapp.com.falldetection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import cat.uab.falldetectionapp.com.falldetection.listeners.NotifyListener;
import cat.uab.falldetectionapp.com.falldetection.model.BatteryInfo;

public class mainView extends AppCompatActivity{
    Button authentication_btn, DiscoverButton, activate_detection, device_info;
    ListView list, lvNewDevices, list_paired;
    TextView status, batteryPercent;
    BluetoothAdapter bluetoothAdapter;

    private DrawerLayout dl;
    RelativeLayout postConnectionLayout;
    private ActionBarDrawerToggle t;
    private NavigationView nv;
    private MiBand miband;
    private boolean connectionChecker = false, activate_disactivate = true;
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
    private static Timer timer;
    private TimerTask timertask;
    public LineGraphSeries<DataPoint> xSeries, ySeries, zSeries;
    public double continuous = 0.0;
    public double y = 0.0;
    private GraphView mScatterPlot;
    private SensorManager sensorManager;
    private double ax, ay, az;   // these are the acceleration in x,y and z axis

    //make xyValueArray global
    private ArrayList<XYValue> xValueArray;
    private ArrayList<XYValue> yValueArray;
    private ArrayList<XYValue> zValueArray;


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);
        final Intent intent = this.getIntent();
        timer = new Timer();
        postConnectionLayout = findViewById(R.id.postConnectionLayout);
        postConnectionLayout.setVisibility(View.GONE);
        miband = new MiBand(this);
        mScatterPlot = findViewById(R.id.scatterPlotBand);
        xValueArray = new ArrayList<>();
        yValueArray = new ArrayList<>();
        zValueArray = new ArrayList<>();
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
                    case R.id.real_time_graph:
                        intent.setClass(mainView.this, realtime_diagram.class);
                        mainView.this.startActivity(intent);
                        break;
                    case R.id.account:
                        Toast.makeText(mainView.this, "Developer Info",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        return true;
                }
            return true;
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
                            makePanelVisible();
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

        activate_detection = findViewById(R.id.activate_detection);
        activate_detection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activate_disactivate){
                    activate_detection.setText("fall detection activated");
                    activate_detection.setTextColor(Color.parseColor("#00b22f"));
                    miband.sensorData(activate_disactivate,  new ActionCallback() {
                        @Override
                        public void onSuccess(Object data) {
                            System.out.println("successfully activated");
                        }
                        @Override
                        public void onFail(int errorCode, String msg) {
                            System.out.println("activation fail");
                        }
                    });
                }else{
                    activate_detection.setText("fall detection deactivated");
                    activate_detection.setTextColor(Color.parseColor("#000000"));
                    miband.sensorData(activate_disactivate,  new ActionCallback() {
                        @Override
                        public void onSuccess(Object data) {
                            System.out.println("successfully deactivated");
                        }
                        @Override
                        public void onFail(int errorCode, String msg) {
                            System.out.println("deactivation fail");
                        }
                    });
                }
                activate_disactivate = !activate_disactivate;

            }
        });
        device_info = findViewById(R.id.device_info);
        device_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showDeviceInfos();
                miband.heartRate();
            }
        });

    }

    public void makePanelVisible(){
        batteryPercent = findViewById(R.id.batteryPercent);
        device_info = findViewById(R.id.device_info);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activate_detection.setVisibility(View.VISIBLE);
                device_info.setVisibility(View.VISIBLE);
            }
        });
    }

    public void showDeviceInfos(){
        miband.getBatteryInfo(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                BatteryInfo info = (BatteryInfo) data;
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

    private void setTexts(final Button button, final String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setText(text);
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

    public void plot_mi_band(Double x_value, Double y_value, Double z_value){
        System.out.println("x-axis:"+ x_value+" y-axis:"+y_value+" z-axis:"+z_value+";");
        System.out.println(mScatterPlot);
        xSeries = new LineGraphSeries<>();
        ySeries = new LineGraphSeries<>();
        zSeries = new LineGraphSeries<>();
//
//        continuous = continuous + 5;
//        xValueArray.add(new XYValue(continuous, x_value));
//        for(int i = 0;i <xValueArray.size(); i++){
//            if (xValueArray.size() > 20){
//                xValueArray.remove(0);
//                xSeries.resetData(new DataPoint[] {});
//            }
//            try{
//                double x = xValueArray.get(i).getX();
//                double y = xValueArray.get(i).getY();
//                xSeries.appendData(new DataPoint(x, y),true, 10);
//            }catch (IllegalArgumentException e){
//                System.out.println("createScatterPlot: IllegalArgumentException: " + e.getMessage() );
//            }
//        }
//        yValueArray.add(new XYValue(continuous, y_value));
//        for(int i = 0;i <yValueArray.size(); i++){
//            if (yValueArray.size() > 20){
//                yValueArray.remove(0);
//                ySeries.resetData(new DataPoint[] {});
//            }
//            try{
//                double x = yValueArray.get(i).getX();
//                double y = yValueArray.get(i).getY();
//                ySeries.appendData(new DataPoint(x, y),true, 10);
//            }catch (IllegalArgumentException e){
//                System.out.println("createScatterPlot: IllegalArgumentException: " + e.getMessage() );
//            }
//        }
//        zValueArray.add(new XYValue(continuous, z_value));
//        for(int i = 0;i <zValueArray.size(); i++){
//            if (zValueArray.size() > 20){
//                zValueArray.remove(0);
//                zSeries.resetData(new DataPoint[] {});
//            }
//            try{
//                double x = zValueArray.get(i).getX();
//                double y = zValueArray.get(i).getY();
//                zSeries.appendData(new DataPoint(x, y),true, 10);
//            }catch (IllegalArgumentException e){
//                System.out.println("createScatterPlot: IllegalArgumentException: " + e.getMessage() );
//            }
//        }
//
//        //set some properties
//        //xySeries.setShape(PointsGraphSeries.Shape.POINT);
//        xSeries.setColor(Color.RED);
//        ySeries.setColor(Color.GREEN);
//        zSeries.setColor(Color.BLUE);
//        //xySeries.setSize(5f);
//
//        //set Scrollable and Scaleable
//        mScatterPlot.getViewport().setScalable(true);
//        mScatterPlot.getViewport().setScalableY(true);
//        mScatterPlot.getViewport().setScrollable(true);
//        mScatterPlot.getViewport().setScrollableY(true);
//
//        //set manual x bounds
//        //mScatterPlot.getViewport().setYAxisBoundsManual(true);
//        mScatterPlot.getViewport().setMaxY(60);
//        mScatterPlot.getViewport().setMinY(-60);
//
//        //set manual y bounds
//        //mScatterPlot.getViewport().setXAxisBoundsManual(true);
//        mScatterPlot.getViewport().setMaxX(100 + continuous);
//        mScatterPlot.getViewport().setMinX(-100 + continuous);
//
//        mScatterPlot.addSeries(xSeries);
//        mScatterPlot.addSeries(ySeries);
//        mScatterPlot.addSeries(zSeries);
    }

}
