package cat.uab.falldetectionapp.com.falldetection;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;

public class realtime_diagram extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    double ax, ay, az;   // these are the acceleration in x,y and z axis
    PointsGraphSeries<DataPoint> xySeries;
    LineGraphSeries<DataPoint> xSeries, ySeries, zSeries;
    double continuous = 0.0;
    double y = 0.0;
    GraphView mScatterPlot;

    //make xyValueArray global
    private ArrayList<XYValue> xValueArray;
    private ArrayList<XYValue> yValueArray;
    private ArrayList<XYValue> zValueArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_diagram);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mScatterPlot = (GraphView) findViewById(R.id.scatterPlot);
        xValueArray = new ArrayList<>();
        yValueArray = new ArrayList<>();
        zValueArray = new ArrayList<>();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        xSeries = new LineGraphSeries<>();
        ySeries = new LineGraphSeries<>();
        zSeries = new LineGraphSeries<>();

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
            continuous = continuous + 5;
            xValueArray.add(new XYValue(continuous, ax));
            for(int i = 0;i <xValueArray.size(); i++){
                if (xValueArray.size() > 20){
                    xValueArray.remove(0);
                    xSeries.resetData(new DataPoint[] {});
                }
                try{
                    double x = xValueArray.get(i).getX();
                    double y = xValueArray.get(i).getY();
                    xSeries.appendData(new DataPoint(x, y),true, 10);
                }catch (IllegalArgumentException e){
                    System.out.println("createScatterPlot: IllegalArgumentException: " + e.getMessage() );
                }
            }
            yValueArray.add(new XYValue(continuous, ay));
            for(int i = 0;i <yValueArray.size(); i++){
                if (yValueArray.size() > 20){
                    yValueArray.remove(0);
                    ySeries.resetData(new DataPoint[] {});
                }
                try{
                    double x = yValueArray.get(i).getX();
                    double y = yValueArray.get(i).getY();
                    ySeries.appendData(new DataPoint(x, y),true, 10);
                }catch (IllegalArgumentException e){
                    System.out.println("createScatterPlot: IllegalArgumentException: " + e.getMessage() );
                }
            }
            zValueArray.add(new XYValue(continuous, az));
            for(int i = 0;i <zValueArray.size(); i++){
                if (zValueArray.size() > 20){
                    zValueArray.remove(0);
                    zSeries.resetData(new DataPoint[] {});
                }
                try{
                    double x = zValueArray.get(i).getX();
                    double y = zValueArray.get(i).getY();
                    zSeries.appendData(new DataPoint(x, y),true, 10);
                }catch (IllegalArgumentException e){
                    System.out.println("createScatterPlot: IllegalArgumentException: " + e.getMessage() );
                }
            }

            //set some properties
            //xySeries.setShape(PointsGraphSeries.Shape.POINT);
            xSeries.setColor(Color.RED);
            ySeries.setColor(Color.GREEN);
            zSeries.setColor(Color.BLUE);
            //xySeries.setSize(5f);

            //set Scrollable and Scaleable
            mScatterPlot.getViewport().setScalable(true);
            mScatterPlot.getViewport().setScalableY(true);
            mScatterPlot.getViewport().setScrollable(true);
            mScatterPlot.getViewport().setScrollableY(true);

            //set manual x bounds
            //mScatterPlot.getViewport().setYAxisBoundsManual(true);
            mScatterPlot.getViewport().setMaxY(60);
            mScatterPlot.getViewport().setMinY(-60);

            //set manual y bounds
            //mScatterPlot.getViewport().setXAxisBoundsManual(true);
            mScatterPlot.getViewport().setMaxX(100 + continuous);
            mScatterPlot.getViewport().setMinX(-100 + continuous);

            mScatterPlot.addSeries(xSeries);
            mScatterPlot.addSeries(ySeries);
            mScatterPlot.addSeries(zSeries);
        }
    }

    public void mi_band_plot(){
        //scatterPlotBand
    }

}
