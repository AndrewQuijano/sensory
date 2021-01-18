package columbia.irt.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.util.List;

import columbia.irt.struct.WifiData;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class WifiReceiver extends BroadcastReceiver implements Runnable
{
    private final static String TAG = "MAIN_SCANNER";
    private final ProgressBar loading;
    private final WifiManager my_wifiManager;
    private boolean isRegistered = false;

    // Wifi Scan Results
    private List<ScanResult> results;
    public WifiData wifi_results;

    private final Context context;

    public WifiReceiver(Context context)
    {
        this(context, null);
    }

    public WifiReceiver(Context context, ProgressBar loading)
    {
        this.loading = loading;
        this.context = context;
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
            get_data();
            makeText(context.getApplicationContext(), "Registered Wifi Receiver!", LENGTH_SHORT).show();
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

    private void get_data()
    {
        if(results != null)
        {
            results.clear();
        }
        results = my_wifiManager.getScanResults();
        wifi_results = new WifiData(results);

        if (loading != null)
        {
            loading.setVisibility(View.INVISIBLE);
        }
    }

    public boolean startScan()
    {
        try
        {
            run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void run()
    {
        this.unregisterReceiver(context);
        this.registerReceiver(context);
        get_data();
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