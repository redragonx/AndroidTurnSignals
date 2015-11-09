package com.example.apeters.turnsignals;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.support.v4.app.ActivityCompat.startActivity;

public class MainActivity extends Activity {
    final static int REQUEST_ENABLE_BT = 1;
    public static BluetoothAdapter btAdapter;
    private static BluetoothServerThread serverThread;
    private static BluetoothManager runningThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            //bluetooth device not found
        }
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //Make device discoverable to jacket
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);


        startActivity(discoverableIntent);
        serverThread = new BluetoothServerThread();
        serverThread.start();


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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void manageBTConnection(BluetoothSocket socket){
        Log.d("Test","Test");
        Log.d("BTName", socket.getRemoteDevice().getName());
        runningThread = new BluetoothManager(socket);
        runningThread.start();

    }

//bluetooth server thread. Accepts one connection then
    private class BluetoothServerThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public BluetoothServerThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = btAdapter.listenUsingInsecureRfcommWithServiceRecord("TurnSignals",
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

        public void run(){
            Log.d("Test","run in BluetoothManager");
            while(true) {
                try {
                    byte[] output = {'2', '\n'};
                    outputStream.write(output);
                    Log.d("Test", "Writing 1");
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