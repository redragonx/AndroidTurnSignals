package com.example.apeters.turnsignals;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by apeters on 11/10/2015.
 * TODO: Allow setSignalUpdateListener to store multiple listeners.
 * TODO: Refactor to have bluetooth coms in own package using new SignalUpdateListener
 */
public class Signals {
    /* Signal Control variables */
    private boolean mBrakeOn = false;
    private boolean mLeftOn = false;
    private boolean mRightOn = false;
    private int mOnTime = 500;
    private int mOffTime = 200;
    /* End Signal Control Variables */
    //Timer to trigger the signals flashing on and off
    private Timer mFlashTimer;
    private boolean mTimerRunning;

    private BluetoothServer mBluetoothServerService;
    private String mDeviceName;
    //Public interface to alert controllers of updates.
    private SignalUpdateListener mSignalUpdateListener;

    //Set by timers on when the first signal is set. Both signals
    //reference this master flasher
    private boolean mFlash = false;

    /**
     * Constructor for signals without connection
     */
    public Signals(){
        this(null);
    }

    /**
     * Constructor for signals with established Bluetooth device
     */
    public Signals(BluetoothServer server){
        this(server, false, false, false);
    }

    /**
     * Constructor for signals with established Bluetooth device and known state.
     */
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

    /**
     * Sets a new bluetooth server for communication with physical device
     * @param server    The new BluetoothServer object with an established connection
     */
    public void setBluetoothServerService(BluetoothServer server){
        mBluetoothServerService = server;
    }

    /**
     * Returns the current BluetoothServer
     * @return  The current BluetoothServer object.
     */
    public BluetoothServer getBluetoothServerService(){
        return mBluetoothServerService;
    }

    /**
     * Sets the current DeviceName stored by the BluetoothServer.
     * @param name  The name of the bluetooth device connected in the BluetoothServer.
     */
    public void setDeviceName(String name){
        mDeviceName = name;
    }
    /**
     * Sets the current DeviceName stored by the BluetoothServer.
     * @return  The name of the bluetooth device connected in the BluetoothServer.
     */
    public String getDeviceName(){
        return mDeviceName;
    }

    /**
     * Sets the brake light on and sends to the device if available.
     */
    public void setBrakeOn(){
        mBrakeOn = true;
        updateBrake();
        outputToDevice();
    }

    /**
     * Sets the brake light off and sends to the device if available.
     */
    public void setBrakeOff(){
        mBrakeOn = false;
        updateBrake();
        outputToDevice();
    }

    /**
     * Returns the current brake status
     * @return  The on status of the brake light
     */
    public boolean isBrakeOn(){
        return mBrakeOn;
    }

    /**
     * Sets the right signal on and sends to the device if available. Starts the blinker.
     */
    public void setRightOn(){
        mRightOn = true;
        startBlinker();
        updateRight();
        outputToDevice();
    }

    /**
     * Sets the right signal off and sends to the device if available. Stops the blinker if it is
     * not used by the left signal.
     */
    public void setRightOff(){
        if(!mLeftOn) {
            cancelBlinker();
        }
        mRightOn = false;
        updateRight();
        outputToDevice();
    }
    /**
     * Returns the current right signal status
     * @return  The on status of the right signal
     */
    public boolean isRightOn(){
        return mRightOn;
    }

    /**
     * Returns the current right signal status factoring in the blinker
     * @return  The on status of the right signal with the blinker.
     */
    public boolean getRightSignal(){
        return mRightOn && mFlash;
    }

    /**
     * Sets the left signal on and sends to the device if available. Starts the blinker.
     */
    public void setLeftOn(){
        mLeftOn = true;
        startBlinker();
        updateLeft();
        outputToDevice();
    }

    /**
     * Sets the left signal off and sends to the device if available. Stops the blinker if it is
     * not used by the right signal.
     */
    public void setLeftOff(){
        if(!mRightOn) {
            cancelBlinker();
        }
        mLeftOn = false;
        updateLeft();
        outputToDevice();
    }

