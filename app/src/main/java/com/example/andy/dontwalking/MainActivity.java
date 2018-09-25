package com.example.andy.dontwalking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;


public class MainActivity extends AppCompatActivity implements Runnable, SensorEventListener {

    SensorManager sm;
    TextView tv;
    Handler h;
    float gx, gy, gz;
    float accel;
    float accel1, accel2, accel3;
    boolean flag = false;
    boolean getflag = false;
    boolean standflag = false;
    boolean walkflag = false;
    boolean runflag = false;
    int count;
    int counting = 0;

    Context context = null;

    //DataSource source;
    Instances instances = null;
    Classifier classifier = null;

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

        context = this;

        verifyStoragePermissions(this);

        tv = findViewById(R.id.text);

        h = new Handler();
        h.postDelayed(this, 0);

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/baka/");
        dir.mkdir();

        final Button standButton = (Button)findViewById(R.id.button_stand);
        standButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                standflag = true;
                CreateDialog();
            }
        });

        Button walkButton = (Button)findViewById(R.id.button_walk);
        walkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                walkflag = true;
                CreateDialog();
            }
        });

        Button runButton = (Button)findViewById(R.id.button_run);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runflag = true;
                CreateDialog();
            }
        });

        Button deleteButton = (Button)findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file1 = new File("/storage/emulated/0/Download/baka/stand.csv");
                file1.delete();
                File file2 = new File("/storage/emulated/0/Download/baka/walk.csv");
                file2.delete();
                File file3 = new File("/storage/emulated/0/Download/baka/run.csv");
                file3.delete();
                File file4 = new File("/storage/emulated/0/Download/baka/dead.arff");
                file4.delete();

                new AlertDialog.Builder(context)
                        .setMessage("I was deleted...")
                        .setPositiveButton("close",null)
                        .show();
            }
        });

        Button setButton = (Button)findViewById(R.id.button_set);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    new AlertDialog.Builder(context)
                            .setMessage("I studied your data!")
                            .setPositiveButton("close",null)
                            .show();

                    saveFile("dead.arff", "@relation dead\n\n@attribute sensor real\n@attribute state {stand, walk, run}\n\n@data\n", false);

                    File file1 = new File("/storage/emulated/0/Download/baka/stand.csv");
                    BufferedReader br1 = new BufferedReader(new FileReader(file1));

                    String str = br1.readLine();
                    while(str != null){
                        saveFile("dead.arff", str + "stand", true);
                        str = br1.readLine();
                    }

                    File file2 = new File("/storage/emulated/0/Download/baka/walk.csv");
                    BufferedReader br2 = new BufferedReader(new FileReader(file2));

                    str = br2.readLine();
                    while(str != null){
                        saveFile("dead.arff", str + "walk", true);
                        str = br2.readLine();
                    }

                    File file3 = new File("/storage/emulated/0/Download/baka/run.csv");
                    BufferedReader br3 = new BufferedReader(new FileReader(file3));

                    str = br3.readLine();
                    while(str != null){
                        saveFile("dead.arff", str + "run", true);
                        str = br3.readLine();
                    }

                    br1.close();
                    br2.close();
                    br3.close();
                }catch(FileNotFoundException e){
                    System.out.println(e);
                    System.out.println("error001");
                }catch(IOException e){
                    System.out.println(e);
                    System.out.println("error001");
                }
            }
        });

        Button startButton = (Button)findViewById(R.id.button_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConverterUtils.DataSource source = null;
                try{
                    source = new ConverterUtils.DataSource("/storage/emulated/0/Download/baka/dead.arff");
                    instances = source.getDataSet();
                    instances.setClassIndex(1);
                    classifier = new J48();
                    classifier.buildClassifier(instances);
                    flag = true;
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.println("error002");
                }
            }
        });
    }

    public void saveFile(String filename, String str, boolean tui) {

        try {
            FileWriter file = new FileWriter("/storage/emulated/0/Download/baka/" + filename, tui);
            PrintWriter pw = new PrintWriter(new BufferedWriter(file));

            pw.println(str);

            pw.close();

        } catch (IOException e) {
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
        h.postDelayed(this, 0);
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
        count++;
        gx = event.values[0];
        gy = event.values[1];
        gz = event.values[2];
        accel = (float)Math.sqrt(gx*gx + gy*gy + gz*gz);

        if(flag==true && count%5==0){
            wekaCall();
        }

        if(standflag){
            saveFile("stand.csv", String.valueOf(accel) + ",", true);
        }else if(walkflag){
            saveFile("walk.csv", String.valueOf(accel) + ",", true);
        }else if(runflag){
            saveFile("run.csv", String.valueOf(accel) + ",", true);
        }
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

    public void wekaCall(){
        String str_color = null;

        try{
            String state_str = null;

            Evaluation eval = new Evaluation(instances);
            eval.evaluateModel(classifier, instances);
            //System.out.println(eval.toSummaryString());

            FastVector out = new FastVector(3);
            out.addElement("stand");
            out.addElement("walk");
            out.addElement("run");
            Attribute sensor = new Attribute("sensor", 0);
            Attribute state = new Attribute("state", out, 1);
            FastVector win = new FastVector(2);

            Instance instance = new DenseInstance(2);
            instance.setValue(sensor, accel);
            instance.setDataset(instances);

            double result = classifier.classifyInstance(instance);
            //System.out.println(result);
            switch ((int)result){
                case 0: state_str = "stand";
                        str_color = "#000000";
                        counting = 0;
                        break;
                case 1: state_str = "walk";
                        counting++;
                        str_color = "#FFCC00";
                        break;
                case 2: state_str = "run";
                        counting+=2;
                        str_color = "#FF0000";
                        break;
            }
            if(counting > 5){
                new AlertDialog.Builder(context)
                        .setMessage("standing or dead?")
                        .setPositiveButton("I got it!",null)
                        .show();
            }
            TextView stateText = findViewById(R.id.now_state);
            stateText.setText(state_str);
            stateText.setTextColor(Color.parseColor(str_color));
            //System.out.println(state_str);

        }catch (Exception e){
            e.printStackTrace();
            //System.out.println("error003");
        }
    }

    public void CreateDialog(){
        //System.out.println("alertdialog\n");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Measuring for you!")
                .setMessage("I measure your statement!")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                standflag = false;
                                walkflag = false;
                                runflag = false;
                            }
                        })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        standflag = false;
                        walkflag = false;
                        runflag = false;
                    }
                });

        alertDialogBuilder.setCancelable(true);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        //System.out.println("fin\n");
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
