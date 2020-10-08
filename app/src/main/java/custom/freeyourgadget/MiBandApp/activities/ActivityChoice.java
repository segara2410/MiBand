package custom.freeyourgadget.MiBandApp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.button.MaterialButton;

import custom.freeyourgadget.MiBandApp.R;
import custom.freeyourgadget.MiBandApp.activities.polar.PolarActivity;

public class ActivityChoice extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        Button miband = findViewById(R.id.mi_band);
        Button polarband = findViewById(R.id.polar_band);

        miband.setOnClickListener(v -> {
            startActivity(new Intent(ActivityChoice.this, ControlCenterv2.class));
        });

        polarband.setOnClickListener(v -> {
            startActivity(new Intent(ActivityChoice.this, PolarActivity.class));
        });
    }
}