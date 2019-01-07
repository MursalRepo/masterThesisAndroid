package cat.uab.falldetectionapp.com.falldetection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class splashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Thread splash_screen = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(2000);
                    Intent intent = new Intent(getApplicationContext(), mainView.class);
                    startActivity(intent);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                finish();
            }
        };
        splash_screen.start();
    }
}
