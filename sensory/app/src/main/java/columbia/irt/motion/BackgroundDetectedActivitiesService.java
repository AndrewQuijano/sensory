package columbia.irt.motion;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.Task;

public class BackgroundDetectedActivitiesService extends Service
{
    private PendingIntent mPendingIntent;
    private ActivityRecognitionClient mActivityRecognitionClient;

    IBinder mBinder = new LocalBinder();

    public static class LocalBinder extends Binder
    {

    }

    public BackgroundDetectedActivitiesService()
    {

    }

    public void onCreate()
    {
        super.onCreate();
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        Intent mIntentService = new Intent(this, DetectedActivitiesIntentService.class);
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
        requestActivityUpdatesButtonHandler();
    }

    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public void requestActivityUpdatesButtonHandler()
    {
        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                mPendingIntent);

        task.addOnSuccessListener(result -> Toast.makeText(getApplicationContext(),
                "Successfully requested activity updates",
                Toast.LENGTH_SHORT)
                .show());

        task.addOnFailureListener(e -> Toast.makeText(getApplicationContext(),
                "Requesting activity updates failed to start",
                Toast.LENGTH_SHORT).show());
    }

    public void removeActivityUpdatesButtonHandler()
    {
        Task<Void> task = mActivityRecognitionClient.removeActivityUpdates(
                mPendingIntent);
        task.addOnSuccessListener(result -> Toast.makeText(getApplicationContext(),
                "Removed activity updates successfully!",
                Toast.LENGTH_SHORT)
                .show());

        task.addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to remove activity updates!",
                Toast.LENGTH_SHORT).show());
    }

    public void onDestroy()
    {
        super.onDestroy();
        removeActivityUpdatesButtonHandler();
    }
}