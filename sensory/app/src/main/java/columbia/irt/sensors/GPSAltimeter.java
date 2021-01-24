// Documentation suggests to check if GPS is enabled...
// https://stuff.mit.edu/afs/sipb/project/android/docs/training/basics/location/locationmanager.html
// Base Repo: https://github.com/aulanov/altidroid
// https://github.com/aulanov/altidroid/blob/master/src/org/openskydive/altidroid/sensor/GPSAltimeter.java

package columbia.irt.sensors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
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
import java.util.List;
import java.util.Locale;

public class GPSAltimeter implements LocationListener, Runnable
{
    private final static String BASE_URL = "https://maps.googleapis.com/maps/api/elevation/json?locations=";
    //private final static String API_KEY = "&key=" + R.string.API_KEY;
    private final static String API_KEY = "&key=AIzaSyBYJbhGFl17ejbVxeiqlcoSk2epVB3ALd0";
    private final static String TAG = "MY_SENSOR";
    private final LocationManager mLocationManager;

    // GPS stuff
    public double longitude = -1;
    public double latitude = -1;
    public double hAccuracy = -1;
    public double vAccuracy = -1;
    public double course = -1;
    public double speed = -1;
    public double altitude = -1;

    // Other sensory features
    private List<Address> addresses;
    private final Geocoder geocoder;

    public String env_context;
    public String address;
    public String city_name;
    public String country_name;

    private Thread updateAltitude;
    private boolean isThreadRunning = false;

    public GPSAltimeter(Context context)
    {
        geocoder = new Geocoder(context, Locale.getDefault());
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    public void start()
    {
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2 * 1000, 0, this);
        isThreadRunning = true;
        (updateAltitude = new Thread(this)).start();
        Log.d(TAG, "GPS Started!");
    }

    public void stop()
    {
        isThreadRunning = false;
        try
        {
            if(updateAltitude != null)
            {
                updateAltitude.interrupt();
                updateAltitude.join();
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        updateAltitude = null;
        mLocationManager.removeUpdates(this);
        Log.d(TAG, "GPS Paused/Destroyed!");
    }

    public void onLocationChanged(Location loc)
    {
        Log.d(TAG, "LOCATION CHANGED Longitude: " + loc.getLongitude());
        Log.d(TAG, "LOCATION CHANGED Latitude: " + loc.getLatitude());
        longitude = loc.getLongitude();
        latitude = loc.getLatitude();
        hAccuracy = loc.getAccuracy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            vAccuracy = loc.getVerticalAccuracyMeters();
        }
        else
        {
            vAccuracy = -1;
        }

        course = loc.getBearing();
        speed = loc.getSpeed();

        /*------- To get city name from coordinates -------- */
        try
        {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getThoroughfare();
            env_context = addresses.get(0).getFeatureName();
            city_name = addresses.get(0).getLocality();
            country_name = addresses.get(0).getCountryName();
            Log.d(TAG, city_name + country_name);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void onProviderDisabled(String provider)
    {
        Log.d(TAG, "Provider disabled: " + provider);
    }

    public void onProviderEnabled(String provider)
    {
        Log.d(TAG, "Provider enabled: " + provider);
    }

    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        switch(status)
        {
            case(LocationProvider.OUT_OF_SERVICE):
                Log.d(TAG, "Provider: " + provider + " is OUT_OF_SERVICE");
                break;
            case(LocationProvider.TEMPORARILY_UNAVAILABLE):
                Log.d(TAG, "Provider: " + provider + " is TEMPORARILY_UNAVAILABLE");
                break;
            case(LocationProvider.AVAILABLE):
                Log.d(TAG, "Provider: " + provider + " is AVAILABLE");
                break;
        }
    }

    public void updateLocation()
    {
        Location location = getLastKnownLocation();
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        hAccuracy = location.getAccuracy();
        Log.d(TAG, "Build is: " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            vAccuracy = location.getVerticalAccuracyMeters();
        }
        else
        {
            vAccuracy = -1;
        }

        course = location.getBearing();
        speed = location.getSpeed();
        try
        {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getThoroughfare();
            env_context = addresses.get(0).getFeatureName();
            city_name = addresses.get(0).getLocality();
            country_name = addresses.get(0).getCountryName();
            Log.d(TAG, address + "," + env_context + "," + city_name + "," + country_name);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Source: https://stackoverflow.com/questions/20438627/getlastknownlocation-returns-null
    private Location getLastKnownLocation()
    {
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers)
        {
            // Should have been taken care of in Main Activity...
            @SuppressLint("MissingPermission")
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null)
            {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy())
            {
                // Log.d(TAG, "Found best last known location: ");
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public void run()
    {
        // Sources:
        // https://stackoverflow.com/questions/34691175/how-to-send-httprequest-and-get-json-response-in-android
        // https://developers.google.com/maps/documentation/elevation/intro
        while(isThreadRunning)
        {
            try
            {
                updateLocation();
                String server_response;
                int status;

                String url = BASE_URL + latitude + "," + longitude + API_KEY;

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
                    this.altitude = parseAltitude(server_response);
                }
                else
                {
                    Log.d(TAG, "HTTP GET FAILED!");
                }
                // Update every 3 minutes
                Thread.sleep(10*60*1000);
                Log.d(TAG, "Completed updating altitude from sea level!");
                Log.d(TAG, "altitude: " + altitude);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
            catch (InterruptedException e)
            {
                break;
            }
        }
    }

    // If you get permission denied
    // https://stackoverflow.com/questions/32994634/this-api-project-is-not-authorized-to-use-this-api-please-ensure-that-this-api
    private static double parseAltitude(String input) throws JSONException
    {
        JSONObject jObject = new JSONObject(input);
        String status = jObject.getString("status");
        if (status.equals("OK"))
        {
            JSONArray jArray = jObject.getJSONArray("results");
            JSONObject oneObject = jArray.getJSONObject(0);
            // Pulling items from the array
            return oneObject.getDouble("elevation");
        }
        else
        {   Log.d(TAG, "Error message: " + status);
            return -1;
        }
    }
}