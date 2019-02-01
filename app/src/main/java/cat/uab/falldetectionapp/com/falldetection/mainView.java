package cat.uab.falldetectionapp.com.falldetection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothGattCharacteristic;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.jjoe64.graphview.GraphView;

import org.w3c.dom.Text;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cat.uab.falldetectionapp.com.falldetection.listeners.NotifyListener;
import cat.uab.falldetectionapp.com.falldetection.model.BatteryInfo;

public class mainView extends AppCompatActivity{
    Button authentication_btn, DiscoverButton, activate_detection, device_info, kill_app;
    ListView list, lvNewDevices, list_paired;
    TextView status, batteryPercent, threshold, heartRate;
    BluetoothAdapter bluetoothAdapter;
    private DrawerLayout dl;
    RelativeLayout postConnectionLayout;
    private ActionBarDrawerToggle t;
    private NavigationView nv;
    private MiBand miband;
    private boolean connectionChecker = false, activate_disactivate = true;
    private static final String TAG = "MainActivity";
    public double y = 0.0;
    Switch plot;
    GraphView mScatterPlot;
    LineChart  mChart;
    Thread thread, threadCount;
    Boolean plotData = false;
    Boolean showPlot = false;
    Boolean show_dialog = true;
    SeekBar seekBar;
    Integer detect_threshold = 15;
    String countDownValue = "10";
    Integer timeCount = 10;

