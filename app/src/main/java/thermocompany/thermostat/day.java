package thermocompany.thermostat;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.net.ConnectException;

import util.*;

public class day extends Activity {

    String day;
    WeekProgram wpg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        day = Weekoverview.getLastClickedDay();
        HeatingSystem.WEEK_PROGRAM_ADDRESS = HeatingSystem.BASE_ADDRESS + "/weekProgram";
        setTitle(day);

        addSwitch(3, "night", "07:30");
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
