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

//Створюємо клас MessageReceiver який наслідує клас NotificationListenerService
public class MessageReceiver extends NotificationListenerService{

    Context context;

    @Override
    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();

    }

    //Перевантажуємо метод onNotificationPosted який буде виконуватись при новому сповіщенню
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i("Notification", "Started");

        //Отримуємо інформацію про сповіщення
        String pack = sbn.getPackageName();

        PackageManager packageManager= getApplicationContext().getPackageManager();
        String appName = "";
        try {
            appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(pack, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Log.i("Package", pack);

        // Відправляємо дані на пристрій
        if(!pack.equals("android") && !appName.equals("Security") && !appName.equals("Call") && !appName.equals("Phone service"))
        sendData("xl" + appName + "\n" );

    }

    //Перевантажуємо метод onNotificationRemoved який буде виконуватись після перегляду сповіщення
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        sendData("xr\n");

    }


    //Метод sendData який відповідає за відпраку даних на пристрій
    public void sendData(String message) {
        //Зберігаємо наше повідомлення
        byte[] msgBuffer = message.getBytes();

        //Створюємо звязок з пристроєм
        OutputStream outStream = ArduinoMain.outStream;

        try {
            //відправляєм дані
            outStream.write(msgBuffer);
        } catch (IOException e) {
            //виводим помилку якщо не вдалось відправити

        }
        Log.i("test", message);
    }
}
