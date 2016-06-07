package thermocompany.thermostat;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;

import util.*;

public class day extends Activity {

    String day;
    WeekProgram wpg;
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

        nightTimes = new ArrayList<>();
        dayTimes = new ArrayList<>();

        switchesNight = new EditText[]{switch0, switch1, switch2, switch3, switch4};
        switchesDay = new EditText[]{switch5, switch6, switch7, switch8, switch9};

        retrieve = (Button)findViewById(R.id.retrieve);
        retrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshWeekoverview();
            }
        });

        day = Weekoverview.getLastClickedDay();
        HeatingSystem.WEEK_PROGRAM_ADDRESS = HeatingSystem.BASE_ADDRESS + "/weekProgram";
        setTitle(day);



        //addSwitch(3, "night", "07:30");
        //addSwitch(7, "day", "13:00");
        refreshWeekoverview();
    }


    //TODO: smarter way of retrieving times
    void refreshWeekoverview () {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wpg = HeatingSystem.getWeekProgram();
                } catch (ConnectException e) {
                    e.printStackTrace();
                } catch (CorruptWeekProgramException e) {
                    e.printStackTrace();
                }
                for (int i=0; i<10; i++) {
                    Switch aSwitch = wpg.data.get(day).get(i);
                    if (aSwitch.getType().equals("night")) {
                        nightTimes.add(aSwitch.getTime());
                    }
                    if (aSwitch.getType().equals("day")) {
                        dayTimes.add(aSwitch.getTime());
                    }
                }

                //Collections.sort(dayTimes);
                //Collections.sort(nightTimes);

                for (int i=0; i<5; i++) {
                    switchesDay[i].setText(dayTimes.get(i));
                    switchesNight[i].setText(nightTimes.get(i));
                }
            }
        }).start();
    }

    void addSwitch(int index, final String dayNight, final String switchTime) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wpg = HeatingSystem.getWeekProgram();
                } catch (ConnectException e) {
                    e.printStackTrace();
                } catch (CorruptWeekProgramException e) {
                    e.printStackTrace();
                }
                wpg.data.get(day).set(3, new Switch(dayNight, true, switchTime));
                HeatingSystem.setWeekProgram(wpg);
            }
        }).start();

    }
}
