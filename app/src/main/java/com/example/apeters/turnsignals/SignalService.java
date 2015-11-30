package com.example.apeters.turnsignals;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by apeters on 11/27/2015.
 * Service class to hold the signals, GyroscopeSensor, and AccelSensor objects. Will run in the
 * background even if the app is closed.
 */

public class SignalService extends Service {

    private Signals mSignals;
    private GyroscopeSensor mGyroSensor;
    private AccelSensor mAccelSensor;

    private boolean mUsingGyroscope;
    private boolean mUsingAccelerometer;

    private final IBinder mBinder = new LocalBinder();
    /**
     * Binder to pass references to this object and the signals object to the activity it is bound to
     */
    public class LocalBinder extends Binder {
        SignalService getService() {
            return SignalService.this;
        }
        Signals getSignals(){
            return mSignals;
        }
    }

    /**
     * Start activity to create the signals originally.
     * TODO: read close intent
     * @return  Service restart method.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("Service","Started");

        if(mSignals == null){
            mSignals = new Signals();
        }
        return Service.START_NOT_STICKY;
    }

    /**
     * Return for bindService();
     * @return  IBinder with methods to get this object and signals.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Service", "Binder Called");
        return mBinder;
    }

    /**
     * Enable the Gyroscope for the turn Signals
     */
    public void useGyro(){
        Log.d("SensorService", "Starting Gyro");
        mUsingGyroscope = true;
        if(mGyroSensor == null){
            mGyroSensor = new GyroscopeSensor(getApplicationContext(), mSignals);
        }
    }

    /**
     * Disable the gyroscope and allow the gyroscopeSensor class to cancel update requests.
     */
    public void disableGyro(){
        Log.d("SensorService", "Stopping Gyro");
        mUsingGyroscope = false;
        if(mGyroSensor != null){
            mGyroSensor.cancel();
            mGyroSensor = null;
        }
    }

    /**
     * Enable the accelerometer for the brake
     */
    public void useAccel() throws Exception {
        Log.d("SensorService", "Starting Accel");
        mUsingAccelerometer = true;
        if(mAccelSensor == null) {
          mAccelSensor = new AccelSensor(getApplicationContext(), mSignals);
        }
    }

    /**
     * Disable the accelerometer and allow the AccelSensor object to cancel update requests.
     */
    public void disableAccel() {
        Log.d("SensorService", "Stopping Accel");
        mUsingAccelerometer = false;
        if(mAccelSensor != null) {
            mAccelSensor.cancel();
            mAccelSensor = null;
        }
    }
}
