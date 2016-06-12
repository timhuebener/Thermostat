package thermocompany.thermostat;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Martijn on 12-6-2016.
 */
public class Settings extends Activity {


    EditText daytemp;
    EditText nightTemp;
    Button cancel;
    Button confirm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        daytemp = (EditText)findViewById(R.id.dTemp);
        nightTemp = (EditText)findViewById(R.id.nTemp);
        cancel = (Button)findViewById(R.id.btnCancel);
        confirm = (Button)findViewById(R.id.btnConfirm);

    }
}
