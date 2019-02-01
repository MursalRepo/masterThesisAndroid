package cat.uab.falldetectionapp.com.falldetection;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class realtime_diagram extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    double ax, ay, az;   // these are the acceleration in x,y and z axis
    PointsGraphSeries<DataPoint> xySeries;
    LineGraphSeries<DataPoint> xSeries, ySeries, zSeries;
    static LineGraphSeries<DataPoint> xSeriesMi, ySeriesMi, zSeriesMi;
    double continuous = 0.0;
    static double continuousMi = 0.0;
    double y = 0.0;
    static GraphView mScatterPlot, mScatterPlot2;

    //make xyValueArray global
    private ArrayList<XYValue> xValueArray;
    private ArrayList<XYValue> yValueArray;
    private ArrayList<XYValue> zValueArray;

    static private ArrayList<XYValue> xValueArrayMi;
    private ArrayList<XYValue> yValueArrayMi;
    private ArrayList<XYValue> zValueArrayMi;
    private static final String TAG = "MainActivity";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private  Sensor sensors;

    private LineChart mChart;
    private Thread thread;
    private boolean plotData = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_diagram);
//        mSensorManager = (SensorManager) getSystemService(realtime_diagram.SENSOR_SERVICE);
//
//        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//
//        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
//
//        for(int i=0; i<sensors.size(); i++){
//            Log.d(TAG, "onCreate: Sensor "+ i + ": " + sensors.get(i).toString());
//        }
//
//        if (mAccelerometer != null) {
//            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
//        }
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mChart = findViewById(R.id.chart2);
        xValueArray = new ArrayList<>();
        yValueArray = new ArrayList<>();
        zValueArray = new ArrayList<>();

        xValueArrayMi = new ArrayList<>();
        yValueArrayMi = new ArrayList<>();
        zValueArrayMi = new ArrayList<>();
        mChart =  findViewById(R.id.chart2);

        // enable description text
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
    }
    private void addEntry(SensorEvent event) {

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

//            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 80) + 10f), 0);
            data.addEntry(new Entry(set.getEntryCount(), event.values[0]), 0);
            data.addEntry(new Entry(set.getEntryCount(), event.values[1]), 1);
            data.addEntry(new Entry(set.getEntryCount(), event.values[2]), 2);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(20);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());
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


    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(plotData){
            addEntry(event);
        }
//        xSeries = new LineGraphSeries<>();
//        ySeries = new LineGraphSeries<>();
//        zSeries = new LineGraphSeries<>();
//
//        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
//            ax = event.values[0];
//            ay = event.values[1];
//            az = event.values[2];
//            continuous = continuous + 5;
//            xValueArray.add(new XYValue(continuous, ax));
//            for(int i = 0;i <xValueArray.size(); i++){
//                if (xValueArray.size() > 20){
//                    xValueArray.remove(0);
//                    xSeries.resetData(new DataPoint[] {});
//                }
//                try{
//                    double x = xValueArray.get(i).getX();
//                    double y = xValueArray.get(i).getY();
//                    xSeries.appendData(new DataPoint(x, y),true, 10);
//                }catch (IllegalArgumentException e){
//                    System.out.println("createScatterPlot: IllegalArgumentException: " + e.getMessage() );
//                }
//            }
//            yValueArray.add(new XYValue(continuous, ay));
//            for(int i = 0;i <yValueArray.size(); i++){
//                if (yValueArray.size() > 20){
//                    yValueArray.remove(0);
//                    ySeries.resetData(new DataPoint[] {});
//                }
//                try{
//                    double x = yValueArray.get(i).getX();
//                    double y = yValueArray.get(i).getY();
//                    ySeries.appendData(new DataPoint(x, y),true, 10);
//                }catch (IllegalArgumentException e){
//                    System.out.println("createScatterPlot: IllegalArgumentException: " + e.getMessage() );
//                }
//            }
//            zValueArray.add(new XYValue(continuous, az));
//            for(int i = 0;i <zValueArray.size(); i++){
//                if (zValueArray.size() > 20){
//                    zValueArray.remove(0);
//                    zSeries.resetData(new DataPoint[] {});
//                }
//                try{
//                    double x = zValueArray.get(i).getX();
//                    double y = zValueArray.get(i).getY();
//                    zSeries.appendData(new DataPoint(x, y),true, 10);
//                }catch (IllegalArgumentException e){
//                    System.out.println("createScatterPlot: IllegalArgumentException: " + e.getMessage() );
//                }
//            }
//
//            //set some properties
//            //xySeries.setShape(PointsGraphSeries.Shape.POINT);
//            xSeries.setColor(Color.RED);
//            ySeries.setColor(Color.GREEN);
//            zSeries.setColor(Color.BLUE);
//            //xySeries.setSize(5f);
//
//            //set Scrollable and Scaleable
//            mScatterPlot.getViewport().setScalable(true);
//            mScatterPlot.getViewport().setScalableY(true);
//            mScatterPlot.getViewport().setScrollable(true);
//            mScatterPlot.getViewport().setScrollableY(true);
//
//            //set manual x bounds
//            //mScatterPlot.getViewport().setYAxisBoundsManual(true);
//            mScatterPlot.getViewport().setMaxY(60);
//            mScatterPlot.getViewport().setMinY(-60);
//
//            //set manual y bounds
//            //mScatterPlot.getViewport().setXAxisBoundsManual(true);
//            mScatterPlot.getViewport().setMaxX(100 + continuous);
//            mScatterPlot.getViewport().setMinX(-100 + continuous);
//
//            mScatterPlot.addSeries(xSeries);
//            mScatterPlot.addSeries(ySeries);
//            mScatterPlot.addSeries(zSeries);
//        }
    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
//    }


}
