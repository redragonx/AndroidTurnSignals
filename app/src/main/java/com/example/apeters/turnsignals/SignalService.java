package com.example.apeters.turnsignals;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by apeters on 11/27/2015.
 */
public class SignalService extends Service {

    private Signals mSignals;

    private GyroscopeSensor mGyroSensor;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        SignalService getService() {
            return SignalService.this;
        }
        Signals getSignals(){
            return mSignals;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("Service","Started");

        if(mSignals == null){
            mSignals = new Signals();
        }
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Service", "Binder Called");

        return mBinder;
    }

    public void useGyro(){
        Log.d("SensorService","Starting Gyro");
        if(mGyroSensor == null){
            mGyroSensor = new GyroscopeSensor(getApplicationContext(), mSignals);
        }
    }

    public void disableGyro(){
        Log.d("SensorService","Stopping Gyro");
        if(mGyroSensor != null){
            mGyroSensor.cancel();
            mGyroSensor = null;
        }
    }
}
