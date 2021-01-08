package columbia.irt.motion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.DetectedActivity;

import columbia.irt.sensory.R;

public class MotionReceiver extends BroadcastReceiver
{
    private final Context motion_activity;
    private boolean isRegistered = false;
    private String label;

    // Update
    private final TextView txtActivity;
    private final TextView txtConfidence;
    private final ImageView imgActivity;

    public MotionReceiver (Context context)
    {
        this(context, null, null, null);
    }

    public MotionReceiver (Context context, TextView txtActivity, TextView txtConfident, ImageView imgActivity)
    {
        this.motion_activity = context;
        this.txtActivity = txtActivity;
        this.txtConfidence = txtConfident;
        this.imgActivity = imgActivity;
        this.label = motion_activity.getString(R.string.activity_unknown);
        register(context);
    }

    public void register(Context context)
    {
        if(!isRegistered)
        {
            LocalBroadcastManager.getInstance(context).registerReceiver(this,
                    new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
            isRegistered = true;
            startTracking();
        }
    }

    public void unregister(Context context)
    {
        if(isRegistered)
        {
            stopTracking();
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
            isRegistered = false;
        }
    }

    public void onReceive(Context context, Intent intent)
    {
        if (Constants.BROADCAST_DETECTED_ACTIVITY.equals(intent.getAction()))
        {
            int type = intent.getIntExtra("type", -1);
            int confidence = intent.getIntExtra("confidence", 0);
            handleUserActivity(type, confidence);
        }
    }

    private void handleUserActivity(int type, int confidence)
    {
        int icon;

        switch (type)
        {
            case DetectedActivity.IN_VEHICLE:
                label = motion_activity.getString(R.string.activity_in_vehicle);
                icon = R.drawable.ic_driving;
                break;
            case DetectedActivity.ON_BICYCLE:
                label = motion_activity.getString(R.string.activity_on_bicycle);
                icon = R.drawable.ic_on_bicycle;
                break;
            case DetectedActivity.ON_FOOT:
                label = motion_activity.getString(R.string.activity_on_foot);
                icon = R.drawable.ic_walking;
                break;
            case DetectedActivity.RUNNING:
                label = motion_activity.getString(R.string.activity_running);
                icon = R.drawable.ic_running;
                break;
            case DetectedActivity.STILL:
                label = motion_activity.getString(R.string.activity_still);
                icon = R.drawable.ic_still;
                break;
            case DetectedActivity.TILTING:
                label = motion_activity.getString(R.string.activity_tilting);
                icon = R.drawable.ic_tilting;
                break;
            case DetectedActivity.WALKING:
                label = motion_activity.getString(R.string.activity_walking);
                icon = R.drawable.ic_walking;
                break;
            case DetectedActivity.UNKNOWN:
            default:
                label = motion_activity.getString(R.string.activity_unknown);
                icon = R.drawable.ic_unknown;
                break;
        }

        if (confidence > Constants.CONFIDENCE)
        {
            if(txtActivity != null)
            {
                txtActivity.setText(label);
                String answer = motion_activity.getString(R.string.Confidence) + confidence;
                txtConfidence.setText(answer);
                imgActivity.setImageResource(icon);
            }
        }
    }

    private void startTracking()
    {
        Intent intent1 = new Intent(motion_activity, BackgroundDetectedActivitiesService.class);
        motion_activity.startService(intent1);
    }

    private void stopTracking()
    {
        Intent intent = new Intent(motion_activity, BackgroundDetectedActivitiesService.class);
        motion_activity.stopService(intent);
    }

    public String getActivity()
    {
        return this.label;
    }
}
