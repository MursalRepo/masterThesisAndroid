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
    double y = 0.0;

    //make xyValueArray global
    private ArrayList<XYValue> xValueArray;
    private ArrayList<XYValue> yValueArray;
    private ArrayList<XYValue> zValueArray;

    static private ArrayList<XYValue> xValueArrayMi;
    private ArrayList<XYValue> yValueArrayMi;
    private ArrayList<XYValue> zValueArrayMi;
    private static final String TAG = "MainActivity";

    private LineChart mChart;
    private boolean plotData = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_diagram);
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

        mChart.setPinchZoom(true);

        mChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        mChart.setData(data);

        Legend l = mChart.getLegend();

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
        leftAxis.setAxisMaximum(6f);
        leftAxis.setAxisMinimum(-6f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.setDrawBorders(false);
    }
    private void addEntry(SensorEvent event) {
        double x = event.values[0]/9.81;
        double y = event.values[1]/9.81;
        double z = event.values[2]/9.81;
        double phone_result = Math.sqrt(Math.pow(Math.abs(x), 2) + Math.pow(Math.abs(y), 2) + Math.pow(Math.abs(z), 2));
        float plot_result = (float) phone_result;

        LineData data = mChart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet("X", Color.RED);
                data.addDataSet(set);
            }


            data.addEntry(new Entry(set.getEntryCount(), plot_result), 0);
            data.notifyDataChanged();

            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(20);
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

    }

}
