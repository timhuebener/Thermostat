package thermocompany.thermostat;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class Weekoverview extends Activity {

    Button mon;
    Button tue;
    Button wed;
    Button thu;
    Button fri;
    Button sat;
    Button sun;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekoverview);

        mon = (Button)findViewById(R.id.monday);
        tue = (Button)findViewById(R.id.tuesday);
        wed = (Button)findViewById(R.id.wednesday);
        thu = (Button)findViewById(R.id.thursday);
        fri = (Button)findViewById(R.id.friday);
        sat = (Button)findViewById(R.id.saturday);
        sun = (Button)findViewById(R.id.sunday);


    }
}
