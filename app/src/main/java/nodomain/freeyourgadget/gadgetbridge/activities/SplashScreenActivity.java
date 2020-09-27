package nodomain.freeyourgadget.gadgetbridge.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import java.util.ResourceBundle;

import nodomain.freeyourgadget.gadgetbridge.R;

public class SplashScreenActivity extends AppCompatActivity {
    private Handler loadingHandler;
    private static final int loading_time = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        loadingHandler = new Handler();
        loadingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this, ControlCenterv2.class));
                finish();
            }
        }, loading_time);
    }

    @Override
    protected void onDestroy() {
        loadingHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}