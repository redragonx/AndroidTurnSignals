package com.example.apeters.turnsignals;


import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import android.widget.ToggleButton;

/**
 * Created by stephen on 11/14/15.
 */
public class MainJacketFragment extends Fragment {

    private static final String TAG = "BluetoothJacketFragment";

    /**
     * Name of the connected device
     */
    private static String mConnectedDeviceName = null;

    private Signals mSignals;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private ToggleButton mBrakeButton;
    private ToggleButton mLeftButton;
    private ToggleButton mRightButton;

    private Button mConnectButton;

    private BluetoothAdapter mBluetoothAdapter = null;

    private BluetoothServer mBluetoothServerService = null;

    private SignalService mSignalService;

    private boolean mBound = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("Test","onCreateFragment");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();



        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_main, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBrakeButton = (ToggleButton) view.findViewById(R.id.BrakeToggle);
        mRightButton = (ToggleButton) view.findViewById(R.id.RightToggle);
        mLeftButton = (ToggleButton) view.findViewById(R.id.LeftToggle);

        mConnectButton = (Button) view.findViewById(R.id.connect_button);
        // Bind to LocalService
        Intent intent = new Intent(getActivity(), SignalService.class);
        getActivity().getApplication().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void setupButtons(){


        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ensureDiscoverable();
            }
        });

        mBrakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBrakeButton.isChecked()){
                    mSignals.setBrakeOn();
                }else{
                    mSignals.setBrakeOff();
                }
                mSignals.outputToDevice();
            }
        });

        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLeftButton.isChecked()) {
                    mSignals.setLeftOn();
                }
                else {
                    mSignals.setLeftOff();
                }
            }
        });

        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRightButton.isChecked()) {
                    mSignals.setRightOn();
                } else {
                    mSignals.setRightOff();
                }
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        //If BT is not on, request that it be enabled.
        // setupServer() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the server session
        }
    }




    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a jacket session
                    setupServer();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause()  {
        super.onPause();
    }


    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothServerService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothServerService.getState() == BluetoothServer.STATE_NONE) {
                // Start the Bluetooth chat services
                mBluetoothServerService.start();
            }
        }
    }

    private void setupServer() {
        mBluetoothServerService = new BluetoothServer(getActivity(), mHandler);
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
        TextView txtViewDeviceName = (TextView)activity.findViewById(R.id.txtViewDeviceName);
        txtViewDeviceName.setText(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
        TextView txtViewDeviceName = (TextView)activity.findViewById(R.id.txtViewDeviceName);
        txtViewDeviceName.setText(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothServer.STATE_CONNECTED:
                            if(activity != null) {
                                setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                                mSignals.setBluetoothServerService(mBluetoothServerService);
                                //mSignals = new Signals(mBluetoothServerService);

                        }
                            break;
                        case BluetoothServer.STATE_CONNECTING:
                            if (activity != null) {
                                setStatus(R.string.title_connecting);
                            }
                            break;
                        case BluetoothServer.STATE_LISTEN:
                        case BluetoothServer.STATE_NONE:
                            if (activity != null) {
                                setStatus(R.string.title_not_connected);
                            }
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:

                    break;
                case Constants.MESSAGE_READ:

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                        setStatus(mConnectedDeviceName);
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBluetoothServerService.connect(device, secure);
    }

    private ServiceConnection mConnection = new ServiceConnection() {



        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("Test123456789", "Test");
            Log.d("Service","Bound");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SignalService.LocalBinder binder = (SignalService.LocalBinder) service;
            mSignals = binder.getSignals();
            setupButtons();
            mSignalService = binder.getService();
            mBound = true;


            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                // Otherwise, setup the server session
            } else if (mSignals.getBluetoothServerService() == null) {
                setupServer();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
