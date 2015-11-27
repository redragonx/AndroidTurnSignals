package com.example.apeters.turnsignals;

import com.example.apeters.turnsignals.logger.Log;

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
    private int mOffTime = 500;
    private Timer mFlashTimer;
    private BluetoothServer mBluetoothServerService;
    private boolean mTimerRunning;
    private boolean mTimerCancel;

    private boolean flash = false; //Set by timers on when the first signal is set. Both signals
                                    //reference this master flasher

    public Signals(BluetoothServer server){
        this(server, false, false, false);
    }

    public Signals(BluetoothServer server, boolean brakeOn, boolean leftOn, boolean rightOn){
        this.mBluetoothServerService = server;
        this.mBrakeOn = brakeOn;
        this.mLeftOn = leftOn;
        this.mRightOn = rightOn;
        this.mTimerCancel = false;

        mFlashTimer = new Timer();
        if(mLeftOn || mRightOn){
            startBlinker();
        }
    }

    public boolean getTimerRunning() {
        return mTimerRunning;
    }

    public void setRightOn(){
        mRightOn = true;
        startBlinker();
    }

    public void setRightOff(){
        cancelBlinker();
        mRightOn = false;
    }

    public boolean getRightSignal(){
        return mRightOn && flash;
    }

    public void setLeftOn(){
        mLeftOn = true;
        startBlinker();
    }

    public void setLeftOff(){
        cancelBlinker();
        mLeftOn = false;
    }

    public boolean getLeftSignal(){
        return mLeftOn && flash;
    }


    public void startBlinker(){
        if(mTimerRunning) return;

        mTimerRunning = true;

        if (mTimerCancel) {
            mFlashTimer = new Timer();
            mTimerCancel = false;
        }
        mFlashTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                flash = !flash;


                String blinkerOutputString = blinkerString();

                Log.d("timer", "Blinker: " + blinkerOutputString);

                // mBluetoothServerService.write("0\n".getBytes());
                mBluetoothServerService.write(blinkerOutputString.getBytes());

            }
            // start immediately and every 0.5 secs
        }, 0, mOnTime);
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

    public void cancelBlinker() {
        if (mTimerRunning) {
            mFlashTimer.cancel();
            mFlashTimer = null;

            if(flash) flash = false;

            // reset lights to left
            mBluetoothServerService.write("0".getBytes());
            mTimerRunning = false;
            mTimerCancel = true;
        }
    }

}
