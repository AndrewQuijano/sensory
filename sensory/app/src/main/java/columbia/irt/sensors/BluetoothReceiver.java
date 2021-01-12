package columbia.irt.sensors;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_CAMCORDER;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_MICROPHONE;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_VCR;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CAMERA;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CONFERENCING;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_VIDEO_GAMING_TOY;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET;
import static android.bluetooth.BluetoothClass.Device.COMPUTER_DESKTOP;
import static android.bluetooth.BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA;
import static android.bluetooth.BluetoothClass.Device.COMPUTER_LAPTOP;
import static android.bluetooth.BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA;
import static android.bluetooth.BluetoothClass.Device.COMPUTER_SERVER;
import static android.bluetooth.BluetoothClass.Device.COMPUTER_UNCATEGORIZED;
import static android.bluetooth.BluetoothClass.Device.COMPUTER_WEARABLE;
import static android.bluetooth.BluetoothClass.Device.HEALTH_BLOOD_PRESSURE;
import static android.bluetooth.BluetoothClass.Device.HEALTH_DATA_DISPLAY;
import static android.bluetooth.BluetoothClass.Device.HEALTH_GLUCOSE;
import static android.bluetooth.BluetoothClass.Device.HEALTH_PULSE_OXIMETER;
import static android.bluetooth.BluetoothClass.Device.HEALTH_PULSE_RATE;
import static android.bluetooth.BluetoothClass.Device.HEALTH_THERMOMETER;
import static android.bluetooth.BluetoothClass.Device.HEALTH_UNCATEGORIZED;
import static android.bluetooth.BluetoothClass.Device.HEALTH_WEIGHING;
import static android.bluetooth.BluetoothClass.Device.Major.AUDIO_VIDEO;
import static android.bluetooth.BluetoothClass.Device.Major.COMPUTER;
import static android.bluetooth.BluetoothClass.Device.Major.HEALTH;
import static android.bluetooth.BluetoothClass.Device.Major.IMAGING;
import static android.bluetooth.BluetoothClass.Device.Major.MISC;
import static android.bluetooth.BluetoothClass.Device.Major.NETWORKING;
import static android.bluetooth.BluetoothClass.Device.Major.PERIPHERAL;
import static android.bluetooth.BluetoothClass.Device.Major.PHONE;
import static android.bluetooth.BluetoothClass.Device.Major.TOY;
import static android.bluetooth.BluetoothClass.Device.Major.UNCATEGORIZED;
import static android.bluetooth.BluetoothClass.Device.Major.WEARABLE;
import static android.bluetooth.BluetoothClass.Device.PHONE_CELLULAR;
import static android.bluetooth.BluetoothClass.Device.PHONE_CORDLESS;
import static android.bluetooth.BluetoothClass.Device.PHONE_ISDN;
import static android.bluetooth.BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY;
import static android.bluetooth.BluetoothClass.Device.PHONE_SMART;
import static android.bluetooth.BluetoothClass.Device.PHONE_UNCATEGORIZED;
import static android.bluetooth.BluetoothClass.Device.TOY_CONTROLLER;
import static android.bluetooth.BluetoothClass.Device.TOY_DOLL_ACTION_FIGURE;
import static android.bluetooth.BluetoothClass.Device.TOY_GAME;
import static android.bluetooth.BluetoothClass.Device.TOY_ROBOT;
import static android.bluetooth.BluetoothClass.Device.TOY_UNCATEGORIZED;
import static android.bluetooth.BluetoothClass.Device.TOY_VEHICLE;
import static android.bluetooth.BluetoothClass.Device.WEARABLE_GLASSES;
import static android.bluetooth.BluetoothClass.Device.WEARABLE_HELMET;
import static android.bluetooth.BluetoothClass.Device.WEARABLE_JACKET;
import static android.bluetooth.BluetoothClass.Device.WEARABLE_PAGER;
import static android.bluetooth.BluetoothClass.Device.WEARABLE_UNCATEGORIZED;
import static android.bluetooth.BluetoothClass.Device.WEARABLE_WRIST_WATCH;
import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_BONDING;
import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_CLASSIC;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_DUAL;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_LE;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_UNKNOWN;

public class BluetoothReceiver extends BroadcastReceiver implements Runnable
{
    // For Bluetooth timeout
    private static Timer tick;
    private static TimerTask task;
    private Thread blue;
    private final BluetoothReceiver bluetooth;

