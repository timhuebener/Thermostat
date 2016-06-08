package thermocompany.thermostat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.ConnectException;
import java.util.ArrayList;

import util.*;

public class day extends Activity {

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

        nightTimes = new ArrayList<>();
        dayTimes = new ArrayList<>();

        switchesNight = new EditText[]{switch0, switch1, switch2, switch3, switch4};
        switchesDay = new EditText[]{switch5, switch6, switch7, switch8, switch9};

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieveFromMemory();
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
            retrieveFromServer();
        } else {
            System.out.println("Schedule found on device, retrieving from device");
            retrieveFromMemory();
        }
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
                for (int i = 0; i < 10; i++) {
                    Switch aSwitch = serverWpg.data.get(day).get(i);
                    if (aSwitch.getType().equals("night")) {
                        nightTimes.add(aSwitch.getTime());
                    }
                    if (aSwitch.getType().equals("day")) {
                        dayTimes.add(aSwitch.getTime());
                    }
                }

                storeToTextFields();
                Memory.storeWeekProgram(serverWpg);

            }
        }).start();
    }

    // retrieves schedule from memory and stores in textfields
    void retrieveFromMemory() {
        localWpg = Memory.getWeekProgram();
        for (int i = 0; i < 10; i++) {
            Switch aSwitch = localWpg.data.get(day).get(i);
            if (aSwitch.getType().equals("night")) {
                nightTimes.add(aSwitch.getTime());
            }
            if (aSwitch.getType().equals("day")) {
                dayTimes.add(aSwitch.getTime());
            }
        }
        storeToTextFields();
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
    }

    void storeToTextFields() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Set TO UI");
                for (int i = 0; i < 5; i++) {
                    switchesDay[i].setText(dayTimes.get(i));
                    switchesNight[i].setText(nightTimes.get(i));
                }
            }
        });
    }
}
