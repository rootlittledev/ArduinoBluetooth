package com.example.etheros.arduinobluetooth;

import java.io.IOException;

import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

//Створюємо клас ArduinoMain який наслідує клас Activity
public class ArduinoMain extends Activity {

    //Поля користувацького інтерфейсу
    Button button_one;
    Button button_two;
    Button button_three;
    Button button_four;

    private EditText editText;

    private Boolean showTime = false;

    //Змінні для інформації про bluetooth адаптер
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    public static OutputStream outStream = null;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Зчитуємо МАС адресу
    public String newAddress = null;

    Context context;

    int notificationIn;
    int timerIn;
    private GoogleApiClient mGoogleApiClient;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arduino_main);

        context = getApplicationContext();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .build();
        mGoogleApiClient.connect();

        addKeyListener();

        //Привязуємо змінні з елементами інтерфейсу
        button_one = findViewById(R.id.first_button);
        button_two = findViewById(R.id.second_button);
        button_three = findViewById(R.id.third_button);
        button_four = findViewById(R.id.fourth_button);

        editText = findViewById(R.id.text_input);

        //Знаходим адаптер bluetooth телефона
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //Перевіряємо чи ввімкнений bluetooth
        checkBTState();

        initialize();

        timeSender();

        //Встановлюємо дію при нажатті кнопки
        button_one.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(ArduinoMain.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        //eReminderTime.setText( selectedHour + ":" + selectedMinute);
                        notificationIn = 3600 * selectedHour + 60 * selectedMinute;
                        notificationSender();
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        button_two.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                //Вмкористовуємо Google API для отримання погоди
                Awareness.SnapshotApi.getWeather(mGoogleApiClient)
                        .setResultCallback(new ResultCallback<WeatherResult>() {
                            @Override
                            public void onResult(@NonNull WeatherResult weatherResult) {
                                if (weatherResult.getStatus().isSuccess()) {
                                    Weather weather = weatherResult.getWeather();
                                    weatherSender(weather.getTemperature(Weather.CELSIUS), weather.getConditions());
                                    System.out.print(weather.getTemperature(Weather.CELSIUS));
                                }
                            }
                        });
            }
        });

        button_three.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(ArduinoMain.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        //eReminderTime.setText( selectedHour + ":" + selectedMinute);
                        timerIn = 3600 * selectedHour + 60 * selectedMinute;
                        timerSender();
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();


            }
        });

        button_four.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("xi\n");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        //Відправляєм дані що користувач все ще працює з пристроєм
        sendData("xv\n");

    }

    //Перевантажуємо метод onPause який буде викликатись коли програма буде згорнута в трей
    @Override
    public void onPause() {
        super.onPause();

        //Закриваємо звязок з пристроєм
        try     {
            btSocket.close();
        } catch (IOException e2) {
            Toast.makeText(getBaseContext(), "ERROR - Failed to close Bluetooth socket", Toast.LENGTH_SHORT).show();
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    private void checkBTState() {
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "ERROR - Device does not support bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (btAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "ERROR - Device not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        Log.i("DataSender", message);
    }
    public void addKeyListener() {
        editText = findViewById(R.id.text_input);

        editText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                    textSender();

                    return true;
                }

                return false;
            }
        });
    }

    //метод для відправлення текстового повідомлення
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

    //метод для відправлення погоди
    private void weatherSender(Float temperature, int[] conditions){


        sendData("xw" + conditions[0] + Math.round(temperature) +"C\n");
    }

    //метод для відправлення часу
    private void timeSender(){
        Calendar calendar = Calendar.getInstance();

        int seconds = 3600 * calendar.get(Calendar.HOUR_OF_DAY) + 60 * calendar.get(Calendar.MINUTE) + calendar.get(Calendar.SECOND);

        StringBuilder msg = new StringBuilder();
        msg.append("xx");
        msg.append(seconds);
        msg.append("\n");

        sendData(msg.toString());

        Log.i("TimeSender", msg.toString());
    }

    //метод для відправлення даних для таймера
    private void timerSender(){

        int seconds = timerIn;

        StringBuilder msg = new StringBuilder();
        msg.append("xt");
        msg.append(seconds);
        msg.append("\n");

        sendData(msg.toString());

        Log.i("TimeSender", msg.toString());
    }

    //метод для створення нагадування
    private void notificationSender(){
        String startText = editText.getText().toString();

        int seconds = notificationIn;

        String[] message = startText.split(" ");

        StringBuilder data = new StringBuilder();

        data.append("xn");

        data.append(seconds);

        int offset = 7 - data.length();

        for (int index = 0; index < offset; index++)
            data.append(" ");

        for (String subText : message) {
            data.append(subText);
            data.append("\n");
        }

        Log.i("Notification", data.toString());

        sendData(data.toString());

    }



    @SuppressLint("MissingPermission")
    private void initialize(){
        Intent intent = getIntent();
        newAddress = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        BluetoothDevice device = btAdapter.getRemoteDevice(newAddress);

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e1) {
            Toast.makeText(getBaseContext(), "ERROR - Could not create Bluetooth socket", Toast.LENGTH_SHORT).show();
        }

        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();        //If IO exception occurs attempt to close socket
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "ERROR - Could not close Bluetooth socket", Toast.LENGTH_SHORT).show();
            }
        }

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "ERROR - Could not create bluetooth outstream", Toast.LENGTH_SHORT).show();
        }

    }

}