    private final Context context;
    private final static String TAG = "MY_SCANNER";
    private final BluetoothAdapter BTAdapter;
    private boolean isRegistered = false;
    private final ProgressBar loading;
    protected int isScanning = -1;    // Not initialized
    public HashMap<String, Integer> bluetoothData = new HashMap<>();

    // Henning asked to get all relevant Bluetooth Data
    public String[] BlueAPs = null;
    public Integer[] BlueRSS = null;
    private final ArrayList<BluetoothDevice> blues = new ArrayList<>();
    public String[] bond_status = null;
    public String[] device_type = null;    // Connector Type
    public String[] device_name = null;    // SSID
    public String[] device_sort = null;    // Laptop, computer, etc.
    public HashMap<String, String> device_to_uuid = new HashMap<>();

    // Turn off progress bar on wifi scan complete
    private static long start_time = 0;
    private int counter = 0;
    private int size = 0;

    public BluetoothReceiver(Context context)
    {
        this(context, null);
    }

    public BluetoothReceiver(Context context, ProgressBar loading)
    {
        bluetooth = this;
        this.context = context;
        this.loading = loading;
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (BTAdapter == null)
        {
            Log.d(TAG, "Device does not support BlueTooth!");
        }
        else
        {
            if (!BTAdapter.isEnabled())
            {
                BTAdapter.enable();
            }
        }
        registerReceiver(context);
    }

