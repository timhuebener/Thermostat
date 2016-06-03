package thermocompany.thermostat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.net.ConnectException;

import util.*;

public class MainActivity extends AppCompatActivity {

    TextView temp;
    double currentTemperature;
    Button plus;
    Button minus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HeatingSystem.BASE_ADDRESS = "http://wwwis.win.tue.nl/2id40-ws/60";
        temp = (TextView)findViewById(R.id.temp);
        plus = (Button)findViewById(R.id.plus);
        minus = (Button)findViewById(R.id.minus);




        // for some internet things we need to use Threads, don't know why
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    currentTemperature = Double.parseDouble(HeatingSystem.get("currentTemperature"));
                    temp.setText(String.valueOf(currentTemperature));
                } catch (ConnectException e) {
                    e.printStackTrace();
                }
            }
        }).start();



        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTemperature = (currentTemperature*10+1)/10; // to prevent rounding issues
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HeatingSystem.put("currentTemperature", String.valueOf(currentTemperature));
                            temp.post(new Runnable() {
                                @Override
                                public void run() {
                                    temp.setText(String.valueOf(currentTemperature));

                                }
                            });
                        } catch (InvalidInputValueException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTemperature = (currentTemperature*10-1)/10;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HeatingSystem.put("currentTemperature", String.valueOf(currentTemperature));
                            temp.post(new Runnable() {
                                @Override
                                public void run() {
                                    temp.setText(String.valueOf(currentTemperature));

                                }
                            });
                        } catch (InvalidInputValueException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }
}
