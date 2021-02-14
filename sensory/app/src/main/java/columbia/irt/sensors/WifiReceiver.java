package columbia.irt.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import columbia.irt.struct.WifiData;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

public class WifiReceiver extends BroadcastReceiver
{
    private final static String TAG = "MAIN_SCANNER";
    private final WifiManager my_wifiManager;
    private boolean isRegistered = false;

    // Wifi Scan Results
    private List<ScanResult> results;
    public WifiData wifi_results;

    public WifiReceiver(Context context)
    {
        my_wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(my_wifiManager == null)
        {
            Log.d(TAG, "WIFI MANAGER IS NULL!");
            System.exit(1);
        }
        else
        {
            if (!my_wifiManager.isWifiEnabled())
            {
                makeText(context.getApplicationContext(), "wifi is disabled...making it enabled", LENGTH_LONG).show();
                my_wifiManager.setWifiEnabled(true);
            }
        }
        registerReceiver(context);
    }

    public void registerReceiver(Context context)
    {
        if(!isRegistered)
        {
            context.registerReceiver(this, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            isRegistered = true;
            makeText(context.getApplicationContext(), "Registered Wifi Receiver!", Toast.LENGTH_SHORT).show();
            get_data();
        }
    }



    // Try this...
    // https://stackoverflow.com/questions/13238600/use-registerreceiver-for-non-activity-and-non-service-class
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action))
        {
            get_data();
            makeText(context.getApplicationContext(), "Got new data from WifiReceiver!", LENGTH_LONG).show();
        }
    }

    public void unregisterReceiver(Context context)
    {
        if(isRegistered)
        {
            context.unregisterReceiver(this);
            isRegistered = false;
        }
    }

    private void get_data()
    {
        if(results != null)
        {
            results.clear();
        }
        results = my_wifiManager.getScanResults();
        wifi_results = new WifiData(results);
    }

    public boolean startScan()
    {
        try
        {
            /*
            if(isRegistered)
            {
                this.unregisterReceiver(context);
                this.registerReceiver(context);
            }
            else
            {
                this.registerReceiver(context);
                this.unregisterReceiver(context);
            }
            return true;
             */
            boolean result = my_wifiManager.startScan();
            if(result)
            {
                get_data();
                Log.d("SCAN", "START SCAN");
                for (ScanResult res: results)
                {
                    Log.d("SCAN RESULT: ", res.toString());
                }
                Log.d("SCAN", "END SCAN");
            }
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public String getConnectedMAC()
    {
        return my_wifiManager.getConnectionInfo().getBSSID();
    }

    public int getConnectedRSSI()
    {
        return my_wifiManager.getConnectionInfo().getRssi();
    }
}