package thermocompany.thermostat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.net.ConnectException;

import util.*;

public class MainActivity extends AppCompatActivity {

    TextView tempTarget;
    TextView tempCurrent;
    double targetTemperature;
    double currentTemperature;
    Button plus;
    Button minus;
    CountDownTimer refreshTimer;
    ToggleButton holdButton;
    Handler repeatHandler;
    Runnable repeatPlus;
    Runnable repeatMinus;
    final int CLICK_INTERVAL = 300;
    Button settings;
    Boolean pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HeatingSystem.BASE_ADDRESS = "http://wwwis.win.tue.nl/2id40-ws/60";
        tempTarget = (TextView) findViewById(R.id.temp);
        tempCurrent = (TextView) findViewById(R.id.tempActual);
        plus = (Button) findViewById(R.id.plus);
        minus = (Button) findViewById(R.id.minus);
        holdButton = (ToggleButton) findViewById(R.id.BtnHold);
        plus.setLongClickable(true);
        minus.setLongClickable(true);
        repeatHandler = new Handler();
        settings = (Button) findViewById(R.id.btnsettings);

        pressed = false;

        Button Schedule = (Button) findViewById(R.id.Schedule);

        Schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent weekIntent = new Intent(view.getContext(), Weekoverview.class);
                startActivity(weekIntent);
            }
        });

        tempTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumberPicker();
            }
        });


        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(view.getContext(), Settings.class);
                startActivity(settingsIntent);
            }
        });

        // this part sets the initial values of the target and current temperature
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    targetTemperature = Double.parseDouble(HeatingSystem.get("targetTemperature"));
                    currentTemperature = Double.parseDouble(HeatingSystem.get("currentTemperature"));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTargetTempView();
                            updateCurrentTempView();
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
                                holdButton.setChecked(true);
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
                if (targetTemperature < 25) {
                    targetTemperature = (targetTemperature * 10 + 1) / 10; // to prevent rounding issues
                    updateTargetTempView();
                }
                repeatHandler.postDelayed(repeatPlus, CLICK_INTERVAL);
            }
        };

        repeatMinus = new Runnable() {
            @Override
            public void run() {
                System.out.println("Decreased temp");
                if (targetTemperature > 5) {
                    targetTemperature = (targetTemperature * 10 - 1) / 10; // to prevent rounding issues
                    updateTargetTempView();
                }
                repeatHandler.postDelayed(repeatMinus, CLICK_INTERVAL);
            }
        };

        plus.setOnTouchListener(new View.OnTouchListener()

                                {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {
                                        switch (event.getAction()) {
                                            case MotionEvent.ACTION_DOWN:
                                                repeatHandler.post(repeatPlus);
                                                pressed = true;
                                                break;
                                            case MotionEvent.ACTION_UP:
                                                repeatHandler.removeCallbacks(repeatPlus);
                                                pressed = false;
                                                sendTargetTempToServer(); // only updates to server once done increasing to save bandwidth, good idea?
                                                break;
                                        }
                                        return true;
                                    }
                                }

        );

        minus.setOnTouchListener(new View.OnTouchListener()

                                 {
                                     @Override
                                     public boolean onTouch(View v, MotionEvent event) {
                                         switch (event.getAction()) {
                                             case MotionEvent.ACTION_DOWN:
                                                 repeatHandler.post(repeatMinus);
                                                 pressed = true;
                                                 break;
                                             case MotionEvent.ACTION_UP:
                                                 repeatHandler.removeCallbacks(repeatMinus);
                                                 pressed = false;
                                                 sendTargetTempToServer();
                                                 break;
                                         }
                                         return true;
                                     }
                                 }

        );

        holdButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()

                                              {
                                                  @Override
                                                  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                      if (isChecked) {
                                                          setWeekProgramDisabled();
                                                      } else {
                                                          setWeekProgramEnabled();
                                                      }
                                                  }
                                              }

        );

        refreshTimer = new

                CountDownTimer(1000, 1000) {

                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    currentTemperature = Double.parseDouble(HeatingSystem.get("currentTemperature"));
                                } catch (ConnectException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        System.out.println("test");
                        updateCurrentTempView();
                        if (!pressed) {
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

    void showNumberPicker() {
        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMaxValue(25);
        numberPicker.setMinValue(5);
        numberPicker.setValue((int)targetTemperature);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                targetTemperature = (double)newVal;
                sendTargetTempToServer();
            }
        });
        new AlertDialog.Builder(this).setView(numberPicker)
                /*.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })*/.create().show();
    }


}
