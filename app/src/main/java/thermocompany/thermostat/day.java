package thermocompany.thermostat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Day extends AppCompatActivity {

    String day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        day = Weekoverview.getLastClickedDay();
        setTitle(day);
    }
}
