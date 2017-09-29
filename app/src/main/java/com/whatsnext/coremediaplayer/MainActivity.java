
/*
 * Written by Federico Sanna (federico.sanna15@ic.ac.uk)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.whatsnext.coremediaplayer;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import static android.provider.Contacts.OrganizationColumns.LABEL;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_UPWARD_PITCH_INSTRUMENT_SELECTION = 3;
    private static final int REQUEST_LEFT_ROLL_INSTRUMENT_SELECTION = 4;
    private static final int REQUEST_DOWNWARD_PITCH_INSTRUMENT_SELECTION = 5;
    private static final int REQUEST_RIGHT_ROLL_INSTRUMENT_SELECTION = 6;
    private static final int REQUEST_FLIP_INSTRUMENT_SELECTION = 7;
    private static final int REQUEST_TAP_INSTRUMENT_SELECTION = 8;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    private static int n = 0;
    private static boolean playingBase = true;
    private static boolean playingSecondBase = true;

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect;
    private Button btnFlipInstrument, btnTapInstrument;
    private Button btnUpwardPitch, btnDownwardPitch, btnLeftRoll, btnRightRoll;
    private EditText edtMessage;
    Context context;

    private final static String TAP="TAP";
    private final static String FLIP="FLIP";

    private final static String DRUMS="DRUMS";
    private final static String BASS="BASS";
    private final static String PERCUSSION="PERCUSSION";
    private int instrumentId = 0;

    HashMap<String, PlayAudioBase> instruments = new HashMap<>();
    private static int trackId1 = R.raw.guitar_chord;
    private static int trackId2 = R.raw.sing_along;
    private static int trackId3 = R.raw.solo_trumpet;
    private static int trackId4 = R.raw.funk_guitar;

    ArrayList<PlayAudio> listaudio = new ArrayList();

    PlayAudioBase audio5 = new PlayAudioBase(R.raw.base_batteria);
    PlayAudioBase audio6 = new PlayAudioBase(R.raw.bass_loop);

    private static Bundle displayTracks = new Bundle();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.white));
        super.onCreate(savedInstanceState);
        this.context = this;
        requestPermissions();
        setContentView(R.layout.activity_main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        /*messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);*/
        btnConnectDisconnect = (Button) findViewById(R.id.btn_select);
        btnTapInstrument = (Button) findViewById(R.id.tap_base);
        btnFlipInstrument = (Button) findViewById(R.id.flip_base);
        btnUpwardPitch = (Button) findViewById(R.id.upward_pitch);
        btnDownwardPitch = (Button) findViewById(R.id.downward_pitch);
        btnLeftRoll = (Button) findViewById(R.id.left_roll);
        btnRightRoll = (Button) findViewById(R.id.right_roll);

        service_init();


        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    if (btnConnectDisconnect.getText().equals("connect")) {

                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    } else {
                        //Disconnect button pressed

                        //Stop the music that was playing
                        stopMusic();

                        if (mDevice != null) {
                            mService.disconnect();
                        }
                    }
                }
            }
        });

        //Handle instrument selection for upward pitch
        btnUpwardPitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //UPWARD PITCH button pressed, open InstrumentSelection class, with popup windows that shows instruments
                Intent newIntent = new Intent(MainActivity.this, InstrumentSelection.class);
                //Bundle displayOnlyBase = new Bundle();
                displayTracks.putInt("keyDisplayTracks", 2); //My id
                newIntent.putExtras(displayTracks); //Put my id to my next Intent
                startActivityForResult(newIntent, REQUEST_UPWARD_PITCH_INSTRUMENT_SELECTION);
            }
        });
        //Handle instrument selection for left roll
        btnLeftRoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LEFT ROLL button pressed, open InstrumentSelection class, with popup windows that shows instruments
                Intent newIntent = new Intent(MainActivity.this, InstrumentSelection.class);
                displayTracks.putInt("keyDisplayTracks", 3); //My id
                newIntent.putExtras(displayTracks); //Put my id to my next Intent
                startActivityForResult(newIntent, REQUEST_LEFT_ROLL_INSTRUMENT_SELECTION);
            }
        });
        //Handle instrument selection for downward
        btnDownwardPitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DOWNWARD PITCH button pressed, open InstrumentSelection class, with popup windows that shows instruments
                Intent newIntent = new Intent(MainActivity.this, InstrumentSelection.class);
                displayTracks.putInt("keyDisplayTracks", 4); //My id
                newIntent.putExtras(displayTracks); //Put my id to my next Intent
                startActivityForResult(newIntent, REQUEST_DOWNWARD_PITCH_INSTRUMENT_SELECTION);
            }
        });
        //Handle instrument selection for right roll
        btnRightRoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //RIGHT ROLL button pressed, open InstrumentSelection class, with popup windows that shows instruments
                Intent newIntent = new Intent(MainActivity.this, InstrumentSelection.class);
                displayTracks.putInt("keyDisplayTracks", 5); //My id
                newIntent.putExtras(displayTracks); //Put my id to my next Intent
                startActivityForResult(newIntent, REQUEST_RIGHT_ROLL_INSTRUMENT_SELECTION);
            }
        });
        //Handle instrument selection for flip
        btnFlipInstrument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ON TAP button pressed, open InstrumentSelection class, with popup windows that shows instruments
                Intent newIntent = new Intent(MainActivity.this, InstrumentSelection.class);
                displayTracks.putInt("keyDisplayTracks", 1); //My id
                newIntent.putExtras(displayTracks); //Put my id to my next Intent
                startActivityForResult(newIntent, REQUEST_FLIP_INSTRUMENT_SELECTION);
            }
        });
        //Handle instrument selection for tap
        btnTapInstrument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ON TAP button pressed, open InstrumentSelection class, with popup windows that shows instruments
                Intent newIntent = new Intent(MainActivity.this, InstrumentSelection.class);
                displayTracks.putInt("keyDisplayTracks", 0); //My id
                newIntent.putExtras(displayTracks); //Put my id to my next Intent
                startActivityForResult(newIntent, REQUEST_TAP_INSTRUMENT_SELECTION);
            }
        });
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override

        //Handler events that received from UART service
        public void handleMessage(Message msg) {

        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        btnConnectDisconnect.setText("Disconnect");
                        /*edtMessage.setEnabled(true);
                        btnSend.setEnabled(true);
                        btnio2.setEnabled(true);
                        */
                        /*((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - ready");
                        listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);*/
                        mState = UART_PROFILE_CONNECTED;
                        mService.enableTXNotification();
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        btnConnectDisconnect.setText("connect");
                        /*edtMessage.setEnabled(false);
                        btnSend.setEnabled(false);
                        btnio2.setEnabled(false);
                      */
                        /*((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                        listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());*/
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();

                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            showMessage(text);
                            Log.d(TAG, "Here the message received: "+ text );

                            /*if (text.equals("1")) {
                                PlayAudio audio1 = new PlayAudio(trackId1);
                                int n=0;
                                //try to find the first free space in the array to create an audiio file
                                while(true){
                                    //if the file exist and is still playing, go to the next position
                                    try {
                                        listaudio1.get(n);
                                    } catch (Exception ignore) {
                                        break;
                                    }
                                    //if the file has stop to play you can overwrite it
                                    try {
                                        listaudio1.get(n).isPlaying();
                                    } catch (Exception ignore) {
                                        Log.e(TAG, "isPlaying " + ignore.getMessage());
                                        listaudio1.remove(n);
                                        break;
                                    }
                                    n++;
                                }*/
                                /*while (true){
                                    try {
                                        listaudio1.get(n);
                                    } catch (Exception ignore) {
                                        break;
                                    }
                                    if (!listaudio1.get(n).isPlaying()){
                                        listaudio1.remove(n);
                                        break;
                                    }
                                    n++;
                                }*/
                                /*while (true){
                                    try {
                                        if(listaudio1.get(n).isPlaying()==true)
                                            n++;
                                        else {
                                            listaudio1.get(n).release();
                                            listaudio1.remove(n);
                                            break;
                                        }
                                    } catch (Exception ignore) {
                                        break;
                                    }
                                }*/
                                /*while (true){
                                    try {
                                        if(listaudio1.get(n).isPlaying()==true)
                                            n++;
                                        else {
                                            listaudio1.get(n).pause();
                                            listaudio1.get(n).release();
                                            listaudio1.remove(n);
                                            break;
                                        }
                                    } catch (Exception ignore) {
                                        break;
                                    }
                                }*//*
                                Log.d(TAG, "creating a new file audio at the postion " + n + " in the array");
                                listaudio1.add(n, audio1);
                                audio1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }*/
                            PlayAudio audio = null;

                            if (text.equals("1")) {
                                audio = new PlayAudio(trackId1);
                            } else if (text.equals("2")){
                                audio = new PlayAudio(trackId2);
                            } else if (text.equals("3")){
                                audio = new PlayAudio(trackId3);
                            } else if (text.equals("4")){
                                audio = new PlayAudio(trackId4);
                            }
                            int n=0;
                            while(true){
                                //if the file exist and is still playing, go to the next position
                                try {
                                    listaudio.get(n);
                                } catch (Exception ignore) {
                                    break;
                                }
                                //if the file has stop to play you can overwrite it
                                try {
                                    listaudio.get(n).isPlaying();
                                } catch (Exception ignore) {
                                    Log.e(TAG, "isPlaying " + ignore.getMessage());
                                    listaudio.remove(n);
                                    break;
                                }
                                n++;
                            }
                            Log.d(TAG, "creating a new file audio at the postion " + n + " in the array");
                            if (audio!=null) {
                                listaudio.add(n, audio);
                                audio.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }

                            if (text.equals("5")) {
                                //PlayAudioBase audio = new PlayAudioBase(R.raw.base_batteria);
                                if(!instruments.containsKey(FLIP)){
                                    instruments.put(FLIP, audio5);
                                    instruments.get(FLIP).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                                //audio = instruments.get(FLIP);
                                else if (playingBase) {
                                    instruments.get(FLIP).pause();
                                    playingBase = false;
                                } else {
                                    //instruments.get(FLIP).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    instruments.get(FLIP).start();
                                    playingBase = true;
                                }
                            }

                            if (text.equals("6")) {
                                //PlayAudioBase audio = new PlayAudioBase(instrumentId);
                                if(!instruments.containsKey(TAP)){
                                    instruments.put(TAP, audio6);
                                    instruments.get(TAP).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                                //audio = instruments.get(TAP);
                                else if (playingSecondBase) {
                                    instruments.get(TAP).pause();
                                    playingSecondBase = false;
                                } else {
                                    //instruments.get(TAP).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    Log.d(TAG, "Start to play music for tap!!");

                                    instruments.get(TAP).start();
                                    playingSecondBase = true;
                                }
                            }



                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage(), e);

                        }
                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }


        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService = null;

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        stopMusic();
        finish();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        //stopMusic();
        super.onPause();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    /*((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");*/
                    mService.connect(deviceAddress);

                }
                break;

            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;

            case REQUEST_UPWARD_PITCH_INSTRUMENT_SELECTION:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    //get EXTRA from InstrumentSelection and set a default sound
                    int instrumentId = data.getIntExtra(InstrumentSelection.EXTRA_INSTRUMENT, R.raw.guitar_chord);
                    //create a new PlayAudioBase object with the SounfdID passed by the InstrumentSelection Activity
                    trackId1 = instrumentId;
                    Log.d(TAG, "... onActivityResultdInstrument.ID==" + instrumentId);
                }
                break;

            case REQUEST_LEFT_ROLL_INSTRUMENT_SELECTION:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    //get EXTRA from InstrumentSelection and set a default sound
                    int instrumentId = data.getIntExtra(InstrumentSelection.EXTRA_INSTRUMENT, R.raw.sing_along);
                    //create a new PlayAudioBase object with the SounfdID passed by the InstrumentSelection Activity
                    trackId2 = instrumentId;
                    Log.d(TAG, "... onActivityResultdInstrument.ID==" + instrumentId);
                }
                break;

            case REQUEST_DOWNWARD_PITCH_INSTRUMENT_SELECTION:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    //get EXTRA from InstrumentSelection and set a default sound
                    int instrumentId = data.getIntExtra(InstrumentSelection.EXTRA_INSTRUMENT, R.raw.solo_trumpet);
                    //create a new PlayAudioBase object with the SounfdID passed by the InstrumentSelection Activity
                    trackId3 = instrumentId;
                    Log.d(TAG, "... onActivityResultdInstrument.ID==" + instrumentId);
                }
                break;

            case REQUEST_RIGHT_ROLL_INSTRUMENT_SELECTION:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    //get EXTRA from InstrumentSelection and set a default sound
                    int instrumentId = data.getIntExtra(InstrumentSelection.EXTRA_INSTRUMENT, R.raw.funk_guitar);
                    //create a new PlayAudioBase object with the SounfdID passed by the InstrumentSelection Activity
                    trackId4 = instrumentId;
                    Log.d(TAG, "... onActivityResultdInstrument.ID==" + instrumentId);
                }
                break;

            case REQUEST_FLIP_INSTRUMENT_SELECTION:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    //get EXTRA from InstrumentSelection and set a default sound
                    int instrumentId = data.getIntExtra(InstrumentSelection.EXTRA_INSTRUMENT, R.raw.bass_loop);
                    //create a new PlayAudioBase object with the SounfdID passed by the InstrumentSelection Activity
                    audio5 = new PlayAudioBase(instrumentId);
                    //if audio was playing stop it
                    if(instruments.containsKey(FLIP)){
                        instruments.get(FLIP).stop();
                    }
                    instruments.remove(FLIP);
                    Log.d(TAG, "... onActivityResultdInstrument.ID==" + instrumentId);

                }
                break;

            case REQUEST_TAP_INSTRUMENT_SELECTION:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    //get EXTRA from InstrumentSelection and set a default sound
                    int instrumentId = data.getIntExtra(InstrumentSelection.EXTRA_INSTRUMENT, R.raw.base_batteria);
                    //create a new PlayAudioBase object with the SounfdID passed by the InstrumentSelection Activity
                    audio6 = new PlayAudioBase(instrumentId);
                    //if audio was playing stop it
                    if(instruments.containsKey(TAP)){
                        instruments.get(TAP).stop();
                    }
                    instruments.remove(TAP);
                    Log.d(TAG, "... onActivityResultdInstrument.ID==" + instrumentId);

                }
                break;

        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.popup_title)
                .setMessage(R.string.popup_message)
                .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopMusic();
                        finish();
                    }
                })
                .setNegativeButton(R.string.popup_no, null)
                .show();
    }


    /**
     * Request global permissions for the application
     */
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, 123);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 123);
        }

    }

    /**
     * Async task to play an audio file
     */
    class PlayAudioBase extends AsyncTask<Object, Object, Void> {
        int audiofile=0;
        MediaPlayer mp;
        /**
         * PlayAudioBase Constructor
         * @param audiofile the path of the file you want to play
         */

        PlayAudioBase(int audiofile){
            this.audiofile = audiofile;
        }

        public void stop(){

            Log.d(TAG, "STOP");
            if(mp!=null){
                //mp.setLooping(false);
                mp.stop();
            }else
                Log.d(TAG, "MP is null");

        }

        public void start(){

            Log.d(TAG, "STOP");
            if(mp!=null){
                //mp.setLooping(false);
                mp.start();
            }else
                Log.d(TAG, "MP is null");

        }

        public void pause(){

            Log.d(TAG, "STOP");
            if(mp!=null){
                //mp.setLooping(false);
                mp.pause();
            }else
                Log.d(TAG, "MP is null");

        }

        public boolean isPlaying(){
            if (mp!=null){
                return mp.isPlaying();
            }else{
                Log.d(TAG, "MP is null");
                return false;
            }
        }

        /**
         * Start on execute
         *
         * @param params NN
         * @return
         */

        @Override
        public Void doInBackground(Object... params) {
            //Log.d(LABEL, "Play audio " + fileName);
            //Get the entire path
            //String path = Environment.getExternalStorageDirectory()+"/"+ AntitheftManager.MEDIA_FILE_PATH+"//;
            //File file = new File(path + fileName);
            //if (file.exists()) {
            mp = MediaPlayer.create(context, audiofile);
            if (isCancelled()){
                Log.d(TAG, "isCancelled");
                mp.setLooping(false);
            }
            if (true){
                try {

                    //Create the MediaPlayer object and play the sound
                    //MediaPlayer mp = MediaPlayer.create(context, Uri.parse(path + fileName));
                    //MediaPlayer mp = MediaPlayer.create(context, Uri.parse("R.raw.brasilian_percussion"));
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                        }

                    });
                    mp.setLooping(true);

                    mp.start();

                } catch (Exception e) {
                    Log.d(LABEL, "Play audio");
                    //Log.d(LABEL, e);
                }

            } else
                //Log.d(LABEL, "The file " + path + fileName + " does not exit");
                Log.d(LABEL, "The file " + " does not exit");


            return null;
        }


    }

    class PlayAudio extends AsyncTask<Object, Object, Void> {
        int audiofile=0;
        MediaPlayer mp;
        boolean released=true;
        //Context context = this.context;
        /**
         * PlayAudio Constructor
         * @param audiofile the path of the file you want to play
         */

        PlayAudio(int audiofile){
            this.audiofile = audiofile;
        }

        public void pause(){

            Log.d(TAG, "PAUSE");
            if(mp!=null){
                mp.pause();
            }else
                Log.d(TAG, "MP is null");

        }

        public void stop(){

            Log.d(TAG, "STOP");
            if(mp!=null){
                mp.stop();
                mp.release();
            }else
                Log.d(TAG, "MP is null");

        }

        public void release(){

            Log.d(TAG, "RELEASE");
            if(mp!=null){
                mp.release();
            }else
                Log.d(TAG, "MP is null");

        }

        public boolean isPlaying(){
            if (mp!=null){
                return mp.isPlaying();
            }else{
                Log.d(TAG, "MP is null");
                return false;
            }
        }

        /**
         * Start on execute
         *
         * @param params NN
         * @return
         */

        @Override
        public Void doInBackground(Object... params) {
            //Log.d(LABEL, "Play audio " + fileName);
            //Get the entire path
            //String path = Environment.getExternalStorageDirectory()+"/"+ AntitheftManager.MEDIA_FILE_PATH+"//;
            //File file = new File(path + fileName);
            //if (file.exists()) {

            mp = MediaPlayer.create(context, audiofile);
            if (isCancelled()){
                Log.d(TAG, "isCancelled");
            }
            if (true){
                try {

                    //Create the MediaPlayer object and play the sound
                    //MediaPlayer mp = MediaPlayer.create(context, Uri.parse(path + fileName));
                    //MediaPlayer mp = MediaPlayer.create(context, Uri.parse("R.raw.brasilian_percussion"));
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                            //released=true;
                        }

                    });
                    mp.setLooping(false);
                    mp.start();
                    //released=false;

                } catch (Exception e) {
                    Log.d(LABEL, "Play audio");
                    //Log.d(LABEL, e);
                }

            } else
                //Log.d(LABEL, "The file " + path + fileName + " does not exit");
                Log.d(LABEL, "The file " + " does not exit");
            return null;
        }
    }

    private void stopMusic(){

        for(PlayAudio p:listaudio){
            if (p!=null)
                try{p.stop();}
                catch (Exception ignore){
                    Log.e(TAG, "cannot stop file audio: " + p + " because: " + ignore.getMessage());
                }
        }
        listaudio.clear();

                        /*int n = 0;
                        for(n = 0; n < 20; n++){
                            //if a file audio exists, stop it
                            try {
                                listaudio1.get(n).stop();
                            } catch (Exception ignore) {}

                        */
        if(instruments.containsKey(FLIP)){
            instruments.get(FLIP).pause();
        }
        if(instruments.containsKey(TAP)){
            instruments.get(TAP).pause();
        }

    }
}
