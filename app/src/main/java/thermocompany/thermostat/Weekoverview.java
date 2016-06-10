package thermocompany.thermostat;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.net.ConnectException;

import util.CorruptWeekProgramException;
import util.HeatingSystem;
import util.WeekProgram;

public class Weekoverview extends AppCompatActivity {

    Button mon;
    Button tue;
    Button wed;
    Button thu;
    Button fri;
    Button sat;
    Button sun;
    //Button setDefault;
    static String day;
    WeekProgram wpg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekoverview);

        HeatingSystem.WEEK_PROGRAM_ADDRESS = HeatingSystem.BASE_ADDRESS + "/weekProgram";

        mon = (Button)findViewById(R.id.monday);
        tue = (Button)findViewById(R.id.tuesday);
        wed = (Button)findViewById(R.id.wednesday);
        thu = (Button)findViewById(R.id.thursday);
        fri = (Button)findViewById(R.id.friday);
        sat = (Button)findViewById(R.id.saturday);
        sun = (Button)findViewById(R.id.sunday);
        //setDefault = (Button)findViewById(R.id.setDefault);

        setTitle("Week Overview");


        mon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToDay(v);
                setLastClickedDay("Monday");
            }
        });

        tue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToDay(v);
                setLastClickedDay("Tuesday");
            }
        });

        wed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToDay(v);
                setLastClickedDay("Wednesday");
            }
        });

        thu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToDay(v);
                setLastClickedDay("Thursday");
            }
        });

        fri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToDay(v);
                setLastClickedDay("Friday");
            }
        });

        sat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToDay(v);
                setLastClickedDay("Saturday");
            }
        });

        sun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToDay(v);
                setLastClickedDay("Sunday");
            }
        });

        /*setDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Weekoverview.this)
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
        });*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_weekoverview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.reset:
                new AlertDialog.Builder(Weekoverview.this)
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
                break;
        }

        return true;
    }

    void switchToDay(View view) {
        Intent dayIntent = new Intent (view.getContext(), day.class);
        startActivity(dayIntent);
    }

    public static String getLastClickedDay() {
        return day;
    }

    void setLastClickedDay(String day) {
        this.day = day;
    }

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
                HeatingSystem.setWeekProgram(wpg);
            }
        }).start();

    }
}
