package com.example.etheros.arduinobluetooth;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    int hour;
    int minute;

    int time;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user

        time = 3600 * hour + 60 * minute;


    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        time = 0;
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

    public int getTime() {
        return time;
    }
}

