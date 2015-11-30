package com.example.apeters.turnsignals;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;


import java.util.List;

/**
 * Created by stephen on 11/28/15.
 */
public class AccelSensor implements SensorEventListener {

    private static final String TAG = "AccelSensor";

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Signals mSignals;

    private float ax, ay, az;

    public AccelSensor(Context context, Signals signals) throws Exception {
        mSignals = signals;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // If you do not have a phone with TYPE_LINEAR_ACCELERATION, you are stuck with TYPE_ACCELERATION,
        // which cannot separate gravity (tilt) from linear acceleration.

        Log.d(TAG, "BLAH 1");
        List<Sensor> accelSensors = mSensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
        for (int i = 0; i < accelSensors.size(); i++) {
            if ((accelSensors.get(i).getVendor().contains("Google Inc.")) &&
                    (accelSensors.get(i).getVersion() == 3)) {
                // Use the version 3 accelerometer sensor.
                mSensor = accelSensors.get(i);
                mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
                Log.d(TAG, "Version 3 linear acceleration meter found");
                return;
            }
        }

        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (mSensor == null) {
            // Sorry, there are no accelerometers on your device.
            // You can't use this app.
            Toast.makeText(context, "No accel!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "no Accel hardware");
            throw new Exception("no accel hardware!");
        }

        Log.d(TAG, "hardware linear acceleration meter found");

        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            // Do stuff for version 3

            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];

            Log.d(TAG, "Version 3 accel: " + "x: " + ax + " y: " + ay + " z: " + az);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // apply low pass?
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void cancel() {
        mSensorManager.unregisterListener(this);
    }
}
