package cat.uab.falldetectionapp.com.falldetection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class mainView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);
        Intent intent = getIntent();
        String value = intent.getStringExtra("key"); //if it's a string you stored.
        System.out.println("***********"+ value);
    }
//    @Override
//    public void onBackPressed() {
//        moveTaskToBack(true);
//        System.out.println("*********** Mur");
//        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//    }
}
