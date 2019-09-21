package fake.domain.sleeve;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;
import android.widget.NumberPicker;
import android.widget.TextView;

public class ConfigActivity extends WearableActivity {

    private NumberPicker angle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        angle = findViewById(R.id.anglePicker);

        angle.setMinValue(0);
        angle.setMaxValue(90);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        angle.setValue(prefs.getInt("angle", 80));

        angle.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                prefs.edit().putInt("angle", newVal).commit();
            }
        });
        // Enables Always-on
        setAmbientEnabled();
    }
}
