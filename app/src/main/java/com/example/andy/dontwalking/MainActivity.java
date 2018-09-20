package com.example.andy.dontwalking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Runnable, SensorEventListener {

    SensorManager sm;
    TextView tv;
    Handler h;
    float gx, gy, gz;
    float accel;
    float accel1, accel2, accel3;

    static public String downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";

    private static final int REQUEST_EXTERNAL_STORAGE_CODE = 0x01;
    private static String[] mPermissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println(downloadsPath);
        verifyStoragePermissions(this);

        tv = findViewById(R.id.text);

        h = new Handler();
        h.postDelayed(this, 500);

        Button standButton = (Button)findViewById(R.id.button_stand);
        standButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFile("stand.csv", String.valueOf(accel));
            }
        });

        Button walkButton = (Button)findViewById(R.id.button_walk);
        walkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFile("walk.csv", String.valueOf(accel));
            }
        });

        Button runButton = (Button)findViewById(R.id.button_run);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFile("run.csv", String.valueOf(accel));
            }
        });
    }

    public void saveFile(String filename, String str) {

        try {
            FileWriter file = new FileWriter(downloadsPath + filename);
            PrintWriter pw = new PrintWriter(new BufferedWriter(file));

            pw.println(str);

            pw.close();

        } catch (IOException e) {
            System.out.println("ファイルが拓けませんでした。");
            e.printStackTrace();
        }

    }

    @SuppressLint("SetTextI18n")
    public void run() {
        tv.setText("X-axis : " + gx + "\n"
                + "Y-axis : " + gy + "\n"
                + "Z-axis : " + gz + "\n"
                + "accel : " + accel + "\n"
                + "downloadsPath : " + downloadsPath + "\n");
        h.postDelayed(this, 500);
    }

    protected void onResume() {
        super.onResume();
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors =
                sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (0 < sensors.size()) {
            sm.registerListener(this, sensors.get(0),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }

    protected void onDestroy() {
        super.onDestroy();
        h.removeCallbacks(this);
    }

    public void onSensorChanged(SensorEvent event) {
        gx = event.values[0];
        gy = event.values[1];
        gz = event.values[2];
        accel = (float)Math.sqrt(gx*gx + gy*gy + gz*gz);
    }

    private static void verifyStoragePermissions(Activity activity) {
        int readPermission = ContextCompat.checkSelfPermission(activity, mPermissions[0]);
        int writePermission = ContextCompat.checkSelfPermission(activity, mPermissions[1]);

        if (writePermission != PackageManager.PERMISSION_GRANTED ||
                readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    mPermissions,
                    REQUEST_EXTERNAL_STORAGE_CODE
            );
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
