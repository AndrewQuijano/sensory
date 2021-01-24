// Source:
// https://github.com/aulanov/altidroid/blob/master/src/org/openskydive/altidroid/sensor/BarometricAltimeter.java

package columbia.irt.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_NO_CONTACT;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;


public class BarometricAltimeter implements SensorEventListener, Runnable
{
    private final static String TAG = "MY_SENSOR";
    // Example
    // http://api.openweathermap.org/data/2.5/weather?lat=35&lon=139&appid=570f67646d08ce404c4cc02d3d1bb406
    private final static String URL = "http://api.openweathermap.org/data/2.5/weather?";
    private final static String API = "&appid=570f67646d08ce404c4cc02d3d1bb406";
    private final static int SENSOR_DELAY_US = 150000;  // 150ms
    private final Sensor mBarometer;
    private final SensorManager mSensorManager;
    private boolean isThreadRun = false;

    // Get Values
    public double pressure = -1;
    public double pressure_at_sea_level = -1;
    public double barometricAltitude = -1;
    public String current_weather = null;
    private Thread update;
    private final GPSAltimeter gps;

    public BarometricAltimeter(SensorManager sensorManager, GPSAltimeter gps)
    {
        mSensorManager = sensorManager;
        mBarometer = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        this.gps = gps;
    }

    public void start()
    {
        mSensorManager.registerListener(this, mBarometer, SENSOR_DELAY_US);
        isThreadRun = true;
        (update = new Thread(this)).start();
        Log.d(TAG, "Barometer Started!");
    }

    public void stop()
    {
        isThreadRun = false;
        if(update != null)
        {
            update.interrupt();
            try
            {
                update.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        update = null;
        mSensorManager.unregisterListener(this);
        Log.d(TAG, "Barometer Paused/Destroyed!");
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        switch(accuracy)
        {
            case(SENSOR_STATUS_ACCURACY_LOW):
                Log.d(TAG, "BAROMETER Current Accuracy: LOW");
                break;

            case(SENSOR_STATUS_ACCURACY_MEDIUM):
                Log.d(TAG, "BAROMETER Current Accuracy: MEDIUM");
                break;

            case(SENSOR_STATUS_ACCURACY_HIGH):
                Log.d(TAG, "BAROMETER Current Accuracy: HIGH");
                break;

            case(SENSOR_STATUS_NO_CONTACT):
                Log.d(TAG, "BAROMETER Current Accuracy: NO_CONTACT");
                break;

            case(SENSOR_STATUS_UNRELIABLE):
                Log.d(TAG, "BAROMETER Current Accuracy: UNRELIABLE");
                break;
        }
    }

    public void onSensorChanged(SensorEvent event)
    {
        Log.d(TAG, "BAROMETER CHANGED");
        pressure = event.values[0];
        Log.d(TAG, event.values.length + " Size of baro event");
        for (double i : event.values)
        {
            Log.d(TAG, "VALUE: " + i);
        }
        // I may need an API to get Barometric pressure at my location instead of standard for more accuracy...
        barometricAltitude = SensorManager.getAltitude((float) pressure_at_sea_level, (float) pressure);
        if(Double.isNaN(barometricAltitude))
        {
            Log.d(TAG, "FAILED TO COMPUTE ALTITUDE");
            barometricAltitude = -1.0;
        }
    }

    public void run()
    {
        while (isThreadRun)
        {
            try
            {
                String server_response;
                int status;
                gps.updateLocation();
                String url = URL + "lat=" + gps.latitude + "&lon=" + gps.longitude + API;
                Log.d(TAG, url);
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(url);
                HttpResponse response = client.execute(request);
                if (response == null)
                {
                    status = 404;
                }
                else
                {
                    status = response.getStatusLine().getStatusCode();
                }
                if(status == 200)
                {
                    server_response = EntityUtils.toString(response.getEntity());
                    pressure_at_sea_level = parse_Sea_Level(server_response);
                }
                Log.d(TAG, "pressure at sea: " + pressure_at_sea_level);
                Log.d(TAG, "pressure measured: " + pressure);
                Log.d(TAG, "barometric altitude: " + barometricAltitude);

                // PERMITTED ONLY 1 TIME PER 10 MINUTES, PUTTING 1 MORE MINUTE AS SLACK!
                Thread.sleep(11 * 60 * 1000);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            catch (InterruptedException e)
            {
                break;
            }
        }
    }

    // The unit seems to be hPa for Barometric pressure.
    // I'm unsure what the difference is between sea_level and pressure field though...
    // If you get permission denied
    // https://stackoverflow.com/questions/32994634/this-api-project-is-not-authorized-to-use-this-api-please-ensure-that-this-api
    private double parse_Sea_Level(String input) throws JSONException
    {
        JSONObject jObject = new JSONObject(input);
        JSONObject main = jObject.getJSONObject("main");
        JSONArray current = jObject.getJSONArray("weather");
        current_weather = current.getJSONObject(0).getString("description");
        Log.d(TAG, "Weather is now: " + current_weather);
        try
        {
            return main.getDouble("sea_level");
        }
        catch (JSONException e)
        {
            Log.d(TAG, "sea_level invalid...");
            return main.getDouble("pressure");
        }
    }
}