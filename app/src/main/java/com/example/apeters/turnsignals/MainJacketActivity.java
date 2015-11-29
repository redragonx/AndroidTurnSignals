package com.example.apeters.turnsignals;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import android.util.Log;

/**
 * Created by stephen on 11/14/15.
 */
public class MainJacketActivity extends FragmentActivity{

    public static final String TAG = "MainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        Intent intent = new Intent(this, SignalService.class);
        startService(intent);


//        Intent intent2 = new Intent(this, SignalService.class);
//        getApplicationContext().bindService(intent2, mConnection, Context.BIND_AUTO_CREATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            MainJacketFragment fragment = new MainJacketFragment();
            transaction.replace(R.id.Main_Fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

//    private ServiceConnection mConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            Log.d("Service","Bound2");
//            // We've bound to LocalService, cast the IBinder and get LocalService instance
//
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//
//        }
//    };

}
