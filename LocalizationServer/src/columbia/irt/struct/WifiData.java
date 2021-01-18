package columbia.irt.struct;


import java.io.Serializable;
import java.util.List;

import android.net.wifi.ScanResult;
import android.os.Build;
import static android.net.wifi.ScanResult.CHANNEL_WIDTH_160MHZ;
import static android.net.wifi.ScanResult.CHANNEL_WIDTH_20MHZ;
import static android.net.wifi.ScanResult.CHANNEL_WIDTH_40MHZ;
import static android.net.wifi.ScanResult.CHANNEL_WIDTH_80MHZ;
import static android.net.wifi.ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ;

public class WifiData implements Serializable
{
	private static final long serialVersionUID = 6901070322763386242L;
	
	public String  [] WifiAPs;
    public String  [] SSID;
    public String  [] capabilities;
    public Integer [] centerFreq0;
    public Integer [] centerFreq1;
    public String  [] channelWidth;
    public Integer [] frequency;
    public Integer [] WifiRSS;
    public String  [] operatorFriendlyName;
    public Long    [] timestamp;
    public String  [] vanueName;
    public Integer [] isPassPoint;
    public Integer [] is80211mc;

    public WifiData(List<ScanResult> results)
    {
        int size = results.size();
        WifiAPs = new String[size];
        SSID = new String[size];
        capabilities = new String[size];
        centerFreq0 = new Integer[size];
        centerFreq1 = new Integer[size];
        channelWidth = new String[size];
        frequency = new Integer[size];
        WifiRSS = new Integer[size];
        operatorFriendlyName = new String[size];
        timestamp = new Long[size];
        vanueName = new String[size];
        isPassPoint = new Integer[size];
        is80211mc = new Integer[size];

        for (int i = 0; i < results.size(); i++)
        {
            WifiAPs[i] = results.get(i).BSSID;
            SSID[i] = results.get(i).SSID;
            capabilities[i] = results.get(i).capabilities;
            frequency[i] = results.get(i).frequency;
            WifiRSS[i] = results.get(i).level;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                // Log.d(TAG, "Last seen in seconds: " + (results.get(i).timestamp/1000000));
                timestamp[i] = (results.get(i).timestamp/1000000);
            }
            else
            {
                timestamp[i] = (long) -1;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                centerFreq0[i] = results.get(i).centerFreq0;
                centerFreq1[i] = results.get(i).centerFreq1;
                channelWidth[i] = getChannel(results.get(i).channelWidth);
                operatorFriendlyName[i] = results.get(i).operatorFriendlyName.toString();
                vanueName[i] = results.get(i).venueName.toString();

                if(results.get(i).isPasspointNetwork())
                {
                    isPassPoint[i] = 1;
                }
                else
                {
                    isPassPoint[i] = 0;
                }
                if(results.get(i).is80211mcResponder())
                {
                    is80211mc[i] = 1;
                }
                else
                {
                    is80211mc[i] = 0;
                }
            }
            else
            {
                centerFreq0[i] = -1;
                centerFreq1[i] = -1;
                operatorFriendlyName[i] = "NOT_SUPPORTED";
                vanueName[i] = "NOT_SUPPORTED";
                isPassPoint[i] = -1;
                is80211mc[i] = -1;
            }
        }
    }

    // Pulled Jar from 
    // https://androidsdkmanager.azurewebsites.net/SDKPlatform
    private String getChannel(int channel)
    {
        String answer;
        switch(channel)
        {
            case CHANNEL_WIDTH_20MHZ:
                answer = "CHANNEL_WIDTH_20MHZ";
                break;

            case CHANNEL_WIDTH_40MHZ:
                answer = "CHANNEL_WIDTH_40MHZ";
                break;

            case CHANNEL_WIDTH_80MHZ:
                answer = "CHANNEL_WIDTH_80MHZ";
                break;

            case CHANNEL_WIDTH_160MHZ:
                answer = "CHANNEL_WIDTH_160MHZ";
                break;

            case CHANNEL_WIDTH_80MHZ_PLUS_MHZ:
                answer= "CHANNEL_WIDTH_80MHZ_PLUS_MHZ";
                break;

            default:
                answer = "NOT_SUPPORTED";
                break;
        }
        return answer;
    }
}
