package com.example.apeters.turnsignals;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.List;

/**
 * Created by stephen on 11/28/15.
 */
public class AccelSensor implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Signals mSignals;

    public AccelSensor(Context context, Signals signals) {
        mSignals = signals;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // If you do not have a phone with TYPE_LINEAR_ACCELERATION, you are stuck with TYPE_ACCELERATION,
        // which cannot separate gravity (tilt) from linear acceleration.

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            List<Sensor> accelSensors = mSensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
            for(int i=0; i < accelSensors.size(); i++) {
                if ((accelSensors.get(i).getVendor().contains("Google Inc.")) &&
                        (accelSensors.get(i).getVersion() == 3)){
                    // Use the version 3 accelerometer sensor.
                    mSensor = accelSensors.get(i);
                }
            }
        }
        else {
            // Use the accelerometer.
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
                mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
            else{
                // Sorry, there are no accelerometers on your device.
                // You can't use this app.
            }
        }

        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            // Do stuff for version 3
        }
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // apply low pass?
        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
