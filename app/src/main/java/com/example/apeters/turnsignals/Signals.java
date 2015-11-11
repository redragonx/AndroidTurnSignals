package com.example.apeters.turnsignals;

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
    private boolean mTimerRunning;

    private boolean flash = false; //Set by timers on when the first signal is set. Both signals
                                    //reference this master flasher

    public Signals(){
        this(false, false, false);
    }

    public Signals(boolean brakeOn, boolean leftOn, boolean rightOn){
        this.mBrakeOn = brakeOn;
        this.mLeftOn = leftOn;
        this.mRightOn = rightOn;
        mFlashTimer = new Timer();
        if(mLeftOn || mRightOn){
            startBlinker();
        }
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
        int time = mOnTime;
        if(!flash) time = mOffTime;
        flash = !flash;

        mFlashTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                startBlinker();
            }
        },time);
    }

    public void cancelBlinker(){
        mFlashTimer.cancel();
        mTimerRunning = false;
    }

}
