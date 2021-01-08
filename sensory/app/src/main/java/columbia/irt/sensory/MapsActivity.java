package columbia.irt.sensory;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{
    private GoogleMap mMap;

    // Sensor Classes
    private WifiReceiver wifiReceiver;
    private BluetoothReceiver blueWrapper;
    private BarometricAltimeter barometer;
    private LightSensor light;
    private TemperatureSensor temp;
    private HumiditySensor humid;
    private GPSAltimeter gps;
    private MagneticFieldSensor magneto;
    private AudioSensor audio;
    private MotionReceiver motion;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, 1001);
        }

        // Build Sensors
        SensorManager my_SensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        blueWrapper = new BluetoothReceiver(this);
        wifiReceiver = new WifiReceiver(this);
        motion = new MotionReceiver(this);
        light = new LightSensor(my_SensorManager);
        temp = new TemperatureSensor(my_SensorManager);
        humid = new HumiditySensor(my_SensorManager);
        magneto = new MagneticFieldSensor(my_SensorManager);
        gps = new GPSAltimeter(this);
        audio = new AudioSensor();
        barometer = new BarometricAltimeter(my_SensorManager, gps);
        motion = new MotionReceiver(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng current = new LatLng(gps.latitude, gps.longitude);
        mMap.addMarker(new MarkerOptions().position(current).title("Marker"));

        // Move in with Camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current,15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    protected void onStart()
    {
        super.onStart();
        //File file = FileUtil.createFile(this, "temp.amr");
        // audio.start() is implicitly called here
        //audio.startRecord(this, file);
    }

    protected void onResume()
    {
        super.onResume();
        gps.start();
        barometer.start();
        humid.start();
        light.start();
        temp.start();
        magneto.start();
        //audio.resume();
        motion.register(this);

        if(blueWrapper != null)
        {
            blueWrapper.registerReceiver(this);
        }
    }

    protected void onPause()
    {
        super.onPause();
        gps.stop();
        barometer.stop();
        humid.stop();
        light.stop();
        temp.stop();
        magneto.stop();
        motion.unregister(this);

        // stop is implicitly called
        // file is deleted with recording...
        // audio.pause();
        if(blueWrapper != null)
        {
            blueWrapper.unregisterReceiver(this);
        }
    }

    protected void onDestroy()
    {
        super.onDestroy();
        gps.stop();
        barometer.stop();
        humid.stop();
        light.stop();
        temp.stop();
        magneto.stop();
        motion.unregister(this);

        // stop is implicitly called
        // file is deleted with recording...
        // audio.delete();

        if(blueWrapper != null)
        {
            blueWrapper.unregisterReceiver(this);
        }
    }
}