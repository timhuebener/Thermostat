package thermocompany.thermostat;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import java.net.ConnectException;

import util.*;

public class MainActivity extends AppCompatActivity {

    TextView tempCurrent;
    TextView tempTarget;
    double targetTemperature;
    double currentTemperature;
    Button plus;
    Button minus;
    String timeValue;
    String dayValue;
    CountDownTimer refreshTimer;
    TextView time;
    Handler repeatHandler;
    Runnable repeatPlus;
    Runnable repeatMinus;
    final int CLICK_INTERVAL = 300;
    Boolean doNotUpdateTarget;
    android.widget.Switch holdSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HeatingSystem.BASE_ADDRESS = "http://wwwis.win.tue.nl/2id40-ws/60";
        tempTarget = (TextView) findViewById(R.id.temp);
        tempCurrent = (TextView) findViewById(R.id.tempActual);
        plus = (Button) findViewById(R.id.plus);
        minus = (Button) findViewById(R.id.minus);
        plus.setLongClickable(true);
        minus.setLongClickable(true);
        repeatHandler = new Handler();
        time = (TextView) findViewById(R.id.time);
        holdSwitch = (android.widget.Switch) findViewById(R.id.scheduleOnOff);
        doNotUpdateTarget = false;

        // this part sets the initial values of the target and current temperature
        // and of the day value and time
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    targetTemperature = Double.parseDouble(HeatingSystem.get("targetTemperature"));
                    currentTemperature = Double.parseDouble(HeatingSystem.get("currentTemperature"));
                    timeValue = HeatingSystem.get("time");
                    dayValue = HeatingSystem.get("day");
                    if (HeatingSystem.getVacationMode()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holdSwitch.setChecked(false);
                            }
                        });
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTargetTempView();
                            updateCurrentTempView();
                            updateTimeView();
                        }
                    });

                } catch (ConnectException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        repeatPlus = new Runnable() {
            @Override
            public void run() {
                System.out.println("Increased temp");
                if (targetTemperature < 29) {
                    targetTemperature = (targetTemperature * 10 + 10) / 10; // to prevent rounding issues
                    updateTargetTempView();
                }
                repeatHandler.postDelayed(repeatPlus, CLICK_INTERVAL);
            }
        };

        repeatMinus = new Runnable() {
            @Override
            public void run() {
                System.out.println("Decreased temp");
                if (targetTemperature > 6) {
                    targetTemperature = (targetTemperature * 10 - 10) / 10; // to prevent rounding issues
                    updateTargetTempView();
                }
                repeatHandler.postDelayed(repeatMinus, CLICK_INTERVAL);
            }
        };

        plus.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                repeatHandler.postDelayed(repeatPlus, 500);
                                if (targetTemperature < 30) {
                                    targetTemperature = (targetTemperature * 10 + 1) / 10; // to prevent rounding issues
                                    updateTargetTempView();
                                }
                                doNotUpdateTarget = true;
                                break;
                            case MotionEvent.ACTION_UP:
                                repeatHandler.removeCallbacks(repeatPlus);
                                doNotUpdateTarget = false;
                                sendTargetTempToServer(); // only updates to server once done increasing to save bandwidth, good idea?
                                break;
                        }
                        return true;
                    }
                }

        );

        minus.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                repeatHandler.postDelayed(repeatMinus, 500);
                                if (targetTemperature > 5) {
                                    targetTemperature = (targetTemperature * 10 - 1) / 10; // to prevent rounding issues
                                    updateTargetTempView();
                                }
                                doNotUpdateTarget = true;
                                break;
                            case MotionEvent.ACTION_UP:
                                repeatHandler.removeCallbacks(repeatMinus);
                                doNotUpdateTarget = false;
                                sendTargetTempToServer();
                                break;
                        }
                        return true;
                    }
                }

        );

        holdSwitch.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            setWeekProgramEnabled();
                        } else {
                            setWeekProgramDisabled();
                        }
                    }
                }
        );

        refreshTimer = new CountDownTimer(1000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            currentTemperature = Double.parseDouble(HeatingSystem.get("currentTemperature"));
                            timeValue = HeatingSystem.get("time");
                            dayValue = HeatingSystem.get("day");
                            if (HeatingSystem.getVacationMode()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        holdSwitch.setChecked(false);
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        holdSwitch.setChecked(true);
                                    }
                                });
                            }

                        } catch (ConnectException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                if (!doNotUpdateTarget) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                targetTemperature = Double.parseDouble(HeatingSystem.get("targetTemperature"));
                            } catch (ConnectException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    updateTargetTempView();
                }
                updateCurrentTempView();
                updateTimeView();
                refreshTimer.start();
            }

        }.start();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.settings:
                Intent settingsIntent = new Intent(this, Settings.class);
                startActivity(settingsIntent);
                break;
            case R.id.scheduleSetting:
                Intent weekIntent = new Intent(this, Weekoverview.class);
                startActivity(weekIntent);
                break;
        }

        return true;
    }

    // disable week program
    void setWeekProgramDisabled() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HeatingSystem.put("weekProgramState", "off");
                } catch (InvalidInputValueException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // enable week program
    void setWeekProgramEnabled() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HeatingSystem.put("weekProgramState", "on");
                } catch (InvalidInputValueException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    // update the target temperature textview
    void updateTargetTempView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempTarget.setText(String.valueOf(targetTemperature) + "\u2103");
            }
        });
    }

    // update the current temperature textview
    void updateCurrentTempView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempCurrent.setText(String.valueOf(currentTemperature) + "\u2103");
            }
        });
    }

    // update the time textview
    void updateTimeView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String current = (dayValue + ", " + timeValue);
                time.setText(current);
            }
        });

    }

    // send the target temperature to server
    void sendTargetTempToServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HeatingSystem.put("targetTemperature", String.valueOf(targetTemperature));
                } catch (InvalidInputValueException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
