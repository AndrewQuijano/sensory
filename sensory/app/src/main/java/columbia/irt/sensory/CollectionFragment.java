package columbia.irt.sensory;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import columbia.irt.motion.MotionReceiver;
import columbia.irt.sensors.BarometricAltimeter;
import columbia.irt.sensors.GPSAltimeter;
import columbia.irt.sensors.MagneticFieldSensor;
import columbia.irt.sensors.WifiReceiver;
import columbia.irt.struct.FloorData;

public class CollectionFragment extends Fragment
{
    protected Switch start;
    protected Switch indoors;
    protected Switch center;
    private NumberPicker floor;
    private NumberPicker env_building_mean_floor;
    private EditText env_context;
    protected EditText current_floor_data;
    protected EditText room;
    protected EditText building;
    private String [] floor_options;
    private String [] env_building_mean_floor_options;

    // IP data
    public final static String SQLDatabase = "72.229.36.215";
    public final static int portNumber = 9254;
    private String android_model;

    // Timer stuff
    private Timer tick = new Timer();
    private TimerTask timerTask;

    Toast send_successful;
    Toast send_failed;

    // variables
    private int isIndoor = 0;
    private int isCenter = 0;
    private FloorData f = null;

    private String session_id = null;

    // Sensors from Main
    protected MapsActivity main = null;
    private WifiReceiver wifi = null;
    private GPSAltimeter gps = null;
    private BarometricAltimeter barometer = null;
    private MagneticFieldSensor magneto = null;
    private MotionReceiver motion = null;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ShowToast")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        main = (MapsActivity) getActivity();
        assert main != null;
        wifi = main.wifi;
        gps = main.gps;
        barometer = main.barometer;
        magneto = main.magneto;
        motion = main.motion;

        View rootView = inflater.inflate(R.layout.collection_settings, container, false);

        send_successful = Toast.makeText(getActivity(), "Data sent!", Toast.LENGTH_SHORT);
        send_failed = Toast.makeText(getActivity(), "Data Failed!", Toast.LENGTH_SHORT);

        String DEVICE = android.os.Build.DEVICE;            // Device
        String MODEL = android.os.Build.MODEL;              // Model
        android_model = DEVICE + '-' + MODEL;

        // Connect to GUI
        start = rootView.findViewById(R.id.start);
        indoors = rootView.findViewById(R.id.Indoors);
        center = rootView.findViewById(R.id.center);
        env_context = rootView.findViewById(R.id.env_context);
        room = rootView.findViewById(R.id.room);
        building = rootView.findViewById(R.id.building);
        floor = rootView.findViewById(R.id.floor);
        env_building_mean_floor = rootView.findViewById(R.id.env_floor_picker);
        current_floor_data = rootView.findViewById(R.id.current_floor_data);

        // Fill Floor options
        floor_options = new String[20];
        for (int i = 0; i < 20; i++)
        {
            floor_options[i] = String.valueOf(i + 1);
        }
        floor.setMinValue(1);
        floor.setMaxValue(floor_options.length);
        floor.setDisplayedValues(floor_options);

        // Fill env_build_mean_floor picker
        env_building_mean_floor_options = new String[]{"0-2", "3-5", "6-10", "10-20", "20-50", "50+"};
        env_building_mean_floor.setMinValue(1);
        env_building_mean_floor.setMaxValue(env_building_mean_floor_options.length);
        env_building_mean_floor.setDisplayedValues(env_building_mean_floor_options);

        // Connect Switches/EditText
        start.setOnCheckedChangeListener(new scan());
        center.setOnCheckedChangeListener(new center());
        indoors.setOnCheckedChangeListener(new indoors());
        // Inflate the layout for this fragment
        return rootView;
    }

    public void onViewCreated (@NonNull View view, Bundle savedInstanceState)
    {
        gps.start();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        f = new FloorData(isIndoor, timeStamp, session_id, android_model, room.getText().toString(),
                floor_options[floor.getValue() - 1], building.getText().toString(),
                wifi.getConnectedMAC(), wifi.getConnectedRSSI(), this.isCenter,
                gps.altitude,
                gps.latitude, gps.longitude, gps.vAccuracy, gps.hAccuracy, gps.course, gps.speed,
                barometer.pressure_at_sea_level, barometer.pressure, barometer.barometricAltitude,
                env_context.getText().toString(),
                env_building_mean_floor_options[env_building_mean_floor.getValue() - 1], motion.getActivity(),
                gps.city_name, gps.country_name, magneto.magnetX, magneto.magnetY, magneto.magnetZ,
                wifi.wifi_results);
        current_floor_data.setText(f.toString());
        building.setText(gps.address);
        env_context.setText(gps.env_context);
    }

    protected class indoors implements CompoundButton.OnCheckedChangeListener
    {
        public void onCheckedChanged(CompoundButton compoundButton, final boolean isChecked)
        {
            if(isChecked)
            {
                isIndoor = 1;
            }
            else
            {
                isIndoor = 0;
            }
        }
    }

    protected class center implements CompoundButton.OnCheckedChangeListener
    {
        public void onCheckedChanged(CompoundButton compoundButton, final boolean isChecked)
        {
            if (isChecked)
            {
                isCenter = 1;
            }
            else
            {
                isCenter = 0;
            }
        }
    }

    protected class scan extends TimerTask implements CompoundButton.OnCheckedChangeListener
    {
        public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked)
        {
            // 1 scan per second
            int sampling_rate = 1;

            if (isChecked)
            {
                Log.d("HELLO", "ON");
                // Get data from sensors
                if (tick == null)
                {
                    tick = new Timer();
                }
                if (timerTask == null)
                {
                    timerTask = new scan();
                }
                // Put here time 1,000 milliseconds = 1 second
                tick.schedule(timerTask, 0, 1000 * sampling_rate);
            }
            else
            {
                Log.d("HELLO", "OFF");
                timerTask.cancel();
                tick.cancel();
                timerTask = null;
                tick = null;
                session_id = null;
            }
        }

        public void run()
        {
            try
            {
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
                if (session_id == null)
                {
                    session_id = timeStamp;
                }
                f = new FloorData(isIndoor, timeStamp, session_id, android_model, room.getText().toString(),
                        floor_options[floor.getValue() - 1], building.getText().toString(),
                        wifi.getConnectedMAC(), wifi.getConnectedRSSI(), isCenter,
                        gps.altitude,
                        gps.latitude, gps.longitude, gps.vAccuracy, gps.hAccuracy, gps.course, gps.speed,
                        barometer.pressure_at_sea_level, barometer.pressure, barometer.barometricAltitude,
                        env_context.getText().toString(),
                        env_building_mean_floor_options[env_building_mean_floor.getValue() - 1], motion.getActivity(),
                        gps.city_name, gps.country_name, magneto.magnetX, magneto.magnetY, magneto.magnetZ,
                        wifi.wifi_results);
                current_floor_data.post(() -> current_floor_data.setText(f.toString()));

                // I/O
                Socket clientSocket = new Socket();
                clientSocket.connect(new InetSocketAddress(SQLDatabase, portNumber), 2 * 1000);

                // Send Data
                ObjectOutputStream toServer = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream fromServer = new ObjectInputStream(clientSocket.getInputStream());
                toServer.writeObject(f);
                toServer.flush();

                if(fromServer.readBoolean())
                {
                    send_successful.show();
                }
                else
                {
                    send_failed.show();
                }
            }
            catch(SocketTimeoutException ioe)
            {
                ioe.printStackTrace();
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }
}