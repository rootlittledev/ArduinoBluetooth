package com.example.etheros.arduinobluetooth;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.OutputStream;

public class MessageReceiver extends NotificationListenerService{

    Context context;

    @Override
    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();

    }
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {


        String pack = sbn.getPackageName();

        PackageManager packageManager= getApplicationContext().getPackageManager();
        String appName = "";
        try {
            appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(pack, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Log.i("Package", pack);

        if(!pack.equals("android") && !appName.equals("Security") && !appName.equals("Call") && !appName.equals("Phone"))
        sendData("xl" + appName + "\n" );

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        sendData("xr\n");

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
