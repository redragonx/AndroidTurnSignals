package com.example.apeters.turnsignals;


import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.example.apeters.turnsignals.logger.*;

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
    private ToggleButton mLeftBlinkerToggle;
    private ToggleButton mRightBlinkerToggle;

    private Button mConnectButton;

    private BluetoothAdapter mBluetoothAdapter = null;

    private BluetoothServer mBluetoothServerService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        mBrakeButton = (ToggleButton) view.findViewById(R.id.BrakeToggle);
        mRightButton = (ToggleButton) view.findViewById(R.id.RightToggle);
        mLeftButton = (ToggleButton) view.findViewById(R.id.LeftToggle);

        mLeftBlinkerToggle = (ToggleButton) view.findViewById(R.id.leftBlinkerToggle);
        mRightBlinkerToggle = (ToggleButton) view.findViewById(R.id.rightBlinkerToggle);

        mConnectButton = (Button) view.findViewById(R.id.connect_button);

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ensureDiscoverable();
            }
        });

        mBrakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check that we're actually connected before trying anything
                if (mBluetoothServerService.getState() != BluetoothServer.STATE_CONNECTED) {
                    Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                    mBrakeButton.setChecked(false);
                    return;
                }
                mBluetoothServerService.write(newOutPutJacketString().getBytes());
            }

        });

        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check that we're actually connected before trying anything
                if (mBluetoothServerService.getState() != BluetoothServer.STATE_CONNECTED) {
                    Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                    mLeftButton.setChecked(false);
                    return;
                }
                mBluetoothServerService.write(newOutPutJacketString().getBytes());
            }
        });

        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check that we're actually connected before trying anything
                if (mBluetoothServerService.getState() != BluetoothServer.STATE_CONNECTED) {
                    Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                    mRightButton.setChecked(false);
                    return;
                }
                mBluetoothServerService.write(newOutPutJacketString().getBytes());
            }
        });

        mLeftBlinkerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check that we're actually connected before trying anything
                if (mBluetoothServerService.getState() != BluetoothServer.STATE_CONNECTED) {
                    Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                    mLeftBlinkerToggle.setChecked(false);
                    return;
                }

                if (mLeftBlinkerToggle.isChecked()) {
                    mSignals.setLeftOn();
                }
                else {
                    mSignals.setLeftOff();
                }
            }
        });

        mRightBlinkerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check that we're actually connected before trying anything
                if (mBluetoothServerService.getState() != BluetoothServer.STATE_CONNECTED) {
                    Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                    mRightBlinkerToggle.setChecked(false);
                    return;
                }

                if (mRightBlinkerToggle.isChecked()) {
                    mSignals.setRightOn();
                }
                else {
                    mSignals.setRightOff();
                }
            }
        });
    }

    private void startRightBlinker() {
        // mSignals.setLeftOff();
        mSignals.setRightOn();
    }

    private void startLeftBlinker() {
       // mSignals.setRightOff();
        mSignals.setLeftOn();
    }
    private String newOutPutJacketString() {
        //encode output into single byte
        byte output = 0;
        if (!mBrakeButton.isChecked()) output += 1;
        if (!mLeftButton.isChecked()) output += 2;
        if (!mRightButton.isChecked()) output += 4;
        String outputStr = output + "\n";

        return outputStr;
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupServer() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the server session
        } else if (mBluetoothServerService == null) {
            setupServer();
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
        if (mBluetoothServerService != null) {
            mBluetoothServerService.stop();
        }
    }

    @Override
    public void onPause()  {
        super.onPause();

        if (mBluetoothServerService.getState() == BluetoothServer.STATE_CONNECTED) {
            mBluetoothServerService.stop();
        }
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
                                mSignals = new Signals(mBluetoothServerService);
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
}
