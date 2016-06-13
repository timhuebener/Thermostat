package thermocompany.thermostat;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by s152582 on 10-6-2016.
 */
public class TimePickerFragment extends DialogFragment {

    Activity activity;
    TimePickerDialog.OnTimeSetListener listener;

    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity)context;
        listener = (TimePickerDialog.OnTimeSetListener)activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour = 0;
        int minute = 0;

        // Create a new instance of TimePickerDialog and return it
        // use a different listener, listener in day activity
        return new TimePickerDialog(activity, listener, hour, minute, true);
    }


}