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

public class HumiditySensor implements SensorEventListener
{
    private final static String TAG = "MY_SENSOR";
    private static final int SENSOR_DELAY_US = 150000;  // 150ms
    private final Sensor my_Humidity;
    private final SensorManager mSensorManager;

    // THIS VALUE IS A PERCENTAGE!
    public double humidity = -1;

    public HumiditySensor(SensorManager sensorManager)
    {
        mSensorManager = sensorManager;
        my_Humidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
    }

    public void start()
    {
        mSensorManager.registerListener(this, my_Humidity, SENSOR_DELAY_US);
        Log.d(TAG, "Humidity Sensor Started!");
    }

    public void stop()
    {
        mSensorManager.unregisterListener(this);
        Log.d(TAG, "Humidity Sensor Paused/Destroyed!");
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        switch(accuracy)
        {
            case(SENSOR_STATUS_ACCURACY_LOW):
                Log.d(TAG, "Humidity Current Accuracy: LOW");
                break;

            case(SENSOR_STATUS_ACCURACY_MEDIUM):
                Log.d(TAG, "Humidity Current Accuracy: MEDIUM");
                break;

            case(SENSOR_STATUS_ACCURACY_HIGH):
                Log.d(TAG, "Humidity Current Accuracy: HIGH");
                break;

            case(SENSOR_STATUS_NO_CONTACT):
                Log.d(TAG, "Humidity Current Accuracy: NO_CONTACT");
                break;

            case(SENSOR_STATUS_UNRELIABLE):
                Log.d(TAG, "Humidity Current Accuracy: UNRELIABLE");
                break;
        }
    }

    public void onSensorChanged(SensorEvent event)
    {
        humidity = event.values[0];
    }
}
