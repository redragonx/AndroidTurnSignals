package com.example.apeters.turnsignals;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
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
import android.widget.CompoundButton;
import android.widget.Switch;
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
    private static String EXTRA_DEVICE_ADDRESS = "device_address";

    private TextView mDeviceStatusTextView;

    private ToggleButton mBrakeButton;
    private ToggleButton mLeftButton;
    private ToggleButton mRightButton;

    private Switch mGyroSwitch;
    private Switch mAccelSwitch;

    private Button mConnectButton;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothServer mBluetoothServerService = null;

    private SignalService mSignalService;

    private boolean mBound = false;


/* Fragment Lifecycle methods */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("Test","onCreateFragment");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            setStatus(R.string.bt_not_available);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
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
        mGyroSwitch = (Switch) view.findViewById(R.id.gyro_switch);
        mAccelSwitch = (Switch) view.findViewById(R.id.accelerometer_switch);
        mDeviceStatusTextView = (TextView) view.findViewById(R.id.txtViewDeviceName);

        // Bind to LocalService
        Intent intent = new Intent(getActivity(), SignalService.class);
        getActivity().getApplication().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mSignals != null) {
            setupSignalListener();
        }
    }

    @Override
    public void onPause()  {
        if(mSignals != null) {
            mSignals.removeSignalUpdateListener();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


/* End Fragment Lifecycle methods */

/* Private Methods */

    //Setup all of the buttons. Called once the service has been bound.
    private void setupButtons(boolean brakeOn, boolean leftOn, boolean rightOn){

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ensureDiscoverable();
                setupServer();
            }
        });


        mBrakeButton.setChecked(brakeOn);
        mBrakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBrakeButton.isChecked()) {
                    mSignals.setBrakeOn();
                } else {
                    mSignals.setBrakeOff();
                }
            }
        });



        mLeftButton.setChecked(leftOn);
        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLeftButton.isChecked()) {
                    mSignals.setLeftOn();
                } else {
                    mSignals.setLeftOff();
                }
            }
        });

        mRightButton.setChecked(rightOn);
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

        mGyroSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSignalService.useGyro();
                    mLeftButton.setEnabled(false);
                    mLeftButton.setClickable(false);
                    mRightButton.setEnabled(false);
                    mRightButton.setClickable(false);
                } else {
                    mSignalService.disableGyro();
                    mLeftButton.setEnabled(true);
                    mLeftButton.setClickable(true);
                    mRightButton.setEnabled(true);
                    mRightButton.setClickable(true);
                }
            }
        });

        mAccelSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try {
                        mSignalService.useAccel();
                    } catch (Exception e) {
                        getActivity().finish();
                    }
                    mBrakeButton.setEnabled(false);
                    mBrakeButton.setClickable(false);
                } else {
                    mSignalService.disableAccel();
                    mBrakeButton.setEnabled(true);
                    mBrakeButton.setClickable(true);
                }
            }
        });

    }

    private void setupSignalListener(){
        mSignals.setSignalUpdateListener(new Signals.SignalUpdateListener() {
            @Override
            public void brakeUpdate(final boolean brakeIsOn) {
                mBrakeButton.setChecked(brakeIsOn);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (brakeIsOn) {
                            mBrakeButton.setTextColor(Color.RED);
                        } else {
                            mBrakeButton.setTextColor(Color.BLACK);
                        }
                    }
                });
            }

            @Override
            public void leftUpdate(boolean leftIsOn) {
                mLeftButton.setChecked(leftIsOn);
            }

            @Override
            public void rightUpdate(boolean rightIsOn) {
                mRightButton.setChecked(rightIsOn);
            }

            @Override
            public void leftSignalUpdate(final boolean leftSignalIsOn) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(leftSignalIsOn) {
                            mLeftButton.setTextColor(Color.YELLOW);
                        }else{
                            mLeftButton.setTextColor(Color.BLACK);
                        }
                    }
                });
            }

            @Override
            public void rightSignalUpdate(final boolean rightSignalIsOn) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(rightSignalIsOn) {
                            mRightButton.setTextColor(Color.YELLOW);
                        }else{
                            mRightButton.setTextColor(Color.BLACK);
                        }
                    }
                });
            }
        });
    }
    //create new server and start listening for connections
    private void setupServer() {
        Log.d(TAG, "setupServer");
        mBluetoothServerService = new BluetoothServer(getActivity(), mHandler);
        mBluetoothServerService.start();
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
     * Updates the status in the device status text
     * @param resId a string resource ID for status message
     */
    private void setStatus(int resId) {
        mDeviceStatusTextView.setText(resId);
    }

    /**
     * Updates the status to in the device status text
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        mDeviceStatusTextView.setText(subTitle);
    }

    /**
     * Establish connection with other device
     * @param data   An {@link Intent} with EXTRA_DEVICE_ADDRESS extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBluetoothServerService.connect(device, secure);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    setStatus(R.string.title_connecting);
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    setStatus(R.string.title_connecting);
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG,"Bluetooth Enabled");
                    //Bluetooth is now enabled, so set up a jacket session
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }



    /**
     * The Handler that gets information back from the BluetoothServer
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
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    mSignals.setDeviceName(mConnectedDeviceName);
                    break;
                case Constants.MESSAGE_TOAST:
                    break;
            }
        }
    };

    /*
     *  Service Connection for the SignalsService. Called when the service binds
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG,"Bound");
            SignalService.LocalBinder binder = (SignalService.LocalBinder) service;
            mSignals = binder.getSignals();
            mConnectedDeviceName = mSignals.getDeviceName();
            //Buttons cannot affect the signals before the service is bound.
            setupButtons(mSignals.isBrakeOn(), mSignals.isLeftOn(), mSignals.isRightOn());
            setupSignalListener();
            mSignalService = binder.getService();
            mBound = true;

            if(mSignals.getBluetoothServerService() != null) {
                Log.d(TAG,"State: "+mSignals.getBluetoothServerService().getState());
                switch (mSignals.getBluetoothServerService().getState()) {
                    case BluetoothServer.STATE_CONNECTED:
                        setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                        break;
                    case BluetoothServer.STATE_LISTEN:
                        setStatus(R.string.title_listening);
                        break;
                    case BluetoothServer.STATE_CONNECTING:
                        setStatus(R.string.title_connecting);
                        break;
                    default:
                        setStatus(R.string.title_not_connected);
                }
            }else{
                setStatus(R.string.title_not_connected);
            }



            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                setStatus(R.string.bt_not_enabled);
            } else if (mSignals.getBluetoothServerService() == null) {
                setStatus(R.string.title_not_connected);
                //setupServer();
            }



        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
