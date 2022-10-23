package com.example.waltal5;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;

public class ConversationBackgr {


    private Context context;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;

    private int intBufferSize;
    private short[] shortAudioData;

    private int intGain;
    private boolean isActive = true;

    private Thread thread;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ConversationBackgr(Context context) {
        this.context = context;
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);
        int intRecordSampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);

        intBufferSize = AudioRecord.getMinBufferSize(intRecordSampleRate, AudioFormat.CHANNEL_IN_MONO
                , AudioFormat.ENCODING_PCM_16BIT);
        //intBufferSize = 1024;
        shortAudioData = new short[intBufferSize];

        /*if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/
        Log.d("perm ok","perm ok");
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC
                , intRecordSampleRate
                , AudioFormat.CHANNEL_IN_STEREO
                , AudioFormat.ENCODING_PCM_16BIT
                , intBufferSize);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC
                , intRecordSampleRate
                , AudioFormat.CHANNEL_IN_STEREO
                , AudioFormat.ENCODING_PCM_16BIT
                , intBufferSize
                , AudioTrack.MODE_STREAM);
        Log.d("audiorecord", String.valueOf(audioRecord.getState()));
        audioTrack.setPlaybackRate(intRecordSampleRate);

        audioRecord.startRecording();

        audioTrack.play();
        audioTrack.setVolume(0.3f);
        Log.d("audio ok", "audio objects created");
    }

    public void runConversation(){
        thread = new Thread(new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
                    threadLoop();
                    }
                });
        thread.start();
    }

    /*public void buttonStart(View view){
        isActive = true;
        intGain = Integer.parseInt(editTextGainFactor.getText().toString());
        textViewStatus.setText("Active");
        thread.start();
    }*/

    /*public void buttonStop(View view){

        isActive = false;
        audioTrack.stop();
        audioRecord.stop();

        textViewStatus.setText("Stopped");
    }*/

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void threadLoop() {



        while (isActive){
        audioRecord.read(shortAudioData, 0, shortAudioData.length);

            /*for (int i = 0; i< shortAudioData.length; i++){
                shortAudioData[i] = (short) Math.min (shortAudioData[i] * intGain, Short.MAX_VALUE);
            }*/
        //Log.d("buff_1", "threadLoop: "+ Arrays.toString(shortAudioData));
        audioTrack.write(shortAudioData, 0, shortAudioData.length);
        //Log.d("buff_2", "threadLoop: "+ Arrays.toString(shortAudioData));
        audioTrack.setVolume(0.3f);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void playSound(byte[] inBuffer, int bytes)
    {
        //Log.d("buff_1", "threadLoop: "+ Arrays.toString(shortAudioData));
        audioTrack.write(inBuffer,0,bytes);


    }
     public void readSound(byte[] outBuffer)
    {
        audioRecord.read(outBuffer, 0, intBufferSize);
        //Log.d("buff_1", "threadLoop: "+ Arrays.toString(shortAudioData));
    }

    public void readSoundShort(short[] outBufferShort)
    {
         int result = audioRecord.read(outBufferShort,0, intBufferSize);

    }

    public void release(){
        audioRecord.release();

        audioTrack.release();

    }
    public int getIntBufferSize(){
        return intBufferSize;
    }
}
