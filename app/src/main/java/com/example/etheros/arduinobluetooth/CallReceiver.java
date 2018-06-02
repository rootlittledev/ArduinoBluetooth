package com.example.etheros.arduinobluetooth;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public class CallReceiver extends PhoneCallReceiver {
    private OutputStream outStream = null;


    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {


        // Create a data stream so we can talk to the device
        //outStream = ArduinoMain.outStream;

        String caller = number.substring(3);

        sendData("Calling\n\n" + caller);




        Log.i("test","Calling\n" + caller);
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
    }


    public void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        try {
            //attempt to place data on the outstream to the BT device
            outStream.write(msgBuffer);
        } catch (IOException e) {
            //if the sending fails this is most likely because device is no longer there

        }
        Log.i("test", message);
    }




}