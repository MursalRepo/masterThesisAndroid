package cat.uab.falldetectionapp.com.falldetection;

import android.bluetooth.BluetoothAdapter;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.jjoe64.graphview.GraphView;

public class config extends AppCompatActivity {
    Button authentication_btn, activate_detection;
    ListView list;
    TextView status, threshold, heartRate, batteryText;
    SeekBar seekBar;
    Double detect_threshold = 2.0;
    ImageView lightIndigator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        threshold = findViewById(R.id.threshold);
        threshold.setText(mainView.detect_threshold.toString());
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(100);
        seekBar.setProgress(mainView.detect_threshold.intValue()*10);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            double progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double value = ((float)progress / 10.0);
                progressChangedValue = value;
                if(progressChangedValue < 2.0){
                    progressChangedValue = 2.0;
                }
                setTextContect(threshold, Double.toString(progressChangedValue));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if(progressChangedValue < 2.0){
                    progressChangedValue = 2.0;
                }
                setTextContect(threshold, Double.toString(progressChangedValue));
                //detect_threshold = progressChangedValue;
                mainView.detect_threshold = progressChangedValue;
                //Toast.makeText(mainView.this, "Dete is :" + progressChangedValue, Toast.LENGTH_SHORT).show();
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