    public void registerReceiver(Context context)
    {
        if(!isRegistered)
        {
            context.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            context.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
            context.registerReceiver(this, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
            context.registerReceiver(this, new IntentFilter(BluetoothDevice.ACTION_UUID));
            Log.d(TAG, "Wifi/Bluetooth Registered!");
            isRegistered = true;
        }
    }

    public void unregisterReceiver(Context context)
    {
        if(isRegistered)
        {
            context.unregisterReceiver(this);
            isRegistered = false;
            Log.d(TAG, "Wifi/Bluetooth unregistered!");
        }
    }

    // -1, not initialized,
    // 0, not scanning,     GO,   DISCOVERY FINISHED
    // 1, scanning, BUSY,   STOP, DISCOVERY STARTED

    // It may have not been initialized yet as well...
    public boolean blueToothScannerReady()
    {
        //Log.d(TAG, "blueToothReady result is: " + blueReceiver.isScanning);
        return isScanning == 0 || isScanning == -1;
    }

    public void run()
    {
        try
        {
            // LOCK IT!
            isScanning = 1;

            // Reset Bluetooth
            bond_status = null;
            device_type = null;    // Connector Type
            device_name = null;    // SSID
            device_sort = null;    // Laptop, computer, etc.

            if (BTAdapter.isDiscovering())
            {
                Log.d(TAG, "It is discovering now");
                if (!BTAdapter.cancelDiscovery())
                {
                    Log.d(TAG, "Error Cancelling discovery");
                }
                else
                {
                    Log.d(TAG, "Cancelling discovery successful");
                }
            }
            else
            {
                Log.d(TAG, "It is NOT discovering now");
            }

            if(BTAdapter.startDiscovery())
            {
                Log.d(TAG, "Starting to discover...");
            }
            else
            {
                Log.d(TAG, "Starting to discover...FAILED");
            }
            // Sleep for about 2 minutes, to be sure you get interrupt
            Thread.sleep(2 * 60 * 1000);
        }
        catch (InterruptedException e)
        {
            if(BTAdapter.cancelDiscovery())
            {
                Log.d(TAG, "Interrupt received!");
            }
            else
            {
                Log.d(TAG, "Interrupt NOT received!");
            }
        }
    }

    public HashMap<String, String> getBondedDevices()
    {
        HashMap<String, String> device_to_MAC = new HashMap<>();
        Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();

        if (pairedDevices != null)
        {
            if (pairedDevices.size() == 0)
            {
                return null;
            }
            for (BluetoothDevice device : pairedDevices)
            {
                Log.d(TAG, "Paired Name: " + device.getName() + " Paired Bonded: " + device.getAddress());
                device_to_MAC.put(device.getName(), device.getAddress());
            }
        }
        return device_to_MAC;
    }

    // Try this...
    // https://stackoverflow.com/questions/13238600/use-registerreceiver-for-non-activity-and-non-service-class
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action))
        {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(!blues.contains(device))
            {
                blues.add(device);
            }
            int RSS = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
            assert device != null;
            Toast.makeText(context,"NAME: " + device.getName() + " MAC: " + device.getAddress() + " RSS: " + RSS, Toast.LENGTH_SHORT).show();
            bluetoothData.put(device.getAddress(), RSS);
            Log.d(TAG, "Device Name: " + device.getName() + " Bluetooth MAC: " + device.getAddress() + " Bluetooth RSS: " + RSS);
        }

        else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
        {
            int i = 0;
            size = bluetoothData.size();
            // MAC Address, RSSI already obtained
            device_name = new String[size];    // SSID
            device_sort = new String[size];    // Laptop, computer, etc.
            bond_status = new String[size];
            device_type = new String[size];    // Connector Type

            for (BluetoothDevice device: blues)
            {
                if(device.fetchUuidsWithSdp())
                {
                    Log.d(TAG, "Fetching UUID...");
                }
                else
                {
                    Log.d(TAG, "Failed to fetch UUID...");
                }

                device_name[i] = device.getName();
                BluetoothClass dev = device.getBluetoothClass();
                String dev_type = getDevice(dev.getDeviceClass());
                if (dev_type == null)
                {
                    device_sort[i] = getMajorDevice((dev.getDeviceClass()));
                }
                else
                {
                    device_sort[i] = getDevice(dev.getDeviceClass());
                }

                bond_status[i] = getBondedStatus(device.getBondState());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                {
                    device_type[i] = getDeviceType(device.getType());
                }
                else
                {
                    device_type[i] = "NOT_SUPPORTED";
                }
                ++i;
            }

            Log.d(TAG, "Bluetooth Receiver: Ok post the semaphore");
            Log.d(TAG, "size of HashMap: " + size);
            Toast.makeText(context,"Bluetooth Scan Complete! Found " + size + " devices!", Toast.LENGTH_SHORT).show();
            loading.setVisibility(View.INVISIBLE);

            BlueAPs = bluetoothData.keySet().toArray(new String[size]);
            BlueRSS = bluetoothData.values().toArray(new Integer[size]);
            long end_time = System.currentTimeMillis() - start_time;
            Log.d(TAG, "diff:! " + (end_time));
            Toast.makeText(context,"Time to complete scan: " +  end_time/1000 + " seconds", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Time to complete scan: " + end_time/1000 + " seconds");
            cancelBluetoothEarly();

            //=====================Unlock Complete=======================
            isScanning = 0;
        }

        else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
        {
            isScanning = 1;
            // ===============Lock Activated================
            blues.clear();
            bluetoothData.clear();
            Log.d(TAG, "Bluetooth Receiver: Scanning Started!!");
            Toast.makeText(context,"Bluetooth Scan Initialized!", Toast.LENGTH_SHORT).show();
            start_time = System.currentTimeMillis();
            Log.d(TAG, "Start Time NOW: " + start_time);
            device_to_uuid.clear();
            counter = 0;
        }

        else if(BluetoothDevice.ACTION_UUID.equals(action))
        {
            BluetoothDevice btd = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
            StringBuilder sb = new StringBuilder();
            if(uuidExtra != null)
            {
                //List<String> uuids = new ArrayList<>(uuidExtra.length);
                for (Parcelable anUuidExtra : uuidExtra)
                {
                    sb.append(anUuidExtra.toString()).append(',');
                    //uuids.add(uuidExtra[i].toString());
                }
                assert btd != null;
                device_to_uuid.put(btd.getAddress(), sb.toString());
                Log.d(TAG,"ACTION_UUID received for " + btd.getAddress() + " uuid: " + sb.toString());
            }
            else
            {
                assert btd != null;
                device_to_uuid.put(btd.getAddress(), "NULL");
                Log.d(TAG, "ACTION_UUID received for " + btd.getAddress() + " uuid: NULL");
            }
            ++counter;
            if(counter == size)
            {
                Toast.makeText(context,"Bluetooth UUIDs obtained!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getBondedStatus(int type)
    {
        String answer = null;
        switch(type)
        {
            case BOND_BONDED:
                answer = "BOND_BONDED";
                break;
            case BOND_BONDING:
                answer = "BOND_BONDING";
                break;
            case BOND_NONE:
                answer = "BOND_NONE";
                break;
            default:
                break;
        }
        return answer;
    }

    private String getDevice(int type)
    {
        String answer = null;
        switch (type)
        {
            case AUDIO_VIDEO_CAMCORDER:
                answer = "AUDIO_VIDEO_CAMCORDER";
                break;
            case AUDIO_VIDEO_CAR_AUDIO:
                answer = "AUDIO_VIDEO_CAR_AUDIO";
                break;
            case AUDIO_VIDEO_HANDSFREE:
                answer = "AUDIO_VIDEO_HANDSFREE";
                break;
            case AUDIO_VIDEO_HEADPHONES:
                answer = "AUDIO_VIDEO_HEADPHONES";
                break;
            case AUDIO_VIDEO_HIFI_AUDIO:
                answer = "AUDIO_VIDEO_HIFI_AUDIO";
                break;
            case AUDIO_VIDEO_LOUDSPEAKER:
                answer = "AUDIO_VIDEO_LOUDSPEAKER";
                break;
            case AUDIO_VIDEO_MICROPHONE:
                answer = "AUDIO_VIDEO_MICROPHONE";
                break;
            case AUDIO_VIDEO_PORTABLE_AUDIO:
                answer = "AUDIO_VIDEO_PORTABLE_AUDIO";
                break;
            case AUDIO_VIDEO_SET_TOP_BOX:
                answer = "AUDIO_VIDEO_SET_TOP_BOXO";
                break;
            case AUDIO_VIDEO_UNCATEGORIZED:
                answer = "AUDIO_VIDEO_UNCATEGORIZED";
                break;
            case AUDIO_VIDEO_VCR:
                answer = "AUDIO_VIDEO_VCR";
                break;
            case AUDIO_VIDEO_VIDEO_CAMERA:
                answer = "AUDIO_VIDEO_VIDEO_CAMERA";
                break;
            case AUDIO_VIDEO_VIDEO_CONFERENCING:
                answer = "AUDIO_VIDEO_VIDEO_CONFERENCING";
                break;
            case AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER:
                answer = "AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER";
                break;
            case AUDIO_VIDEO_VIDEO_GAMING_TOY:
                answer = "AUDIO_VIDEO_VIDEO_GAMING_TOY";
                break;
            case AUDIO_VIDEO_VIDEO_MONITOR:
                answer = "AUDIO_VIDEO_VIDEO_MONITOR";
                break;
            case AUDIO_VIDEO_WEARABLE_HEADSET:
                answer = "AUDIO_VIDEO_WEARABLE_HEADSET";
                break;
            case COMPUTER_DESKTOP:
                answer = "COMPUTER_DESKTOP";
                break;
            case COMPUTER_HANDHELD_PC_PDA:
                answer = "COMPUTER_HANDHELD_PC_PDA";
                break;
            case COMPUTER_LAPTOP:
                answer = "COMPUTER_LAPTOP";
                break;
            case COMPUTER_PALM_SIZE_PC_PDA:
                answer = "COMPUTER_PALM_SIZE_PC_PDA";
                break;
            case COMPUTER_SERVER:
                answer = "COMPUTER_SERVER";
                break;
            case COMPUTER_UNCATEGORIZED:
                answer = "COMPUTER_UNCATEGORIZED";
                break;
            case COMPUTER_WEARABLE:
                answer = "COMPUTER_WEARABLE";
                break;
            case HEALTH_BLOOD_PRESSURE:
                answer = "HEALTH_BLOOD_PRESSURE";
                break;
            case HEALTH_DATA_DISPLAY:
                answer = "HEALTH_DATA_DISPLAY";
                break;
            case HEALTH_GLUCOSE:
                answer = "HEALTH_GLUCOSE";
                break;
            case HEALTH_PULSE_OXIMETER:
                answer = "HEALTH_PULSE_OXIMETER";
                break;
            case HEALTH_PULSE_RATE:
                answer = "HEALTH_PULSE_RATE";
                break;
            case HEALTH_THERMOMETER:
                answer = "HEALTH_THERMOMETER";
                break;
            case HEALTH_UNCATEGORIZED:
                answer = "HEALTH_UNCATEGORIZED";
                break;
            case HEALTH_WEIGHING:
                answer = "HEALTH_WEIGHING";
                break;
            case PHONE_CELLULAR:
                answer = "PHONE_CELLULAR";
                break;
            case PHONE_CORDLESS:
                answer = "PHONE_CORDLESS";
                break;
            case PHONE_ISDN:
                answer = "PHONE_ISDN";
                break;
            case PHONE_MODEM_OR_GATEWAY:
                answer = "PHONE_MODEM_OR_GATEWAY";
                break;
            case PHONE_SMART:
                answer = "PHONE_SMART";
                break;
            case PHONE_UNCATEGORIZED:
                answer = "PHONE_UNCATEGORIZED";
                break;
            case TOY_CONTROLLER:
                answer = "TOY_CONTROLLER";
                break;
            case TOY_DOLL_ACTION_FIGURE:
                answer = "TOY_DOLL_ACTION_FIGURE";
                break;
            case TOY_GAME:
                answer = "TOY_GAME";
                break;
            case TOY_ROBOT:
                answer = "TOY_ROBOT";
                break;
            case TOY_UNCATEGORIZED:
                answer = "TOY_UNCATEGORIZED";
                break;
            case TOY_VEHICLE:
                answer = "TOY_VEHICLE";
                break;
            case WEARABLE_GLASSES:
                answer = "WEARABLE_GLASSES";
                break;
            case WEARABLE_HELMET:
                answer = "WEARABLE_HELMET";
                break;
            case WEARABLE_JACKET:
                answer = "WEARABLE_JACKET";
                break;
            case WEARABLE_PAGER:
                answer = "WEARABLE_PAGER";
                break;
            case WEARABLE_UNCATEGORIZED:
                answer = "WEARABLE_UNCATEGORIZED";
                break;
            case WEARABLE_WRIST_WATCH:
                answer = "WEARABLE_WRIST_WATCH";
                break;
            default:
                break;
        }
        return answer;
    }

    private String getMajorDevice(int type)
    {
        String answer = null;
        switch (type)
        {
            case AUDIO_VIDEO:
                answer = "AUDIO_VIDEO";
                break;
            case COMPUTER:
                answer = "COMPUTER";
                break;
            case HEALTH:
                answer = "HEALTH";
                break;
            case IMAGING:
                answer = "IMAGING";
                break;
            case MISC:
                answer = "MISC";
                break;
            case NETWORKING:
                answer = "NETWORKING";
                break;
            case PERIPHERAL:
                answer = "PERIPHERAL";
                break;
            case PHONE:
                answer = "PHONE";
                break;
            case TOY:
                answer = "TOY";
                break;
            case UNCATEGORIZED:
                answer = "UNCATEGORIZED";
                break;
            case WEARABLE:
                answer = "WEARABLE";
            default:
                break;
        }
        return answer;
    }

    private String getDeviceType(int type)
    {
        String answer = null;
        switch(type)
        {
            case DEVICE_TYPE_CLASSIC:
                answer = "DEVICE_TYPE_CLASSIC";
                break;

            case DEVICE_TYPE_DUAL:
                answer = "DEVICE_TYPE_DUAL";
                break;

            case DEVICE_TYPE_LE:
                answer = "DEVICE_TYPE_LE";
                break;

            case DEVICE_TYPE_UNKNOWN:
                answer = "DEVICE_TYPE_UNKNOWN";
                break;

            default:
                break;
        }
        return answer;
    }

    // -----------Permit Timing out Bluetooth Scans after a minute -----
    private class bluetooth_timer extends TimerTask implements View.OnClickListener
    {
        public void onClick(View view)
        {
            // If busy scanning! STOP!!!
            if(!bluetooth.blueToothScannerReady())
            {
                Toast.makeText(context.getApplicationContext(), "Wait for scan to complete!", Toast.LENGTH_SHORT).show();
                return;
            }

            loading.setVisibility(View.VISIBLE);
            // If it is not ready stay stuck until scan is ready!
            tick = new Timer();
            task = new bluetooth_timer();
            while(true)
            {
                if(bluetooth.blueToothScannerReady())
                {
                    //final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                    //executorService.scheduleAtFixedRate(App::myTask, 0, 1, TimeUnit.SECONDS);
                    (blue = new Thread(bluetooth)).start();
                    // Wait for 1 minute, then Bluetooth Thread must die!
                    tick.schedule(task, 60 * 1000);
                    // Bluetooth scan complete!
                    break;
                }
            }
        }

        public void run()
        {
            if(blue != null)
            {
                blue.interrupt();
            }
            blue = null;
            if(task != null)
            {
                task.cancel();
            }
            if(tick != null)
            {
                tick.cancel();
                tick.purge();
            }
            task = null;
        }
    }

    public static void cancelBluetoothEarly()
    {
        if(task != null)
        {
            task.cancel();
        }
        if(tick != null)
        {
            tick.cancel();
            tick.purge();
        }
        task = null;
    }
}