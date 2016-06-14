package thermocompany.thermostat;

import android.os.CountDownTimer;
import android.os.SystemClock;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;

import util.*;

/**
 * Created by s152582 on 14-6-2016.
 */
public class BackgroundManager {
    ArrayList<Integer> activeNightTimes;
    ArrayList<Integer> activeDayTimes;
    ArrayList<Integer> daySwitchQueue;
    ArrayList<Integer> nightSwitchQueue;
    WeekProgram wpg;
    String day;
    CountDownTimer refreshTimer;
    int currentTime;
    int previousTime;

    BackgroundManager() {
        activeDayTimes = new ArrayList<>();
        activeNightTimes = new ArrayList<>();

        HeatingSystem.WEEK_PROGRAM_ADDRESS = HeatingSystem.BASE_ADDRESS + "/weekProgram";
        refreshTimer = new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            currentTime = Integer.parseInt(HeatingSystem.get("time").replace(":", ""));
                        } catch (ConnectException e) {
                            e.printStackTrace();
                        }
                        if (previousTime - currentTime > 0) {
                            System.out.println("New day");
                            storeActiveSwitches();
                        }
                        previousTime = currentTime;
                    }
                }).start();
                //System.out.println(currentTime);
                checkForSwitch();
                refreshTimer.start();
            }
        }.start();
        determineNextSwitch(currentTime);
    }

    void storeActiveSwitches() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wpg = HeatingSystem.getWeekProgram();
                    day = HeatingSystem.get("day");
                } catch (ConnectException e) {
                    e.printStackTrace();
                } catch (CorruptWeekProgramException e) {
                    e.printStackTrace();
                }
                activeNightTimes.clear();
                activeDayTimes.clear();
                for (int i = 0; i < 10; i++) {
                    Switch aSwitch = wpg.data.get(day).get(i);
                    if (aSwitch.getState()) {
                        int time = Integer.parseInt(aSwitch.getTime().replace(":", ""));
                        if (aSwitch.getType().equals("night")) {
                            activeNightTimes.add(time);
                        }
                        if (aSwitch.getType().equals("day")) {
                            activeDayTimes.add(time);
                        }
                    }
                }
                System.out.println(activeDayTimes);
            }
        }).start();

    }

    void checkForSwitch() {

    }

    void determineNextSwitch(int time) {
        daySwitchQueue = new ArrayList<>();
        nightSwitchQueue = new ArrayList<>();

        for (int i = 0; i < activeDayTimes.size(); i++) {
            daySwitchQueue.add(activeDayTimes.get(i) - time);
        }
        for (int i = 0; i < activeNightTimes.size(); i++) {
            nightSwitchQueue.add(activeNightTimes.get(i) - time);
        }

        Collections.sort(nightSwitchQueue);
        Collections.sort(daySwitchQueue);
        System.out.println(nightSwitchQueue);
        System.out.println(daySwitchQueue);
    }
}
