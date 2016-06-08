package thermocompany.thermostat;

import android.content.Intent;
import android.os.CountDownTimer;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.net.ConnectException;

import util.*;

public class MainActivity extends Activity {

    TextView tempTarget;
    TextView tempCurrent;
    double targetTemperature;
    double currentTemperature;
    Button plus;
    Button minus;
    CountDownTimer refreshTimer;
    ToggleButton holdButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HeatingSystem.BASE_ADDRESS = "http://wwwis.win.tue.nl/2id40-ws/60";
        tempTarget = (TextView)findViewById(R.id.temp);
        tempCurrent = (TextView)findViewById(R.id.tempActual);
        plus = (Button)findViewById(R.id.plus);
        minus = (Button)findViewById(R.id.minus);
        holdButton = (ToggleButton)findViewById(R.id.BtnHold);

        Button Schedule = (Button)findViewById(R.id.Schedule);


        Schedule.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent weekIntent = new Intent (view.getContext(), Weekoverview.class);
                startActivity(weekIntent);
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
                            tempTarget.setText(String.valueOf(targetTemperature)+ "\u2103");
                            tempCurrent.setText(String.valueOf(currentTemperature)+ "\u2103");
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
                    String temp = HeatingSystem.get("weekProgramState");
                    if (temp.equals("off")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holdButton.setChecked(true);
                            }
                        });
                    }
                } catch (ConnectException e) {
                    e.printStackTrace();
                };
            }
        }).start();





        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                targetTemperature = (targetTemperature*10+1)/10; // to prevent rounding issues
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HeatingSystem.put("currentTemperature", String.valueOf(targetTemperature));
                            tempTarget.post(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            tempTarget.setText(String.valueOf(targetTemperature)+ "\u2103");

                                        }
                                    });

                                }
                            });
                        } catch (InvalidInputValueException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        //TODO get the longClickListeners to work
        //Don't know why this doesn't work

        /*plus.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public void onLongClick(View v){
                targetTemperature = (targetTemperature*10+1)/10;
                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        try{
                            HeatingSystem.put("currentTemperature", String.valueOf(targetTemperature));
                            tempTarget.post(new Runnable(){
                                @Override
                                public void run(){
                                    tempTarget.setText(String.valueOf(targetTemperature) + " \u2103");
                                }
                            });

                        }catch (InvalidInputValueException e){
                            e.printStackTrace();
                        }

                    }

                }).start();

            }

        });

        */





        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                targetTemperature = (targetTemperature*10-1)/10;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HeatingSystem.put("currentTemperature", String.valueOf(targetTemperature));
                            tempTarget.post(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            tempTarget.setText(String.valueOf(targetTemperature)+" \u2103");

                                        }
                                    });

                                }
                            });
                        } catch (InvalidInputValueException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        holdButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setWeekProgramDisabled();
                }
                else {
                    setWeekProgramEnabled();
                }
            }
        });

        refreshTimer = new CountDownTimer(1000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                refreshCurrent();
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
                } catch (InvalidInputValueException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void refreshCurrent() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    currentTemperature = Double.parseDouble(HeatingSystem.get("currentTemperature"));
                    targetTemperature = Double.parseDouble(HeatingSystem.get("targetTemperature"));
                    refreshTimer.start();
                } catch (ConnectException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempCurrent.setText(String.valueOf(currentTemperature)+" \u2103");
            }
        });
    }
}
