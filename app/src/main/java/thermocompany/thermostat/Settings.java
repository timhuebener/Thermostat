package thermocompany.thermostat;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.ConnectException;

import util.CorruptWeekProgramException;
import util.HeatingSystem;
import util.InvalidInputValueException;
import util.WeekProgram;

/**
 * Created by Martijn on 12-6-2016.
 */
public class Settings extends AppCompatActivity {


    EditText daytemp;
    EditText nightTemp;
    Button cancel;
    Button confirm;
    Button importButton;
    Double dayTempValue;
    Double nightTempValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        daytemp = (EditText) findViewById(R.id.dTemp);
        nightTemp = (EditText) findViewById(R.id.nTemp);
        cancel = (Button) findViewById(R.id.btnCancel);
        confirm = (Button) findViewById(R.id.btnConfirm);
        importButton = (Button) findViewById(R.id.importButton);

        HeatingSystem.WEEK_PROGRAM_ADDRESS = HeatingSystem.BASE_ADDRESS + "/weekProgram";

        setTitle("Settings");

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Settings.this)
                        .setMessage("Are you sure you want to import the schedule from server?" +
                                "\n(device schedule will be overwritten)")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                retrieveFromServer();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create().show();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTempsToServer();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetInput();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    dayTempValue = Double.parseDouble(HeatingSystem.get("dayTemperature"));
                    nightTempValue = Double.parseDouble(HeatingSystem.get("nightTemperature"));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            daytemp.setText(String.valueOf(dayTempValue));
                            nightTemp.setText(String.valueOf(nightTempValue));
                        }
                    });
                } catch (ConnectException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

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

                Memory.storeWeekProgram(serverWpg);
            }
        }).start();
    }

    void updateTempsToServer() {
        dayTempValue = Double.parseDouble(daytemp.getText().toString());
        nightTempValue = Double.parseDouble(nightTemp.getText().toString());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HeatingSystem.put("dayTemperature", String.valueOf(dayTempValue));
                    HeatingSystem.put("nightTemperature", String.valueOf(nightTempValue));
                } catch (InvalidInputValueException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void resetInput() {
        daytemp.setText(String.valueOf(dayTempValue));
        nightTemp.setText(String.valueOf(nightTempValue));
    }
}
