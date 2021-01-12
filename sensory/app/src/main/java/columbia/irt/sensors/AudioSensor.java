package columbia.irt.sensors;

// Inspired by:
// https://github.com/jeff2900/Sound-Meter/blob/master/app/src/main/java/com/bodekjan/soundmeter/MyMediaRecorder.java
import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class AudioSensor implements Runnable
{
    private final static String TAG = "MY_SENSOR";
    private MediaRecorder mRecorder = null;

    private File myRecAudioFile;
    private Thread thread;
    private boolean isThreadRun = true;
    private boolean isRecording = false;
    private boolean background_Listener = true;
    private boolean refreshed = false;

    // from: https://github.com/jeff2900/Sound-Meter/blob/master/app/src/main/java/com/bodekjan/soundmeter/World.java
    public float dbCount = 40;
    private float minDB = 100;
    private float maxDB = 0;
    private float lastDbCount = dbCount;

    private void setDbCount(float dbValue)
    {
        float value;
        float min = 0.5f;  //Set the minimum sound change
        if (dbValue > lastDbCount)
        {
            value = Math.max(dbValue - lastDbCount, min);
        }
        else
        {
            value = Math.min(dbValue - lastDbCount, -min);
        }
        dbCount = lastDbCount + value * 0.2f ; //To prevent the sound from changing too fast
        lastDbCount = dbCount;
        if(dbCount < minDB)
        {
            minDB=dbCount;
        }
        if(dbCount > maxDB)
        {
            maxDB=dbCount;
        }

        //Log.d(TAG, "audio dB: " + value);
        //Log.d(TAG, "min dB: " + minDB);
        //Log.d(TAG, "dbCount dB: " + dbCount);
        //Log.d(TAG, "max dB: " + maxDB);
    }

    private float getMaxAmplitude()
    {
        if (mRecorder != null)
        {
            try
            {
                return mRecorder.getMaxAmplitude();
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
                return 0;
            }
        }
        else
        {
            return 5;
        }
    }

    private void startListenAudio()
    {
        (thread = new Thread(this)).start();
    }

    private void setMyRecAudioFile(File myRecAudioFile)
    {
        this.myRecAudioFile = myRecAudioFile;
    }

    public void startRecord(Context c, File fFile)
    {
        try
        {
            setMyRecAudioFile(fFile);
            if (startRecorder())
            {
                Log.d(TAG, "Begin Recording!");
                startListenAudio();
            }
            else
            {
                Toast.makeText(c, "Error Starting Recorder!", Toast.LENGTH_SHORT).show();
            }
        }
        catch(Exception e)
        {
            Toast.makeText(c, "Recorder is currently Busy!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private boolean startRecorder()
    {
        if(myRecAudioFile == null)
        {
            return false;
        }

        if (mRecorder == null)
        {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(myRecAudioFile.getAbsolutePath());
            try
            {
                mRecorder.prepare();
                mRecorder.start();
                isRecording = true;
                background_Listener = true;
                Log.d(TAG, "Recorder started!");
                return true;
            }
            catch (java.io.IOException ioe)
            {
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
                isRecording = false;
                Log.e(TAG, "IOException: " + Log.getStackTraceString(ioe));
            }
            catch (SecurityException e)
            {
                stopRecording();
                isRecording = false;
                Log.e(TAG, "SecurityException: " + Log.getStackTraceString(e));
            }
        }
        return false;
    }

    private void stopRecording()
    {
        if (mRecorder != null)
        {
            try
            {
                if(isRecording)
                {
                    mRecorder.stop();
                }
            }
            catch (RuntimeException e)
            {
                Log.d(TAG, "Yikes!");
                e.printStackTrace();
            }
            finally
            {
                mRecorder.release();
                mRecorder = null;
                isRecording = false;
            }
        }
    }

    public void resume()
    {
        background_Listener = true;
        if(startRecorder())
        {
            Log.d(TAG, "Audio Resume Successful!");
        }
        else
        {
            Log.d(TAG, "Audio Resume Failed!");
        }
    }

    public void pause()
    {
        background_Listener = false;
        // Same as Delete
        stopRecording();
        if (myRecAudioFile != null)
        {
            if(myRecAudioFile.delete())
            {
                myRecAudioFile = null;
            }
            myRecAudioFile = null;
        }
        // thread = null;
        Log.d(TAG, "Recorder paused!");
    }

    public void delete()
    {
        if(thread != null)
        {
            isThreadRun = false;
            thread = null;
        }
        // Same as delete from original
        stopRecording();
        if (myRecAudioFile != null)
        {
            if(myRecAudioFile.delete())
            {
                myRecAudioFile = null;
            }
            myRecAudioFile = null;
        }
        Log.d(TAG, "Recorder destroyed!");
    }

    public void run()
    {
        while (isThreadRun)
        {
            try
            {
                if(background_Listener)
                {
                    float volume = getMaxAmplitude();  //Get the sound pressure value
                    if(volume > 0 && volume < 1000000)
                    {
                        setDbCount(20 * (float)(Math.log10(volume)));  //Change the sound pressure value to the decibel value
                    }
                    if(refreshed)
                    {
                        Thread.sleep(1200);
                        refreshed = false;
                    }
                    else
                    {
                        Thread.sleep(200);
                    }
                }
            }
            catch (InterruptedException e)
            {
                //e.printStackTrace();
                //background_Listener = false;
                break;
            }
        }
    }

    public void refresh()
    {
        refreshed = true;
        minDB = 100;
        dbCount = 0;
        lastDbCount = 0;
        maxDB = 0;
    }
}
