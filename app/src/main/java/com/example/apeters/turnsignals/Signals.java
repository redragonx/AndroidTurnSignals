package com.example.apeters.turnsignals;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by apeters on 11/10/2015.
 */
public class Signals {
    private boolean mBrakeOn = false;
    private boolean mLeftOn = false;
    private boolean mRightOn = false;
    private int mOnTime = 500;
    private int mOffTime = 200;
    private Timer mFlashTimer;
    private BluetoothServer mBluetoothServerService;
    private boolean mTimerRunning;
    private SignalUpdateListener mSignalUpdateListener;
    private String mDeviceName;

    private boolean mFlash = false; //Set by timers on when the first signal is set. Both signals
                                    //reference this master flasher
    public Signals(){
        this(null);
    }

    public Signals(BluetoothServer server){
        this(server, false, false, false);
    }

    public Signals(BluetoothServer server, boolean brakeOn, boolean leftOn, boolean rightOn){
        this.mBluetoothServerService = server;
        this.mBrakeOn = brakeOn;
        this.mLeftOn = leftOn;
        this.mRightOn = rightOn;

        mFlashTimer = new Timer();
        if(mLeftOn || mRightOn){
            startBlinker();
        }
    }

    public void setBluetoothServerService(BluetoothServer server){
        mBluetoothServerService = server;
    }

    public BluetoothServer getBluetoothServerService(){
        return mBluetoothServerService;
    }

    public void setDeviceName(String name){
        mDeviceName = name;
    }
    public String getDeviceName(){
        return mDeviceName;
    }

    public void setBrakeOn(){
        mBrakeOn = true;
        updateBrake();
        outputToDevice();
    }

    public void setBrakeOff(){
        mBrakeOn = false;
        updateBrake();
        outputToDevice();
    }

    public boolean isBrakeOn(){
        return mBrakeOn;
    }

    public void setRightOn(){
        mRightOn = true;
        startBlinker();
        updateRight();
        outputToDevice();
    }

    public void setRightOff(){
        if(!mLeftOn) {
            cancelBlinker();
        }
        mRightOn = false;
        updateRight();
        outputToDevice();
    }

    public boolean isRightOn(){
        return mRightOn;
    }

    //Return the current lit state
    public boolean getRightSignal(){
        return mRightOn && mFlash;
    }

    public void setLeftOn(){
        mLeftOn = true;
        startBlinker();
        updateLeft();
        outputToDevice();
    }

    public void setLeftOff(){
        if(!mRightOn) {
            cancelBlinker();
        }
        mLeftOn = false;
        updateLeft();
        outputToDevice();
    }

    public boolean isLeftOn(){
        return mLeftOn;
    }

    //Return the current lit state
    public boolean getLeftSignal(){
        return mLeftOn && mFlash;
    }

    public void setFlashTimes(int onMillis, int offMillis){
        mOnTime = onMillis;
        mOffTime = offMillis;
    }

    public void startBlinker(){
        if(mTimerRunning) return;
        mTimerRunning = true;
        mFlashTimer = new Timer();
        mFlash = true;
        mFlashTimer.schedule(new FlashTimer(), mOnTime);
    }

    private class FlashTimer extends TimerTask {
        @Override
        public void run() {
            int time = mFlash ? mOffTime : mOnTime;
            mFlash = !mFlash;
            outputToDevice();
            updateLeftSignal();
            updateRightSignal();
            mFlashTimer = new Timer();
            mFlashTimer.schedule(new FlashTimer(),time);
        }
    }


    private String blinkerString() {
        //encode output into single byte
        byte output = 0;
        if (!mBrakeOn) output += 1;
        if (!getLeftSignal()) output += 2;
        if (!getRightSignal()) output += 4;
        String outputStr = output + "\n";

        return outputStr;
    }

    public void outputToDevice(){
        if(mBluetoothServerService == null){
            return;
        }
        String blinkerOutputString = blinkerString();
        mBluetoothServerService.write(blinkerOutputString.getBytes());
    }

    public void cancelBlinker() {
        if (mTimerRunning) {
            mFlashTimer.cancel();
            mFlashTimer = null;
            mFlash = false;
            mTimerRunning = false;
            updateLeftSignal();
            updateRightSignal();
        }
    }

    public void setSignalUpdateListener(SignalUpdateListener listener){
        this.mSignalUpdateListener = listener;
    }

    public void removeSignalUpdateListener(){
        this.mSignalUpdateListener = null;
    }

    private void updateBrake(){
        if(mSignalUpdateListener != null){
            mSignalUpdateListener.brakeUpdate(mBrakeOn);
        }
    }
    private void updateLeft(){
        if(mSignalUpdateListener != null){
            mSignalUpdateListener.leftUpdate(mLeftOn);
        }
    }
    private void updateRight(){
        if(mSignalUpdateListener != null){
            mSignalUpdateListener.rightUpdate(mRightOn);
        }
    }
    private void updateLeftSignal(){
        if(mSignalUpdateListener != null){
            mSignalUpdateListener.leftSignalUpdate(mLeftOn && mFlash);
        }
    }
    private void updateRightSignal(){
        if(mSignalUpdateListener != null){
            mSignalUpdateListener.rightSignalUpdate(mRightOn && mFlash);
        }
    }

    public interface SignalUpdateListener {
        void brakeUpdate(boolean brakeIsOn);
        void leftUpdate(boolean leftIsOn);
        void rightUpdate(boolean rightIsOn);
        void leftSignalUpdate(boolean leftSignalIsOn);
        void rightSignalUpdate(boolean rightSignalIsOn);
    }

}


