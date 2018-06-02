package com.example.etheros.arduinobluetooth;

import java.io.IOException;

import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ArduinoMain extends Activity {

    //Declare buttons & editText
    Button functionOne, functionTwo;

    private EditText editText;

    private Boolean showTime = false;

    //Memeber Fields
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    public static OutputStream outStream = null;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();

    // UUID service - This is the type of Bluetooth device that the BT module is
    // It is very likely yours will be the same, if not google UUID for your manufacturer
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module
    public String newAddress = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arduino_main);

        addKeyListener();

        //Initialising buttons in the view
        //mDetect = (Button) findViewById(R.id.mDetect);
        functionOne = findViewById(R.id.functionOne);
        functionTwo = findViewById(R.id.functionTwo);

        editText = findViewById(R.id.editText1);

        //getting the bluetooth adapter value and calling checkBTstate function
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        initialize();

        timeSender();

        functionOne.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                textSender();
            }
        });

        functionTwo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                showTime = !showTime;

                if (showTime) {
                    timeSender();
                }
                else {
                    timerHandler.removeCallbacks(timerRunnable);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // connection methods are best here in case program goes into the background etc

        //Get MAC address from DeviceListActivity

        //When activity is resumed, attempt to send a piece of junk data ('x') so that it will fail if not connected
        // i.e don't wait for a user to press button to recognise connection failure

    }

    @Override
    public void onPause() {
        super.onPause();
        //Pausing can be the end of an app if the device kills it or the user doesn't open it again
        //close all connections so resources are not wasted

        //Close BT socket to device
        try     {
            btSocket.close();
        } catch (IOException e2) {
            Toast.makeText(getBaseContext(), "ERROR - Failed to close Bluetooth socket", Toast.LENGTH_SHORT).show();
        }
    }
    //takes the UUID and creates a comms socket
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    //same as in device list activity
    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "ERROR - Device does not support bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (btAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Enabled", Toast.LENGTH_SHORT).show();
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    // Method to send data
    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        try {
            //attempt to place data on the outstream to the BT device
            outStream.write(msgBuffer);
        } catch (IOException e) {
            //if the sending fails this is most likely because device is no longer there
            Toast.makeText(getBaseContext(), "ERROR - Device not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        Log.i("DataSender", message);
    }
    public void addKeyListener() {

        // get edittext component
        editText = findViewById(R.id.editText1);

        // add a keylistener to keep track user input
        editText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if keydown and send is pressed implement the sendData method
                if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                    textSender();

                    return true;
                }

                return false;
            }
        });
    }

    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            Toast.makeText(getBaseContext(), "Display time", Toast.LENGTH_SHORT).show();

            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            String data = date + "\n" + calendar.get(Calendar.HOUR_OF_DAY) + " " + calendar.get(Calendar.MINUTE);
            sendData(data);

            timerHandler.postDelayed(this, 60000);
        }
    };

    private void textSender(){
        char[] message = editText
                .getText()
                .toString()
                .toCharArray();

        StringBuilder data = new StringBuilder();

        for (int index = 1; index <= message.length; index++){
            if(index%11 == 0){
                data.append("\n");
            }
            data.append(message[index - 1]);
        }

        data.append("\n");

        sendData(data.toString());

        Log.i("TextSender", data.toString());

    }

    private void timeSender(){
        Calendar calendar = Calendar.getInstance();

        int seconds = 3600 * calendar.get(Calendar.HOUR_OF_DAY) + 60 * calendar.get(Calendar.MINUTE) + calendar.get(Calendar.SECOND);

        StringBuilder msg = new StringBuilder();
        msg.append("\b\b");
        msg.append(seconds);
        msg.append("\n");

        sendData(msg.toString());

        Log.i("TimeSender", msg.toString());
    }

    private void initialize(){
        Intent intent = getIntent();
        newAddress = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        // Set up a pointer to the remote device using its address.
        BluetoothDevice device = btAdapter.getRemoteDevice(newAddress);

        //Attempt to create a bluetooth socket for comms
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e1) {
            Toast.makeText(getBaseContext(), "ERROR - Could not create Bluetooth socket", Toast.LENGTH_SHORT).show();
        }

        // Establish the connection.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();        //If IO exception occurs attempt to close socket
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "ERROR - Could not close Bluetooth socket", Toast.LENGTH_SHORT).show();
            }
        }

        // Create a data stream so we can talk to the device
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "ERROR - Could not create bluetooth outstream", Toast.LENGTH_SHORT).show();
        }
    }

}