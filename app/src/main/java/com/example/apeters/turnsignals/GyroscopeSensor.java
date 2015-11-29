package com.example.apeters.turnsignals;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by Brittany on 11/27/2015.
 */
public class GyroscopeSensor implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Signals mSignals;

    private float quantum = 1.0f,
            xt = 0.0f,
            yt = 0.0f,
            zt = 0.0f;

    public GyroscopeSensor(Context context, Signals signals){
        mSignals = signals;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        xt += event.values[0]*quantum;
        yt += event.values[1]*quantum;
        zt += event.values[2]*quantum;
        Log.d("Gyro", "Sensor Chamged: "+yt);
        if(yt < -3){
            Log.d("Gyro", "Left On");
            mSignals.setLeftOn();
            mSignals.setRightOff();
        } else if (yt > 3) {
            Log.d("Gyro", "Right On");
            mSignals.setRightOn();
            mSignals.setLeftOff();
        } else {
            mSignals.setLeftOff();
            mSignals.setRightOff();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float[] getGyroscopeVals(){
        float[] f = new float[]{xt, yt, zt};
        return f;
    }

    public void setQuantum(float f){
        quantum = f;
    }

    public void cancel() {
        mSensorManager.unregisterListener(this);
    }
}