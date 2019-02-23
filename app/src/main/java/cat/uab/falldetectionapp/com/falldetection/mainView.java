package cat.uab.falldetectionapp.com.falldetection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cat.uab.falldetectionapp.com.falldetection.listeners.NotifyListener;
import cat.uab.falldetectionapp.com.falldetection.model.BatteryInfo;

public class mainView extends AppCompatActivity implements SensorEventListener {
    Button authentication_btn, DiscoverButton, activate_detection, save_email;
    ListView list;
    private int custom_heart_rate = 60;
    RelativeLayout dev_mode_layout;
    TextView status, threshold, heartRate, batteryText, phone_acc_thr;
    BluetoothAdapter bluetoothAdapter;
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;
    private MiBand miband;
    private boolean connectionChecker = false, activate_disactivate = true;
    private static final String TAG = "MainActivity";
    public double y = 0.0;
    public static String user_email = "";
    Switch plot;
    GraphView mScatterPlot;
    LineChart  mChart;
    Thread thread, threadCount;
    Boolean plotData = true;
    Boolean showPlot = false;
    Boolean show_dialog = true;
    SeekBar seekBar, phone_acc_seekbar;
    public static Double detect_threshold = 2.0;
    public static Double phone_threshold = 1.2;
    public double phone_result = 1.2;
    ImageView lightIndigator;
    private SensorManager sensorManager;
    public static boolean use_phone = false;
    public static boolean dev_mode = false, visibility = true;
    EditText email_field;
    Context context = this;
    private sqlite_IO db;
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
        String pass = getResources().getString(R.string.pass);
        db = new sqlite_IO(context);
        String check = db.checkValues();
        user_email = check;
        batteryText = findViewById(R.id.batteryText);
        mChart = findViewById(R.id.chart);
        miband = new MiBand(this, mScatterPlot);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
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
                    setIndigator(R.drawable.circleyellow);
                    connectionChecker = true;
                    miband.setDisconnectedListener(new NotifyListener() {
                        @Override
                        public void onNotify(byte[] data) {
                            setIndigator(R.drawable.circleblack);
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
        dev_mode_layout = findViewById(R.id.dev_mode_layout);

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
                    case R.id.configuration:
                        intent.setClass(mainView.this, config.class);
                        mainView.this.startActivity(intent);
                        break;
                    case R.id.account:
                        Toast.makeText(mainView.this, "Master thesis UAB Mursal Sheydayev",Toast.LENGTH_LONG).show();
                        break;
                    case R.id.shut_down:
                        miband.disconnect();
                        appExit();
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
                    setIndigator(R.drawable.circlegreen);
                    miband.sensorData(activate_disactivate, new NotifyListener() {
                        @Override
                        public void onNotify(byte[] data) {
                            if(dev_mode && visibility){
                                makeLayoutVisible(dev_mode_layout, View.VISIBLE);
                                visibility = false;
                            }
                            if(!dev_mode && !visibility){
                                makeLayoutVisible(dev_mode_layout, View.GONE);
                                visibility = true;
                            }
                            handleSensorData(data);
                        }
                    });
                }else{
                    activate_detection.setText("fall detection deactivated");
                    setIndigator(R.drawable.circleyellow);
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

        plot = findViewById(R.id.plot);

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
        lightIndigator = findViewById(R.id.lightIndigator);
        threshold = findViewById(R.id.threshold);
        phone_acc_thr = findViewById(R.id.phone_acc_thr);


        mChart.getDescription().setEnabled(false);

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
        l.setEnabled(false);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(6f);
        leftAxis.setAxisMinimum(-6f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.setDrawBorders(false);
        startTimeThread();
        startHeartRateThread();

    }
    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[0]/9.81;
        double y = event.values[1]/9.81;
        double z = event.values[2]/9.81;
        phone_result = Math.sqrt(Math.pow(Math.abs(x), 2) + Math.pow(Math.abs(y), 2) + Math.pow(Math.abs(z), 2));
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
                    try {
                        Thread.sleep(60000);
                        showDeviceInfos();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }
    public void startHeartRateThread(){
        if (threadCount != null){
            threadCount.interrupt();
        }
        threadCount = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        saveHeartRate();
                        Thread.sleep(300000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
        threadCount.start();
    }

    public void makePanelVisible(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activate_detection.setVisibility(View.VISIBLE);
            }
        });
    }

    public void makeLayoutVisible(final RelativeLayout l, final int val){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                l.setVisibility(val);
            }
        });
    }

    public void setIndigator(final Integer res){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lightIndigator.setImageResource(res);
            }
        });
    }

    public void showDeviceInfos(){
        miband.getBatteryInfo(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                BatteryInfo info = (BatteryInfo) data;
                setBattery(String.valueOf(info.getLevel()));
                System.out.println(String.valueOf(info.getLevel()));
            }

            @Override
            public void onFail(int errorCode, String msg) {
                System.out.println("getBatteryInfo fail");
            }
        });

    }

    public void saveHeartRate(){
        miband.heartRate(1, new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                for (byte i: data){
                    System.out.println("heart data "+ i);
                    if(i > 0){
                        setTextContect(heartRate, "Heart rate: "+String.valueOf(i));
                        custom_heart_rate = i;
                    }
                }
            }
        });
    }

    private void setBattery(final String battery){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                batteryText.setText("Battery: "+battery+"%");
                batteryText.setVisibility(View.VISIBLE);

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


    private void addEntry(Float x, Float y, Float z, Double result) {

        try {
            LineData data = mChart.getData();

            if (data != null) {

                ILineDataSet set = data.getDataSetByIndex(0);
                ILineDataSet setY = data.getDataSetByIndex(0);
                ILineDataSet setZ = data.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet("X", Color.RED);
                    setY = createSet("Y", Color.GREEN);
                    setZ = createSet("Z", Color.BLUE);
                    data.addDataSet(set);
                    data.addDataSet(setY);
                    data.addDataSet(setZ);
                }
                data.addEntry(new Entry(set.getEntryCount(), x), 0);
                data.addEntry(new Entry(set.getEntryCount(), y), 1);
                data.addEntry(new Entry(set.getEntryCount(), z), 2);
                data.notifyDataChanged();
                mChart.notifyDataSetChanged();
                mChart.setVisibleXRangeMaximum(150);
                try {
                    mChart.moveViewToX(data.getEntryCount());
                }catch (Exception e){
                    System.out.println(e);
                }

            }
        }catch (Exception e){
            System.out.println(e);
        }



    }

    private LineDataSet createSet(String name, Integer c) {
        LineDataSet set = new LineDataSet(null, name);
        try {
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setLineWidth(1f);
            set.setColor(c);
            set.setHighlightEnabled(false);
            set.setDrawValues(true);
            set.setDrawCircles(false);
            set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set.setCubicIntensity(0.2f);
        }catch (Exception e){
            System.out.println(e);
        }

        return set;

    }

    private void handleSensorData(byte[] value) {
        int counter=0, step=0;
        double xAxis=0.0, yAxis=0.0, zAxis=0.0;
        double scale_factor = 250.0;
        //double gravity = 9.81;
        double gravity = 1;

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

                double result = Math.sqrt(Math.pow(Math.abs(xAxis), 2) + Math.pow(Math.abs(yAxis), 2) + Math.pow(Math.abs(zAxis), 2));
                float x = (float) xAxis;
                float y = (float) yAxis;
                float z = (float) zAxis;
                x = ensureRange(x);
                y = ensureRange(y);
                z = ensureRange(z);
                if(plotData && showPlot && dev_mode){
                    addEntry(x, y, z, result);
                }
                Boolean detect_condition = result > detect_threshold && show_dialog;
                if(use_phone){
                    detect_condition = result > detect_threshold && phone_result > phone_threshold && show_dialog;
                }
                if(detect_condition){
                    Vibrator vibrator = (Vibrator) mainView.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(2000);
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
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                            dialog.setCancelable(false);
                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                private static final int AUTO_DISMISS_MILLIS = 15000;
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
                                                miband.heartRate(0, new NotifyListener() {
                                                    @Override
                                                    public void onNotify(byte[] data) {
                                                        sendEmail("Heart rate: measuring...");
                                                        setTextContect(heartRate, "Heart rate: ...");
                                                        for (byte i: data){
                                                            System.out.println("heart data "+ i);
                                                            if(i > 0){
                                                                int diff = ((i * 100)/custom_heart_rate - 100);
                                                                if(diff > 0){
                                                                    sendEmail("Heart rate is "+diff+"% higher than normal: "+String.valueOf(i));
                                                                }else{
                                                                    sendEmail("Heart rate is "+diff+"% lower than normal: "+String.valueOf(i));
                                                                }
                                                                setTextContect(heartRate, "Heart rate: "+String.valueOf(i));
                                                            }
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

    protected void sendEmail(String body) {
        String pass = getResources().getString(R.string.pass);
        try {
            GMailSender sender = new GMailSender("falldetectionemergency@gmail.com", pass);
            sender.sendMail("Man is down!!!",
                    body,
                    "falldetectionemergency@gmail.com",
                    user_email);
        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }
    }

}
