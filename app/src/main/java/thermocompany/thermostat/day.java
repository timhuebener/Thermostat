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


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wpg = HeatingSystem.getWeekProgram();
                    wpg.setDefault();
                } catch (ConnectException e) {
                    e.printStackTrace();
                } catch (CorruptWeekProgramException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        addSwitch();
    }

    // this does not work, will try to fix
    void addSwitch() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                wpg.data.get(day).set(5, new Switch("day", true, "07:30"));
                HeatingSystem.setWeekProgram(wpg);
            }
        });

    }
}
