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

    //TextView tempTarget;
    TextView tempCurrent;
    TextView tempTarget;
    double targetTemperature;
    double currentTemperature;
    Button plus;
    Button minus;
    String timeValue;
    String dayValue;
    CountDownTimer refreshTimer;
    ToggleButton holdButton;
    TextView time;
    Handler repeatHandler;
    Runnable repeatPlus;
    Runnable repeatMinus;
    final int CLICK_INTERVAL = 300;
    //Button settings;
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
        //holdButton = (ToggleButton) findViewById(R.id.BtnHold);
        plus.setLongClickable(true);
        minus.setLongClickable(true);
        repeatHandler = new Handler();
        time = (TextView)findViewById(R.id.time);
        holdSwitch = (android.widget.Switch) findViewById(R.id.scheduleOnOff);
        //settings = (Button) findViewById(R.id.btnsettings);

        doNotUpdateTarget = false;
        // backgroundmanager disabled because too difficult
        /*BackgroundManager manager = new BackgroundManager();
        manager.storeActiveSwitches();*/

       // Button Schedule = (Button) findViewById(R.id.Schedule);

        /*Schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent weekIntent = new Intent(view.getContext(), Weekoverview.class);
                startActivity(weekIntent);
            }
        });*/

        /*tempTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doNotUpdateTarget = true;
                showNumberPicker();
            }
        });*/


        /*settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(view.getContext(), Settings.class);
                startActivity(settingsIntent);
            }
        });*/

        // this part sets the initial values of the target and current temperature
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    targetTemperature = Double.parseDouble(HeatingSystem.get("targetTemperature"));
                    currentTemperature = Double.parseDouble(HeatingSystem.get("currentTemperature"));
                    timeValue = HeatingSystem.get("time");
                    dayValue = HeatingSystem.get("day");
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String weekProgramState = HeatingSystem.get("weekProgramState");
                    if (weekProgramState.equals("off")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holdSwitch.setChecked(false);
                            }
                        });
                    }
                } catch (ConnectException e) {
                    e.printStackTrace();
                }
                ;
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
                new View.OnTouchListener()

                {
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
                updateCurrentTempView();
                updateTimeView();
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

    void setWeekProgramEnabled() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HeatingSystem.put("weekProgramState", "on");
                    try {
                        targetTemperature = Double.parseDouble(HeatingSystem.get("targetTemperature"));
                    } catch (ConnectException e) {
                        e.printStackTrace();
                    }
                } catch (InvalidInputValueException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        System.out.println(targetTemperature);
        updateTargetTempView(); // does not update correctly, maybe thread is not finished
    }


    void updateTargetTempView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempTarget.setText(String.valueOf(targetTemperature) + "\u2103");
            }
        });
    }

    void updateCurrentTempView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempCurrent.setText(String.valueOf(currentTemperature) + "\u2103");
            }
        });
    }

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

    void updateTimeView() {
        String current = (dayValue+", "+timeValue);
        time.setText(current);
    }

    /*void showNumberPicker() {
        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMaxValue(30);
        numberPicker.setMinValue(5);
        numberPicker.setValue((int) targetTemperature);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                targetTemperature = (double) newVal;
            }
        });
        new AlertDialog.Builder(this).setView(numberPicker)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendTargetTempToServer();
                        doNotUpdateTarget = false;
                    }
                })
                .create().show();
    }*/


}
