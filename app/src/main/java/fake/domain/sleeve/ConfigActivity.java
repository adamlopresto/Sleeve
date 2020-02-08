package fake.domain.sleeve;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ProviderChooserIntent;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConfigActivity extends WearableActivity {

    private NumberPicker angle;

    public static final int PROVIDER_CHOOSER_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        final View topLayout = findViewById(R.id.topLayout);
        angle = findViewById(R.id.anglePicker);

        angle.setMinValue(0);
        angle.setMaxValue(90);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        angle.setValue(prefs.getInt("angle", 80));

        angle.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                prefs.edit().putInt("angle", newVal).commit();
                topLayout.postInvalidate();
            }
        });

        final View btn = findViewById(R.id.complicationButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfigActivity.this.startActivityForResult(
                        ProviderChooserIntent.createProviderChooserIntent(
                                new ComponentName(getApplicationContext(), MyWatchFace.class),
                                0,
                                ComplicationData.TYPE_LONG_TEXT) ,
                        PROVIDER_CHOOSER_REQUEST_CODE);
            }
        });
        // Enables Always-on
        setAmbientEnabled();

        topLayout.getOverlay().add(new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                /*
                 * Save the canvas state before we can begin to rotate it.
                 */
                canvas.save();

                //int deg = new Date().getSeconds() % 20 + 70;
                //canvas.drawText(Integer.toString(deg), mCenterX, mCenterY, mTimePaint );
                int deg = prefs.getInt("angle", 80);

                int height  = canvas.getHeight();
                int width   = canvas.getWidth();
                int centerX = width/2;
                int centerY = height/2;

                Paint mTimePaint = new Paint();
                mTimePaint.setColor(Color.WHITE);
                mTimePaint.setTextAlign(Paint.Align.CENTER);
                mTimePaint.setAntiAlias(true);
                mTimePaint.setTextSize(60f);
                mTimePaint.setStyle(Paint.Style.FILL);
                mTimePaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                SimpleDateFormat timeFormat = new SimpleDateFormat("h mm");

                canvas.rotate(deg, centerX, centerY);
                canvas.translate(0, -height*0.34f);
                canvas.drawText(timeFormat.format(new Date()), centerX, centerY, mTimePaint );

                canvas.restore();
            }

            @Override
            public void setAlpha(int i) {

            }

            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) {

            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSLUCENT;
            }
        });
    }
}
