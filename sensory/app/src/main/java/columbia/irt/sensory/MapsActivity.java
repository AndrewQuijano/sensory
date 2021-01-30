package columbia.irt.sensory;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

import columbia.irt.motion.MotionReceiver;
import columbia.irt.sensors.AudioSensor;
import columbia.irt.sensors.BarometricAltimeter;
import columbia.irt.sensors.BluetoothReceiver;
import columbia.irt.sensors.GPSAltimeter;
import columbia.irt.sensors.HumiditySensor;
import columbia.irt.sensors.LightSensor;
import columbia.irt.sensors.MagneticFieldSensor;
import columbia.irt.sensors.TemperatureSensor;
import columbia.irt.sensors.WifiReceiver;

public class MapsActivity extends FragmentActivity
{
    // Sensor Classes
    protected BluetoothReceiver blueWrapper;
    protected WifiReceiver wifi;
    protected BarometricAltimeter barometer;
    protected LightSensor light;
    protected TemperatureSensor temp;
    protected HumiditySensor humid;
    protected GPSAltimeter gps;
    protected MagneticFieldSensor magneto;
    protected MotionReceiver motion;
    protected AudioSensor audio;

    protected Button collection;
    protected Button set_map;

    // Collection Fragment settings (in case activity changes)
    protected boolean collect = false;
    protected boolean indoors = false;
    protected boolean center = false;

    // Collection Fragment may be bad to leave Timer Information to run
    // As it forgets the moment you leave the fragment
    protected Timer tick = null;
    protected TimerTask timerTask = null;

    // Main Activity should save information of labels
    // so I avoid having to re-label if I accidentally hit map
    protected String env_context = "GS";
    protected String room = "606";
    protected String building = "Lewisohn";
    protected int mean_floor_idx = 1;
    protected int floor_idx = 1;

    @SuppressLint("ShowToast")
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Build Sensors
        SensorManager my_SensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        blueWrapper = new BluetoothReceiver(this);
        motion = new MotionReceiver(this);
        light = new LightSensor(my_SensorManager);
        temp = new TemperatureSensor(my_SensorManager);
        humid = new HumiditySensor(my_SensorManager);
        magneto = new MagneticFieldSensor(my_SensorManager);
        gps = new GPSAltimeter(this);
        barometer = new BarometricAltimeter(my_SensorManager, gps);
        motion = new MotionReceiver(this);
        wifi = new WifiReceiver(this);
        audio = new AudioSensor();

        setContentView(R.layout.activity_maps);
        // Place collection after setContent View to avoid null.
        collection = findViewById(R.id.collect);
        collection.setOnClickListener(new collect());
        set_map = findViewById(R.id.set_map);
        set_map.setOnClickListener(new set_map());

        // A good Reference I found
        // https://www.youtube.com/watch?v=YCFPClPjDIQ
        setFragment(new MapView());
    }

    protected void onStart()
    {
        super.onStart();
        gps.start(this);
        barometer.start();
        magneto.start();
        motion.register(this);
        wifi.registerReceiver(this);

        // Extra
        humid.start();
        light.start();
        temp.start();
        audio.startRecord(this);
    }

    protected void onResume()
    {
        super.onResume();
        gps.start(this);
        barometer.start();
        magneto.start();
        motion.register(this);
        wifi.registerReceiver(this);

        // Extra
        humid.start();
        light.start();
        temp.start();
        audio.resume();
        blueWrapper.registerReceiver(this);
    }

    protected void onPause()
    {
        super.onPause();
        gps.stop();
        barometer.stop();
        magneto.stop();
        motion.unregister(this);
        wifi.registerReceiver(this);

        // Extra
        humid.stop();
        light.stop();
        temp.stop();
        audio.pause();
        blueWrapper.unregisterReceiver(this);
    }

    protected void onDestroy()
    {
        super.onDestroy();
        gps.stop();
        barometer.stop();
        magneto.stop();
        motion.unregister(this);
        wifi.registerReceiver(this);

        humid.stop();
        light.stop();
        temp.stop();
        audio.delete();
        blueWrapper.unregisterReceiver(this);
    }

    private class collect implements View.OnClickListener
    {
        public void onClick(View view)
        {
            setFragment(new CollectionFragment());
        }
    }

    private class set_map implements View.OnClickListener
    {
        public void onClick(View view)
        {
            setFragment(new MapView());
        }
    }

    private void setFragment(Fragment frag)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flayout, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}