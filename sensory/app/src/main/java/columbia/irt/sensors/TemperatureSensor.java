package columbia.irt.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_NO_CONTACT;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;

public class TemperatureSensor implements SensorEventListener
{
    private final static String TAG = "MY_SENSOR";
    private static final int SENSOR_DELAY_US = 150000;  // 150ms
    private final Sensor mThermometer;
    private final SensorManager mSensorManager;

    // Get Values
    // Temperature sensor doesn't exist on my phone...
    // So obviously Absolute Zero isn't a value that can happen in a room!
    public double temperature = -273;

    public TemperatureSensor(SensorManager sensorManager)
    {
        mSensorManager = sensorManager;
        mThermometer = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
    }

    public void start()
    {
        mSensorManager.registerListener(this, mThermometer, SENSOR_DELAY_US);
    }

    public void stop()
    {
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        switch(accuracy)
        {
            case(SENSOR_STATUS_ACCURACY_LOW):
                Log.d(TAG, "Temperature Current Accuracy: LOW");
                break;

            case(SENSOR_STATUS_ACCURACY_MEDIUM):
                Log.d(TAG, "Temperature Current Accuracy: MEDIUM");
                break;

            case(SENSOR_STATUS_ACCURACY_HIGH):
                Log.d(TAG, "Temperature Current Accuracy: HIGH");
                break;

            case(SENSOR_STATUS_NO_CONTACT):
                Log.d(TAG, "Temperature Current Accuracy: NO_CONTACT");
                break;

            case(SENSOR_STATUS_UNRELIABLE):
                Log.d(TAG, "Temperature Current Accuracy: UNRELIABLE");
                break;
        }
    }

    public void onSensorChanged(SensorEvent event)
    {
        temperature = event.values[0];
    }
}
