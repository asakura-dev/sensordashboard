package com.github.pocmo.sensordashboard;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.media.MediaRecorder;
import android.os.Environment;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import android.util.Log;
import android.media.AudioRecord;
import android.media.AudioFormat;


/**
 * Created by asakura on 15/12/14.
 */
public class AudioService extends Service {
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private volatile boolean isRecording;
    private String filename = "";
    private DeviceClient client;

    @Override
    public void onCreate() {
        super.onCreate();
        client = DeviceClient.getInstance(this);
        bufferSize =
                AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                        RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        startRecord();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecord();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void stopRecord(){
        Log.d("Audio", "Stop Record");
        recorder.stop();
        isRecording = false;
        recorder.release();
    }
    private void startRecord(){
        Log.d("Audio", "Start Record");
        //SimpleDateFormat sdf = new SimpleDateFormat("M_d_hh_mm_ss_SSS");
        //filename = "/"+sdf.format(System.currentTimeMillis())+"_audio.3gp";
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING,     bufferSize);
        if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            recorder.startRecording();
            isRecording = true;
            Log.v("Audio", "Successfully started recording");

            recordingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    processRawAudioData();
                }
            }, "AudioRecorder Thread");

            recordingThread.start();
        } else {
            Log.v("Audio", "Failed to started recording");
        }
    }
    private void processRawAudioData() {
        byte data[] = new byte[bufferSize];
        int read = 0;
        while(isRecording) {
            read = recorder.read(data, 0, bufferSize);
            if(AudioRecord.ERROR_INVALID_OPERATION != read) {
                Log.v("Audio", "Successfully read " + data.length + " bytes of audio");
            }
        }
    }
}
