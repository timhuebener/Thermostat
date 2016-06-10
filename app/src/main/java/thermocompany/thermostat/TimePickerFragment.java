package thermocompany.thermostat;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by s152582 on 10-6-2016.
 */
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = 0;
        int minute = 0;

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, true);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
        String time;
        String hour;
        String minute;

        if (hourOfDay < 10) {
            hour = ("0" + hourOfDay);
        } else {
            hour = String.valueOf(hourOfDay);
        }

        if (minuteOfHour < 10) {
            minute = ("0" + minuteOfHour);
        } else {
            minute = String.valueOf(minuteOfHour);
        }

        time = (hour + ":" + minute);

        day.setTimeToSwitch(time);
    }
}