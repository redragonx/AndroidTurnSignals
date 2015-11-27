package com.example.apeters.turnsignals;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends Activity {
    final static int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerThread mServerThread;
    private static BluetoothManager sRunningThread;
    private ToggleButton mBrakeButton;
    private ToggleButton mLeftButton;
    private ToggleButton mRightButton;
    private Button mConnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBrakeButton = (ToggleButton)findViewById(R.id.BrakeToggle);
        mRightButton = (ToggleButton)findViewById(R.id.RightToggle);
        mLeftButton = (ToggleButton)findViewById(R.id.LeftToggle);
        mConnectButton = (Button)findViewById(R.id.connect_button);

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeDiscoverable();
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //bluetooth device not found
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //TODO continue testing to make sure this works in all cases.
        if(sRunningThread == null) {
            mServerThread = new BluetoothServerThread();
            mServerThread.start();
        }
    }

    private void makeDiscoverable(){
        //Make device discoverable to jacket
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
        startActivity(discoverableIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    private void manageBTConnection(BluetoothSocket socket){
        Log.d("Test","Test");
        Log.d("BTName", socket.getRemoteDevice().getName());
        sRunningThread = new BluetoothManager(socket);
        sRunningThread.start();
    }

//bluetooth server thread. Accepts one connection then
    private class BluetoothServerThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public BluetoothServerThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("TurnSignals",
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));   //Default UUID of Roving networks bt device
            } catch (IOException e) {
                Log.d("Test","Test4");
                e.printStackTrace();
            }
            serverSocket = tmp;
            Log.d("Test", serverSocket.toString());
        }

        public void run(){
            BluetoothSocket socket = null;
            Log.d("Test","Server Started");
            while (true){
                Log.d("Test","Server Running");
                try{
                    Log.d("Test","Test1");
                    socket = serverSocket.accept();
                    Log.d("Test","Test2");
                } catch (IOException e) {
                    Log.d("Test","Test3");
                    e.printStackTrace();
                    break;
                }
                if(socket != null){
                    manageBTConnection(socket);
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    private class BluetoothManager extends Thread {
        private final BluetoothSocket socket;
        private final OutputStream outputStream;
        private byte outputByte =       0b00000000;
        private final byte brakeBit =   0b00000001;
        private final byte leftBit =    0b00000010;
        private final byte rightBit =   0b00000100;

        public BluetoothManager(BluetoothSocket socket){
            this.socket = socket;
            OutputStream tmp = null;
            try {
                tmp = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = tmp;
        }
        //TODO may use these but also might remove to a different class
        public void setLeftSignalOn(){
            outputByte |= leftBit;
        }
        public void setLeftSignalOff(){
            outputByte &= ~leftBit;
        }
        public void setRightSignalOn(){
            outputByte |= rightBit;
        }
        public void setRightSignalOff(){
            outputByte &= ~rightBit;
        }
        public void setBrakeSignalOn(){
            outputByte |= brakeBit;
        }
        public void setBrakeSignalOff(){
            outputByte &= ~brakeBit;
        }


        public void run(){
            Log.d("Test","run in BluetoothManager");
            while(true) {
                //encode output into single byte
                byte output = 0;
                if(!mBrakeButton.isChecked()) output+=1;
                if(!mLeftButton.isChecked()) output+=2;
                if(!mRightButton.isChecked()) output+=4;
                String outputStr = output+"\n";
                char str = outputStr.charAt(0);

                try {
                    byte[] outputByte = {(byte)(output+48), '\n'};
                    //outputStream.write(outputByte);
                    outputStream.write(outputStr.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}