    /**
     * Returns the current left signal status
     * @return  The on status of the left signal
     */
    public boolean isLeftOn(){
        return mLeftOn;
    }

    /**
     * Returns the current left signal status factoring in the blinker
     * @return  The on status of the left signal with the blinker.
     */
    public boolean getLeftSignal(){
        return mLeftOn && mFlash;
    }

    /**
     * Sets the blinker on and off times.
     * @param onMillis  Number of milliseconds the blinker will stay on before turning off.
     * @param offMillis Number of milliseconds the blinker will stay off before turning on.
     */
    public void setFlashTimes(int onMillis, int offMillis){
        mOnTime = onMillis;
        mOffTime = offMillis;
    }

    /**
     * Starts the blinker if it is not running.
     */
    public void startBlinker(){
        if(mTimerRunning) return;
        mTimerRunning = true;
        mFlashTimer = new Timer();
        mFlash = true;
        mFlashTimer.schedule(new FlashTimer(), mOnTime);
    }

    /**
     * Cancels the blinker if it is running.
     */
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

    /**
     * TimerTask object. Changes the flash, updates the device and listeners and
     * schedules a new timer
     */
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

    /**
     * Create the two character string to output to the physical bluetooth device.
     * @return  String with the characters to write to the physical device.
     */
    private String blinkerString() {
        //encode output into single byte
        byte output = 0;
        if (!isBrakeOn()) output += 1;
        if (!getLeftSignal()) output += 2;
        if (!getRightSignal()) output += 4;
        String outputStr = output + "\n";

        return outputStr;
    }

    /**
     * Outputs the current state to the physical device.
     */
    public void outputToDevice(){
        if(mBluetoothServerService == null){
            return;
        }
        String blinkerOutputString = blinkerString();
        mBluetoothServerService.write(blinkerOutputString.getBytes());
    }


    /**
     * Sets the signal listener for this signal object.
     * @param listener  The SignalUpdate Listener to call state updates are made.
     */
    public void setSignalUpdateListener(SignalUpdateListener listener){
        this.mSignalUpdateListener = listener;
    }

    /**
     * Removes the signal listener.
     */
    public void removeSignalUpdateListener(){
        this.mSignalUpdateListener = null;
    }

    /**
     * Calls the SignalListener for the brake
     */
    private void updateBrake(){
        if(mSignalUpdateListener != null){
            mSignalUpdateListener.brakeUpdate(mBrakeOn);
        }
    }
    /**
     * Calls the SignalListener for the leftSignal
     */
    private void updateLeft(){
        if(mSignalUpdateListener != null){
            mSignalUpdateListener.leftUpdate(mLeftOn);
        }
    }
    /**
     * Calls the SignalListener for the rightSignal
     */
    private void updateRight(){
        if(mSignalUpdateListener != null){
            mSignalUpdateListener.rightUpdate(mRightOn);
        }
    }
    /**
     * Calls the SignalListener for the leftSignal with the blinker
     */
    private void updateLeftSignal(){
        if(mSignalUpdateListener != null){
            mSignalUpdateListener.leftSignalUpdate(mLeftOn && mFlash);
        }
    }
    /**
     * Calls the SignalListener for the rightSignal with the blinker
     */
    private void updateRightSignal(){
        if(mSignalUpdateListener != null){
            mSignalUpdateListener.rightSignalUpdate(mRightOn && mFlash);
        }
    }

    /**
     * SignalUpdateListener for activity to receive updates.
     */
    public interface SignalUpdateListener {
        void brakeUpdate(boolean brakeIsOn);
        void leftUpdate(boolean leftIsOn);
        void rightUpdate(boolean rightIsOn);
        void leftSignalUpdate(boolean leftSignalIsOn);
        void rightSignalUpdate(boolean rightSignalIsOn);
    }

}


