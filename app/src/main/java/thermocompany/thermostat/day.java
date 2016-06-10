package thermocompany.thermostat;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;

import util.*;

public class day extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    String day;
    WeekProgram localWpg;
    EditText switch0;
    EditText switch1;
    EditText switch2;
    EditText switch3;
    EditText switch4;
    EditText switch5;
    EditText switch6;
    EditText switch7;
    EditText switch8;
    EditText switch9;
    EditText[] switchesNight;
    EditText[] switchesDay;
    Button retrieve;
    ArrayList<String> nightTimes;
    ArrayList<String> dayTimes;
    Button send;
    Button cancel;
    Button save;
    static int pressedSwitchIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);
        switch0 = (EditText) findViewById(R.id.switch0);
        switch1 = (EditText) findViewById(R.id.switch1);
        switch2 = (EditText) findViewById(R.id.switch2);
        switch3 = (EditText) findViewById(R.id.switch3);
        switch4 = (EditText) findViewById(R.id.switch4);
        switch5 = (EditText) findViewById(R.id.switch5);
        switch6 = (EditText) findViewById(R.id.switch6);
        switch7 = (EditText) findViewById(R.id.switch7);
        switch8 = (EditText) findViewById(R.id.switch8);
        switch9 = (EditText) findViewById(R.id.switch9);
        send = (Button) findViewById(R.id.send);
        cancel = (Button) findViewById(R.id.cancel);
        save = (Button) findViewById(R.id.save);

        switchesNight = new EditText[]{switch0, switch1, switch2, switch3, switch4};
        switchesDay = new EditText[]{switch5, switch6, switch7, switch8, switch9};

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localWpg = Memory.getWeekProgram();
                printWeekProgramToTextFields(localWpg);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Save clicked");
                saveToDevice();
            }
        });

        day = Weekoverview.getLastClickedDay();
        HeatingSystem.WEEK_PROGRAM_ADDRESS = HeatingSystem.BASE_ADDRESS + "/weekProgram";
        setTitle(day);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Send clicked");
                sendToServer();
            }
        });

        localWpg = Memory.getWeekProgram();
        if (localWpg == null) {
            System.out.println("No schedule found on device, retrieving from server");
            Toast.makeText(getApplicationContext(), "No schedule found on device, " +
                    "retrieved from server", Toast.LENGTH_LONG).show();
            retrieveFromServer();
        } else {
            System.out.println("Schedule found on device, retrieving from device");
            localWpg = Memory.getWeekProgram();
            printWeekProgramToTextFields(localWpg);
        }

        switch0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressedSwitchIndex = 0;
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "TimePicker");
            }
        });
        switch1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressedSwitchIndex = 1;
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "TimePicker");
            }
        });
        switch2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressedSwitchIndex = 2;
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "TimePicker");
            }
        });
        switch3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressedSwitchIndex = 3;
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "TimePicker");
            }
        });
        switch4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressedSwitchIndex = 4;
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "TimePicker");
            }
        });
        switch5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressedSwitchIndex = 5;
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "TimePicker");
            }
        });
        switch6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressedSwitchIndex = 6;
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "TimePicker");
            }
        });
        switch7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressedSwitchIndex = 7;
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "TimePicker");
            }
        });
        switch8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressedSwitchIndex = 8;
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "TimePicker");
            }
        });
        switch9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressedSwitchIndex = 9;
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "TimePicker");
            }
        });

    }

    // Retrieves schedule from server and stores it in textfield, used if nothing in memory
    void retrieveFromServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                WeekProgram serverWpg = new WeekProgram();
                try {
                    serverWpg = HeatingSystem.getWeekProgram();

                } catch (ConnectException e) {
                    e.printStackTrace();
                } catch (CorruptWeekProgramException e) {
                    e.printStackTrace();
                }

                printWeekProgramToTextFields(serverWpg);
                Memory.storeWeekProgram(serverWpg);
                localWpg = Memory.getWeekProgram();

            }
        }).start();
    }


    // stores schedule in memory
    void saveToDevice() {
        for (int i = 0; i < 5; i++) {
            String time = switchesNight[i].getText().toString();

            if (time.equals("00:00")) {
                localWpg.data.get(day).set(i, new Switch("night", false, time));
            } else {
                localWpg.data.get(day).set(i, new Switch("night", true, time));
            }

        }
        for (int i = 5; i < 10; i++) {
            String time = switchesDay[i - 5].getText().toString();
            if (time.equals("00:00")) {
                localWpg.data.get(day).set(i, new Switch("day", false, time));
            } else {
                localWpg.data.get(day).set(i, new Switch("day", true, time));
            }
        }
        Memory.storeWeekProgram(localWpg);
        printWeekProgramToTextFields(localWpg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Saved schedule" +
                        " to device", Toast.LENGTH_LONG).show();
            }
        });
    }

    // saves schedule that is on screen and then sends it to the server
    void sendToServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveToDevice();
                HeatingSystem.setWeekProgram(localWpg);
            }
        }).start();
        Toast.makeText(getApplicationContext(), "Sent schedule" +
                " to server", Toast.LENGTH_LONG).show();
    }

    void printWeekProgramToTextFields(WeekProgram wpg) {
        nightTimes = new ArrayList<>();
        dayTimes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Switch aSwitch = wpg.data.get(day).get(i);
            if (aSwitch.getType().equals("night")) {
                nightTimes.add(aSwitch.getTime());
            }
            if (aSwitch.getType().equals("day")) {
                dayTimes.add(aSwitch.getTime());
            }
        }

        Collections.sort(nightTimes);
        Collections.sort(dayTimes);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    switchesDay[i].setText(dayTimes.get(i));
                    switchesNight[i].setText(nightTimes.get(i));
                }
            }
        });
        System.out.println("Stored " + wpg + " in TextFields");
    }

    @Override
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

        if (pressedSwitchIndex < 5) {
            switchesNight[pressedSwitchIndex].setText(time);
        } else {
            switchesDay[pressedSwitchIndex-5].setText(time);
        }
    }
}
