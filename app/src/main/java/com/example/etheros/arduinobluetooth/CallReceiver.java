package com.example.etheros.arduinobluetooth;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

public class CallReceiver extends PhoneCallReceiver {


    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Calendar start) {
        String caller = number.substring(3);

        sendData("Calling\n\n" + caller);

        Log.i("test","Calling\n" + caller);
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Calendar start) {
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Calendar start, Calendar end) {
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Calendar start, Calendar end) {
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Calendar start) {
        String caller = number.substring(3);

        sendData("Missed call\n\n" + caller + "\n\n at: " + start.get(Calendar.HOUR_OF_DAY) + ":" + start.get(Calendar.MINUTE));
    }


    public void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        OutputStream outStream = ArduinoMain.outStream;

        try {
            //attempt to place data on the outstream to the BT device
            outStream.write(msgBuffer);
        } catch (IOException e) {
            //if the sending fails this is most likely because device is no longer there

        }
        Log.i("test", message);
    }




}