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

public class MagneticFieldSensor implements SensorEventListener
{
    private final static String TAG = "MY_SENSOR";
    private static final int SENSOR_DELAY_US = 150000;  // 150ms
    private final Sensor my_Magnet;
    private final SensorManager mSensorManager;

    // Magnetic Field
    public double magnetX = -1;
    public double magnetY = -1;
    public double magnetZ = -1;
    public double totalMagnet = -1;

    public MagneticFieldSensor(SensorManager sensorManager)
    {
        mSensorManager = sensorManager;
        my_Magnet = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void start()
    {
        mSensorManager.registerListener(this, my_Magnet, SENSOR_DELAY_US);
        Log.d(TAG, "Magnetic Field Sensor Started!");
    }

    public void stop()
    {
        mSensorManager.unregisterListener(this);
        Log.d(TAG, "Magnetic Sensor Paused/Destroyed!");
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        switch(accuracy)
        {
            case(SENSOR_STATUS_ACCURACY_LOW):
                Log.d(TAG, "Magnetic Current Accuracy: LOW");
                break;

            case(SENSOR_STATUS_ACCURACY_MEDIUM):
                Log.d(TAG, "Magnetic Accuracy: MEDIUM");
                break;

            case(SENSOR_STATUS_ACCURACY_HIGH):
                Log.d(TAG, "Magnetic Accuracy: HIGH");
                break;

            case(SENSOR_STATUS_NO_CONTACT):
                Log.d(TAG, "Magnetic Accuracy: NO_CONTACT");
                break;

            case(SENSOR_STATUS_UNRELIABLE):
                Log.d(TAG, "Magnetic Accuracy: UNRELIABLE");
                break;
        }
    }

    public void onSensorChanged(SensorEvent event)
    {
        magnetX = event.values[0];
        magnetY = event.values[1];
        magnetZ = event.values[2];
        totalMagnet = Math.sqrt((magnetX*magnetX)+(magnetY*magnetY)+(magnetZ*magnetZ));
    }
}
