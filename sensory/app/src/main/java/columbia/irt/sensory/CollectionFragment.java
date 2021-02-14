package columbia.irt.sensory;

import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
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
    protected NumberPicker floor;
    protected NumberPicker env_building_mean_floor;
    private EditText env_context;
    protected EditText current_floor_data;
    protected EditText room;
    protected EditText building;
    private String [] floor_options;
    private String [] env_building_mean_floor_options;

    // IP data
    protected final static String SQLDatabase = "72.229.36.215";
    protected final static int portNumber = 9254;
    private String android_model;

    // Show message
    private Toast send_successful;
    private Toast send_failed;
    private Toast timeout;
    private Toast io_exception;

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

        send_successful = Toast.makeText(main, "Data sent!", Toast.LENGTH_SHORT);
        send_failed = Toast.makeText(main, "Data Failed!", Toast.LENGTH_SHORT);
        timeout = Toast.makeText(main, "Timeout Exception! Is Collection Server online?", Toast.LENGTH_SHORT);
        io_exception = Toast.makeText(main, "IO Exception! Are you on Wi-Fi?", Toast.LENGTH_SHORT);

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
        Log.d("CURRENT-FLOOR-IDX-NOW", String.valueOf(main.floor_idx));
        floor.setValue(main.floor_idx);
        Log.d("CURRENT-FLOOR-IDX-CHG", String.valueOf(floor.getValue()));

        floor.setOnValueChangedListener(new listen_floor());

        // Fill env_build_mean_floor picker
        env_building_mean_floor_options = new String[]{"0-2", "3-5", "6-10", "10-20", "20-50", "50+"};
        env_building_mean_floor.setMinValue(1);
        env_building_mean_floor.setMaxValue(env_building_mean_floor_options.length);
        env_building_mean_floor.setDisplayedValues(env_building_mean_floor_options);
        env_building_mean_floor.setValue(main.mean_floor_idx);
        env_building_mean_floor.setOnValueChangedListener(new listen_mean_floor());

        // Connect Switches/EditText
        start.setOnCheckedChangeListener(new scan());
        center.setOnCheckedChangeListener(new center());
        indoors.setOnCheckedChangeListener(new indoors());
        room.addTextChangedListener(new listen_room());
        env_context.addTextChangedListener(new listen_env_context());
        building.addTextChangedListener(new listen_building());

        // Pull Information from last time fragment was executed
        if(main.collect)
        {
            Toast.makeText(main, "Was Collecting. Now killing", Toast.LENGTH_SHORT).show();
            // Kill any Running Tasks
            main.timerTask.cancel();
            main.tick.cancel();
            main.timerTask = null;
            main.tick = null;
            session_id = null;
        }
        start.setChecked(main.collect);
        center.setChecked(main.center);
        indoors.setChecked(main.indoors);

        // Set all values based on Main Activity's memory of last time
        env_context.setText(main.env_context);
        room.setText(main.room);
        building.setText(main.building);

        // Inflate the layout for this fragment
        return rootView;
    }

    // https://stackoverflow.com/questions/14625423/detect-when-user-enters-data-into-edittext-immediately-shows-answer
    private class listen_building implements TextWatcher
    {
        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count)
        {
            // Before I open EditText
        }

        public void onTextChanged(CharSequence charSequence, int start, int before, int count)
        {
            // While text is changing
        }

        public void afterTextChanged(Editable editable)
        {
            /*
            if(main.building.equals(""))
            {
                main.building = gps.address;
                building.setText(gps.address);
            }
            */
            // Upon exiting Edit Text
            main.building = building.getText().toString();
            Log.d("TEXT-BUILDING", main.building);
        }
    }

    private class listen_room implements TextWatcher
    {
        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count)
        {
            // Before I open EditText
        }

        public void onTextChanged(CharSequence charSequence, int start, int before, int count)
        {
            // While text is changing
        }

        public void afterTextChanged(Editable editable)
        {
            // Upon exiting Edit Text
            main.room = room.getText().toString();
            Log.d("TEXT-ROOM", main.building);
        }
    }

    private class listen_env_context implements TextWatcher
    {
        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count)
        {
            // Before I open EditText
        }

        public void onTextChanged(CharSequence charSequence, int start, int before, int count)
        {
            // While text is changing
        }

        public void afterTextChanged(Editable editable)
        {
            /*
            if(main.env_context.equals(""))
            {
                main.env_context = gps.env_context;
                env_context.setText(gps.env_context);
            }
            */
            // Upon exiting Edit Text
            main.env_context = env_context.getText().toString();
            Log.d("TEXT-ENV", main.env_context);
        }
    }

    // https://stackoverflow.com/questions/14185317/adding-a-listener-to-a-number-picker-widget
    private class listen_floor implements NumberPicker.OnValueChangeListener
    {
        public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal)
        {
            main.floor_idx = newVal;
            Log.d("FLOOR", String.valueOf(main.floor_idx));
        }
    }

    private class listen_mean_floor implements NumberPicker.OnValueChangeListener
    {
        public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal)
        {
            main.mean_floor_idx = newVal;
            Log.d("MEAN-FLOOR", String.valueOf(main.mean_floor_idx));
        }
    }

    public void onViewCreated (@NonNull View view, Bundle savedInstanceState)
    {
        gps.start(main);
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        f = new FloorData(isIndoor, timeStamp, session_id, android_model, main.room,
                floor_options[main.floor_idx - 1], main.building,
                wifi.getConnectedMAC(), wifi.getConnectedRSSI(), this.isCenter,
                gps.altitude,
                gps.latitude, gps.longitude, gps.vAccuracy, gps.hAccuracy, gps.course, gps.speed,
                barometer.pressure_at_sea_level, barometer.pressure, barometer.barometricAltitude,
                main.env_context,
                env_building_mean_floor_options[main.mean_floor_idx - 1], motion.getActivity(),
                gps.city_name, gps.country_name, magneto.magnetX, magneto.magnetY, magneto.magnetZ,
                wifi.wifi_results);
        current_floor_data.setText(f.toString());
    }

    protected class indoors implements CompoundButton.OnCheckedChangeListener
    {
        public void onCheckedChanged(CompoundButton compoundButton, final boolean isChecked)
        {
            if(isChecked)
            {
                isIndoor = 1;
                main.indoors = true;
            }
            else
            {
                main.indoors = false;
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
                main.indoors = true;
            }
            else
            {
                main.indoors = false;
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

            // Main Fragment should remember status in case you forgot...
            if (isChecked)
            {
                // Get data from sensors
                if (main.tick == null)
                {
                    main.tick = new Timer();
                }
                if (main.timerTask == null)
                {
                    main.timerTask = new scan();
                }
                main.collect = true;
                // Put here time 1,000 milliseconds = 1 second
                main.tick.schedule(main.timerTask, 0, 1000 * sampling_rate);
            }
            else
            {
                main.timerTask.cancel();
                main.tick.cancel();
                main.timerTask = null;
                main.tick = null;
                session_id = null;
                main.collect = false;
            }
        }

        public void run()
        {
            try
            {

                //Looper.prepare();
                if(wifi.startScan())
                {
                    main.runOnUiThread(() -> Toast.makeText(main, "Manually updated Wi-Fi!", Toast.LENGTH_SHORT).show());
                }
                else
                {
                    main.runOnUiThread(() -> Toast.makeText(main, "FAILED TO Manually update Wi-Fi!", Toast.LENGTH_SHORT).show());
                }

                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
                if (session_id == null)
                {
                    session_id = timeStamp;
                }
                f = new FloorData(isIndoor, timeStamp, session_id, android_model, main.room,
                        floor_options[main.floor_idx - 1], main.building,
                        wifi.getConnectedMAC(), wifi.getConnectedRSSI(), isCenter,
                        gps.altitude,
                        gps.latitude, gps.longitude, gps.vAccuracy, gps.hAccuracy, gps.course, gps.speed,
                        barometer.pressure_at_sea_level, barometer.pressure, barometer.barometricAltitude,
                        main.env_context,
                        env_building_mean_floor_options[main.mean_floor_idx - 1], motion.getActivity(),
                        gps.city_name, gps.country_name, magneto.magnetX, magneto.magnetY, magneto.magnetZ,
                        wifi.wifi_results);
                current_floor_data.post(() -> current_floor_data.setText(f.toString()));

                // I/O, time out 1 second.
                Socket clientSocket = new Socket();
                clientSocket.connect(new InetSocketAddress(SQLDatabase, portNumber), 1000);

                // Send Data
                ObjectOutputStream toServer = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream fromServer = new ObjectInputStream(clientSocket.getInputStream());
                toServer.writeObject(f);
                toServer.flush();

                if (!fromServer.readBoolean())
                {
                    send_failed.show();
                }
                // send_successful.show();
            }
            catch(SocketTimeoutException ioe)
            {
                timeout.show();
                ioe.printStackTrace();
            }
            catch(IOException ioe)
            {
                io_exception.show();
                ioe.printStackTrace();
            }
        }
    }
}