    private AlertDialog alertDialog;

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
        postConnectionLayout = findViewById(R.id.postConnectionLayout);
        postConnectionLayout.setVisibility(View.GONE);
        mChart = findViewById(R.id.chart);
        miband = new MiBand(this, mScatterPlot);
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
                    activate_detection.setText("fall detection running..");
                    activate_detection.setTextColor(Color.parseColor("#00b22f"));
                    miband.sensorData(activate_disactivate, new NotifyListener() {
                        @Override
                        public void onNotify(byte[] data) {
                            handleSensorData(data);
                        }
                    });
                }else{
                    activate_detection.setText("fall detection deactivated");
                    activate_detection.setTextColor(Color.parseColor("#000000"));
                    miband.sensorData(activate_disactivate, new NotifyListener() {
                        @Override
                        public void onNotify(byte[] data) {
                            System.out.println(data);
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
                showDeviceInfos();

            }
        });

        kill_app = findViewById(R.id.closeId);
        kill_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appExit();
            }
        });

        threshold = findViewById(R.id.threshold);
        seekBar = findViewById(R.id.seekBar);
        plot = findViewById(R.id.plot);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if(progressChangedValue < 15){
                    progressChangedValue = 15;
                }
                setTextContect(threshold, Integer.toString(progressChangedValue));
                detect_threshold = progressChangedValue;
                //Toast.makeText(mainView.this, "Dete is :" + progressChangedValue, Toast.LENGTH_SHORT).show();
            }
        });

        plot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    showPlot = true;
                }else{
                    showPlot = false;
                }
            }
        });
        heartRate = findViewById(R.id.heartRate);



        mChart.getDescription().setEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(30f);
        leftAxis.setAxisMinimum(-30f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.setDrawBorders(false);
        startTimeThread();

    }
    public void appExit () {
        this.finish();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void startTimeThread(){
        if (thread != null){
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true){
                    plotData = true;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
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

    private void setTextContect(final TextView t, final String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                t.setText(text);
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


    private void addEntry(Float x, Float y, Float z, Double result) {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            ILineDataSet setY = data.getDataSetByIndex(1);
            ILineDataSet setZ = data.getDataSetByIndex(1);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet("X", Color.RED);
                setY = createSet("Y", Color.GREEN);
                setZ = createSet("Z", Color.BLUE);
                data.addDataSet(set);
                data.addDataSet(setY);
                data.addDataSet(setZ);
            }

            try {
                data.addEntry(new Entry(set.getEntryCount(), x), 0);
                data.addEntry(new Entry(set.getEntryCount(), y), 1);
                data.addEntry(new Entry(set.getEntryCount(), z), 2);
                data.notifyDataChanged();
                // let the chart know it's data has changed
                mChart.notifyDataSetChanged();
                // limit the number of visible entries
                mChart.setVisibleXRangeMaximum(20);
                // move to the latest entry
                mChart.moveViewToX(data.getEntryCount());
            }catch (Exception e){
                System.out.println(e);
            }

        }

    }

    private LineDataSet createSet(String name, Integer c) {

        LineDataSet set = new LineDataSet(null, name);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(c);
        set.setHighlightEnabled(false);
        set.setDrawValues(true);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }
    public void showToastMethod() {
        System.out.println("called");
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainView.this);
        builder.setTitle("Time");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
                // call function show alert dialog again
                showToastMethod();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    private void handleSensorData(byte[] value) {
        int counter=0, step=0;
        double xAxis=0.0, yAxis=0.0, zAxis=0.0;
        double scale_factor = 250.0;
        //double gravity = 9.81;
        double gravity = 10;

        if ((value.length - 2) % 6 != 0) {
            System.out.println("wrong value");
        }
        else {
            counter = (value[0] & 0xff) | ((value[1] & 0xff) << 8);
            for (int idx = 0; idx < ((value.length - 2) / 6); idx++) {
                step = idx * 6;
                // Analyse X-axis data
                int xAxisRawValue = (value[step+2] & 0xff) | ((value[step+3] & 0xff) << 8);
                int xAxisSign = (value[step+3] & 0x30) >> 4;
                if (xAxisSign == 0) {
                    xAxis = xAxisRawValue & 0xfff;
                }
                else {
                    xAxis = (xAxisRawValue & 0xfff) - 4097;
                }
                xAxis = (xAxis*1.0 / scale_factor) * gravity;
                // Analyse Y-axis data
                int yAxisRawValue = (value[step+4] & 0xff) | ((value[step+5] & 0xff) << 8);
                int yAxisSign = (value[step+5] & 0x30) >> 4;
                if (yAxisSign == 0) {
                    yAxis = yAxisRawValue & 0xfff;
                }
                else {
                    yAxis = (yAxisRawValue & 0xfff) - 4097;
                }
                yAxis = (yAxis / scale_factor) * gravity;
                // Analyse Z-axis data
                int zAxisRawValue = (value[step+6] & 0xff) | ((value[step+7] & 0xff) << 8);
                int zAxisSign = (value[step+7] & 0x30) >> 4;
                if (zAxisSign == 0) {
                    zAxis = zAxisRawValue & 0xfff;
                }
                else {
                    zAxis = (zAxisRawValue & 0xfff) - 4097;
                }
                zAxis = (zAxis / scale_factor) * gravity;

                //System.out.println("x-axis:"+ String.format("%.03f",xAxis-2)+" y-axis:"+String.format("%.03f",yAxis-1)+" z-axis:"+String.format("%.03f",zAxis-1)+";");
                double result = Math.sqrt(Math.pow(Math.abs(xAxis-1), 2) + Math.pow(Math.abs(yAxis-1), 2) + Math.pow(Math.abs(zAxis-1), 2));
                //System.out.println(result);
                float x = (float) xAxis-1;
                float y = (float) yAxis-1;
                float z = (float) zAxis-1;
                x = ensureRange(x);
                y = ensureRange(y);
                z = ensureRange(z);
                if(plotData && showPlot){
                    addEntry(x, y, z, result);
                    plotData = false;
                }
                //System.out.println(result);
                if(result > detect_threshold && show_dialog){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            AlertDialog dialog = new AlertDialog.Builder(mainView.this)
                                    .setTitle("Fall Detected")
                                    .setMessage("was it wrong alert?")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            show_dialog = true;
                                            System.out.println("all is fine");
                                        }
                                    })
                                    .create();
                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                private static final int AUTO_DISMISS_MILLIS = 5000;
                                @Override
                                public void onShow(final DialogInterface dialog) {
                                    final Button defaultButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                                    final CharSequence positiveButtonText = defaultButton.getText();
                                    new CountDownTimer(AUTO_DISMISS_MILLIS, 100) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                            defaultButton.setText(String.format(
                                                    Locale.getDefault(), "%s (%d)",
                                                    positiveButtonText,
                                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1 //add one so it never displays zero
                                            ));
                                        }
                                        @Override
                                        public void onFinish() {
                                            if (((AlertDialog) dialog).isShowing()) {
                                                show_dialog = true;
                                                dialog.dismiss();
                                                System.out.println("alert! man is dying!!!");
                                                Vibrator vibrator = (Vibrator) mainView.this.getSystemService(Context.VIBRATOR_SERVICE);
                                                vibrator.vibrate(1000);
                                                miband.heartRate(new NotifyListener() {
                                                    @Override
                                                    public void onNotify(byte[] data) {
                                                        for (byte i: data){
                                                            System.out.println("heart data "+ i);
                                                            setTextContect(heartRate, "Heart rate: "+String.valueOf(i));
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }.start();
                                }
                            });
                            dialog.show();
                        }
                    });
                    show_dialog = false;
                }
            }
        }
    }
    float ensureRange(float value) {
        if(value > -1 && value < 1){
            return 1;
        }
        return value;
    }

    public static void setHeartRate(){

    }

}
