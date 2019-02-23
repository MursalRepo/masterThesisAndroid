package cat.uab.falldetectionapp.com.falldetection;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class config extends AppCompatActivity {
    Button authentication_btn, activate_detection, save_email;
    ListView list;
    TextView status, threshold, heartRate, batteryText, phone_acc_thr;
    SeekBar seekBar, phone_acc_seekbar;
    ImageView lightIndigator;
    Switch phone_acc_switch,dev_mode;
    EditText email_field;
    Context context = this;
    public static String test;
    private sqlite_IO db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final email_const e_c = new email_const();
        super.onCreate(savedInstanceState);
        db = new sqlite_IO(context);
        setContentView(R.layout.activity_config);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        threshold = findViewById(R.id.threshold);
        threshold.setText(mainView.detect_threshold.toString());
        phone_acc_switch = findViewById(R.id.phone_acc_switch);
        phone_acc_switch.setChecked(mainView.use_phone);

        dev_mode = findViewById(R.id.dev_mode);
        dev_mode.setChecked(mainView.dev_mode);

        phone_acc_thr = findViewById(R.id.phone_acc_thr);
        phone_acc_thr.setText(mainView.phone_threshold.toString());
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
                mainView.detect_threshold = progressChangedValue;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                //  Toast.makeText(mainView.this, "Dete is :" + progressChangedValue, Toast.LENGTH_SHORT).show();
            }
        });

        phone_acc_seekbar = findViewById(R.id.phone_acc_seekbar);
        phone_acc_seekbar.setMax(100);
        phone_acc_seekbar.setProgress(mainView.phone_threshold.intValue()*10);
        phone_acc_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            double progressChangedValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double value = ((float)progress / 10.0);
                progressChangedValue = value;
                if(progressChangedValue < 1.2){
                    progressChangedValue = 1.2;
                }
                setTextContect(phone_acc_thr, Double.toString(progressChangedValue));
                mainView.phone_threshold = progressChangedValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        phone_acc_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainView.use_phone = isChecked;
            }
        });

        dev_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainView.dev_mode = isChecked;
                mainView.visibility = isChecked;
            }
        });

        email_field = findViewById(R.id.email_field);
        save_email = findViewById(R.id.save_email);
        save_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String field_value = email_field.getText().toString();
                if(field_value.isEmpty()){
                    Toast.makeText(context, "Email can not be empty", Toast.LENGTH_LONG).show();
                }else {
                    e_c.setEmail(field_value);
                    e_c.setEmail_id(1);
                    String check = db.checkValues();
                    if (!check.isEmpty()) {
                        db.update_email(e_c);
                    } else {
                        db.add_email(e_c);
                    }
                    mainView.user_email = e_c.getEmail();
                    Toast.makeText(context, "Email saved!", Toast.LENGTH_LONG).show();
                }

            }
        });
        String check = db.checkValues();
        if(!check.isEmpty()){
            email_field.setText(check);
        }else{
            db.add_email(e_c);
        }

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
