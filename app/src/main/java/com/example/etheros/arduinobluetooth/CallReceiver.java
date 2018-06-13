package com.example.etheros.arduinobluetooth;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

//Створюємо клас CallReceiver який наслідує клас PhoneCallReceiver
public class CallReceiver extends PhoneCallReceiver {

    //Перевантажуємо метод onIncomingCallStarted який буде виконуватись при вхідному дзвінку
    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Calendar start) {
        //зчитуємо інформацію про того хто телефонує
        String caller = number.substring(3);

        //відправляємо інформацію
        sendData("xc" + caller + "\n");

        Log.i("test","Calling\n" + caller);
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Calendar start) {
    }

    //Перевантажуємо метод onIncomingCallEnded який буде виконуватись при завершенні дзвінка
    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Calendar start, Calendar end) {
        //відправляємо сигнал про завершення дзвінка
        sendData("xe\n");
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Calendar start, Calendar end) {
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Calendar start) {
        sendData("xm\n");
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