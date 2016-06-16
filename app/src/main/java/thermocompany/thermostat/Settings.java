package thermocompany.thermostat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

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
    Button importButton;
    Button setDefaultButton;
    double dayTempValue;
    double nightTempValue;
    WeekProgram wpg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        daytemp = (EditText) findViewById(R.id.dTemp);
        nightTemp = (EditText) findViewById(R.id.nTemp);
        cancel = (Button) findViewById(R.id.btnCancel);
        importButton = (Button) findViewById(R.id.importSchedule);
        setDefaultButton = (Button) findViewById(R.id.setDefault);

        HeatingSystem.WEEK_PROGRAM_ADDRESS = HeatingSystem.BASE_ADDRESS + "/weekProgram";

        setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        setDefaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Settings.this)
                        .setMessage("Are you sure you want to reset the schedule?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setDefault();
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

    // retrieves the weekprogram from server and stores in memory
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
        Toast.makeText(getApplicationContext(), "Schedule imported from server", Toast.LENGTH_LONG).show();
    }

    // sends the night and day temperatures to the server
    void updateTempsToServer() {
        if (Double.parseDouble(daytemp.getText().toString()) > 30 ||
                Double.parseDouble(nightTemp.getText().toString()) > 30) {
            Toast.makeText(getApplicationContext(), "Temperature can not be greater than 30 degrees", Toast.LENGTH_SHORT).show();
            resetInput();
            return;
        }
        if (Double.parseDouble(daytemp.getText().toString()) < 5 ||
                Double.parseDouble(nightTemp.getText().toString()) < 5) {
            Toast.makeText(getApplicationContext(), "Temperature can not be lower than 5 degrees", Toast.LENGTH_SHORT).show();
            resetInput();
            return;
        }

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
        Toast.makeText(getApplicationContext(), "Temperatures updated", Toast.LENGTH_LONG).show();
    }

    // resets the input
    void resetInput() {
        daytemp.setText(String.valueOf(dayTempValue));
        nightTemp.setText(String.valueOf(nightTempValue));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.save:
                updateTempsToServer();
                break;
            // back button
            case 16908332:
                System.out.println("Back");
                NavUtils.navigateUpFromSameTask(this);
                break;
        }

        return true;
    }

    // clears the schedule
    void setDefault() {
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
                wpg.setDefault();
                Memory.storeWeekProgram(wpg);
                HeatingSystem.setWeekProgram(wpg);
            }
        }).start();
        Toast.makeText(getApplicationContext(), "Schedule has been reset", Toast.LENGTH_LONG).show();

    }
}
