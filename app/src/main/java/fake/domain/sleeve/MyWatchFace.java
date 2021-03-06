package fake.domain.sleeve;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationText;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't
 * shown. On devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient
 * mode. The watch face is drawn with less contrast in mute mode.
 * <p>
 * Important Note: Because watch face apps do not have a default Activity in
 * their project, you will need to set your Configurations to
 * "Do not launch Activity" for both the Wear and/or Application modules. If you
 * are unsure how to do this, please review the "Run Starter project" section
 * in the Google Watch Face Code Lab:
 * https://codelabs.developers.google.com/codelabs/watchface/index.html#0
 */
public class MyWatchFace extends CanvasWatchFaceService {

    /*
     * Updates rate in milliseconds for interactive mode. Since this face only shows minutes, we
     * only need to update once a minute.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.MINUTES.toMillis(1);
    //private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    //Id value for the one and only (so far) complication
    public static final int complicationId = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<MyWatchFace.Engine> mWeakReference;

        public EngineHandler(MyWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            MyWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        /* Handler to update the time once a second in interactive mode. */
        private final Handler mUpdateTimeHandler = new EngineHandler(this);

        @SuppressLint("SimpleDateFormat")
        private SimpleDateFormat timeFormat = new SimpleDateFormat("h mm");
        private float mCenterX;
        private float mCenterY;
        private float mWidth;
        private float mHeight;

        private Paint mTimePaint;
        private Paint complicationPaint;
        private Paint subtitlePaint;

        private boolean mAmbient;

        private SharedPreferences prefs;

        private String complicationText;
        private String complicationSubtitle;
        private Drawable complicationIcon;
        private PendingIntent complicationIntent;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFace.this)
                    .setAcceptsTapEvents(true)
                    .build());

            initializeComplications();

            initializeWatchFace();
        }

        private void initializeComplications() {
            setActiveComplications(0);
        }

        private void initializeWatchFace() {
            mTimePaint = new Paint();
            mTimePaint.setColor(Color.WHITE);
            mTimePaint.setTextAlign(Paint.Align.CENTER);
            mTimePaint.setAntiAlias(true);
            mTimePaint.setTextSize(60f);
            mTimePaint.setStyle(Paint.Style.FILL);
            mTimePaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

            complicationPaint = new Paint();
            complicationPaint.setColor(Color.WHITE);
            complicationPaint.setTextAlign(Paint.Align.CENTER);
            complicationPaint.setAntiAlias(true);
            complicationPaint.setTextSize(30f);
            complicationPaint.setStyle(Paint.Style.FILL);
            complicationPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

            subtitlePaint = new Paint();
            subtitlePaint.setColor(Color.LTGRAY);
            subtitlePaint.setTextAlign(Paint.Align.CENTER);
            subtitlePaint.setAntiAlias(true);
            subtitlePaint.setTextSize(20f);
            subtitlePaint.setStyle(Paint.Style.FILL);
            subtitlePaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

            prefs = PreferenceManager.getDefaultSharedPreferences(MyWatchFace.this);
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            mAmbient = inAmbientMode;

            updateWatchHandStyle();

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();

            mTimePaint.setAntiAlias(!inAmbientMode);
            complicationPaint.setAntiAlias(!inAmbientMode);
            subtitlePaint.setAntiAlias(!inAmbientMode);

            postInvalidate();
        }

        @Override
        public void onComplicationDataUpdate(int watchFaceComplicationId, ComplicationData data) {
            super.onComplicationDataUpdate(watchFaceComplicationId, data);

            Log.e("Sleeve", "Got new complication data"+data.toString());

            ComplicationText text = data.getLongText();
            if (text != null) {
                complicationText = text.getText(getApplicationContext(), System.currentTimeMillis()).toString();
            } else {
                complicationText = null;
            }

            text = data.getLongTitle();
            if (text != null) {
                complicationSubtitle = text.getText(getApplicationContext(), System.currentTimeMillis()).toString();
            } else {
                complicationSubtitle = null;
            }

            /*
            Icon icon = data.getSmallImage();
            if (icon != null)
                complicationIcon = icon.loadDrawable(getBaseContext());
             */
            complicationIcon = null;

            complicationIntent = data.getTapAction();

            postInvalidate();
        }

        private void updateWatchHandStyle() {
            /*
            if (mAmbient) {
                mTimePaint.setStyle(Paint.Style.STROKE);
            } else {
                mTimePaint.setStyle(Paint.Style.FILL);
            }
             */
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            /*
             * Find the coordinates of the center point on the screen, and ignore the window
             * insets, so that, on round watches with a "chin", the watch face is centered on the
             * entire screen, not just the usable portion.
             */
            mWidth = width;
            mHeight = height;
            mCenterX = width / 2f;
            mCenterY = height / 2f;

        }

        /**
         * Captures tap event (and tap type). The {@link WatchFaceService#TAP_TYPE_TAP} case can be
         * used for implementing specific logic to handle the gesture.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    // TODO: Add code to handle the tap gesture.
                    float fx = x/mWidth;
                    float fy = y/mHeight;

                    if (fx<fy && fy<(1-fx)) {
                        //Left quadrant
                        Intent i = new Intent(getApplicationContext(), ConfigActivity.class);
                        startActivity(i);
                    } else if (fx>fy && fy>1-fx){
                        //Right quadrant
                        if (complicationIntent != null) {
                            try {
                                complicationIntent.send();
                            } catch (PendingIntent.CanceledException ignored) {
                            }
                            ;
                        }
                    } else if (fy < 0.5f){
                        int deg = prefs.getInt("angle", 80);
                        prefs.edit().putInt("angle", deg-1).commit();
                        invalidate();
                        //Toast.makeText(getApplicationContext(), fx+","+fy+" top", Toast.LENGTH_SHORT).show();
                    } else {
                        int deg = prefs.getInt("angle", 80);
                        prefs.edit().putInt("angle", deg+1).commit();
                        invalidate();
                        //Toast.makeText(getApplicationContext(), fx + "," + fy + " bottom", Toast.LENGTH_SHORT).show();
                    }


                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            drawBackground(canvas);
            drawWatchFace(canvas);
        }

        private void drawBackground(Canvas canvas) {
            canvas.drawColor(Color.BLACK);
        }

        private void drawWatchFace(Canvas canvas) {
            /*
             * Save the canvas state before we can begin to rotate it.
             */
            canvas.save();

            //int deg = new Date().getSeconds() % 20 + 70;
            //canvas.drawText(Integer.toString(deg), mCenterX, mCenterY, mTimePaint );
            int deg = prefs.getInt("angle", 80);

            canvas.rotate(deg, mCenterX, mCenterY);
            canvas.translate(0, -mHeight*0.34f);
            canvas.drawText(timeFormat.format(new Date()), mCenterX, mCenterY, mTimePaint );

            if (complicationText != null) {
                canvas.translate(0, mHeight * 0.09f);
                canvas.drawText(complicationText, mCenterX, mCenterY, complicationPaint);
            }

            if (complicationSubtitle != null) {
                canvas.translate(0, mHeight * 0.06f);
                canvas.drawText(complicationSubtitle, mCenterX, mCenterY, subtitlePaint);
            }

            if (complicationIcon != null)
                complicationIcon.draw(canvas);

            canvas.restore();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        /**
         * Starts/stops the {@link #mUpdateTimeHandler} timer based on the state of the watch face.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer
         * should only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !mAmbient;
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}
