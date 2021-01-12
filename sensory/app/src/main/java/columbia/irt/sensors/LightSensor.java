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

public class LightSensor implements SensorEventListener
{
    private final static String TAG = "MY_SENSOR";
    private static final int SENSOR_DELAY_US = 150000;  // 150ms
    private final Sensor mLight;
    private final SensorManager mSensorManager;

    public double lux = -1;

    public LightSensor(SensorManager sensorManager)
    {
        mSensorManager = sensorManager;
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    public void start()
    {
        mSensorManager.registerListener(this, mLight, SENSOR_DELAY_US);
        Log.d(TAG, "Light Sensor Started!");
    }

    public void stop()
    {
        mSensorManager.unregisterListener(this);
        Log.d(TAG, "Light Sensor Paused/Destroyed!");
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        switch(accuracy)
        {
            case(SENSOR_STATUS_ACCURACY_LOW):
                Log.d(TAG, "Light Current Accuracy: LOW");
                break;

            case(SENSOR_STATUS_ACCURACY_MEDIUM):
                Log.d(TAG, "Light Current Accuracy: MEDIUM");
                break;

            case(SENSOR_STATUS_ACCURACY_HIGH):
                Log.d(TAG, "Light Current Accuracy: HIGH");
                break;

            case(SENSOR_STATUS_NO_CONTACT):
                Log.d(TAG, "Light Current Accuracy: NO_CONTACT");
                break;

            case(SENSOR_STATUS_UNRELIABLE):
                Log.d(TAG, "Light Current Accuracy: UNRELIABLE");
                break;
        }
    }

    public void onSensorChanged(SensorEvent event)
    {
        lux = event.values[0];
    }
}
