package columbia.irt.network;

import android.widget.CompoundButton;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

import columbia.irt.sensors.BarometricAltimeter;
import columbia.irt.sensors.GPSAltimeter;
import columbia.irt.sensors.MagneticFieldSensor;
import columbia.irt.struct.floorData;

public class sendData
{
    // IP data
    public final static String SQLDatabase = "160.39.151.251";
    public final static int portNumber = 9000;

    // I/O
    private Socket ClientSocket = null;
    private ObjectOutputStream toServer = null;

    // Timer stuff
    private Timer tick = new Timer();
    private TimerTask timerTask;

    private class collect extends TimerTask implements CompoundButton.OnCheckedChangeListener
    {
        public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked)
        {
            // 1 scan per second
            int sampling_rate = 1;

            if (isChecked)
            {
                // Get data from sensors
                if (tick == null)
                {
                    tick = new Timer();
                }
                if (timerTask == null)
                {
                    timerTask = new collect();
                }
                // Put here time 1,000 milliseconds = 1 second
                tick.schedule(timerTask, 0, 1000 * sampling_rate);
            }
        }

        public void run()
        {
            try
            {
                ClientSocket = new Socket();
                ClientSocket.connect(new InetSocketAddress(SQLDatabase, portNumber), 10 * 1000);

                floorData f;

                // Send Data
                toServer = new ObjectOutputStream(ClientSocket.getOutputStream());
                f = new floorData(null, null, null);
                toServer.writeObject(f);
                //toServer.writeObject(null);

                if(toServer != null)
                {
                    toServer.close();
                }
                if(ClientSocket != null && ClientSocket.isConnected())
                {
                    ClientSocket.close();
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
