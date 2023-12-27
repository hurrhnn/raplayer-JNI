package xyz.hurrhnn.raplayer_jni;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import androidx.annotation.RequiresPermission;

import java.nio.ByteBuffer;

public class Raplayer {
    static {
        System.loadLibrary("raplayer");
        System.loadLibrary("raplayer_jni");
    }
    public long raplayer_ctx;

    Raplayer() {
        this.raplayer_ctx = initRaplayerFromJNI();
    }

    public void bufferReadCallback(byte[] frame, AudioTrack audioTrack) {
        audioTrack.write(frame, 0, frame.length);
    }

    public ByteBuffer bufferWriteCallback(AudioRecord audioRecord) {
        byte[] buffer = new byte[960 * 2 * 2];
        audioRecord.read(buffer, 0, 960 * 2 * 2);
        return ByteBuffer.allocateDirect(960 * 2 * 2).put(buffer, 0, 960 * 2 * 2);
    }

    public long spawn(boolean mode, String address, short port) {
        return spawnRaplayerFromJNI(this.raplayer_ctx, mode, address, port);
    }

    @RequiresPermission(value = "android.permission.RECORD_AUDIO")
    public long registerMediaProvider(long spawn_id) {
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 48000, AudioFormat.CHANNEL_IN_STEREO,AudioFormat.ENCODING_PCM_16BIT, 960 * 2 * 2);
        audioRecord.startRecording();

        return registerRaplayerMediaProviderFromJNI(this.raplayer_ctx, spawn_id, audioRecord);
    }

    public long registerMediaConsumer(long spawn_id) {
        AudioTrack audio = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, //sample rate
            AudioFormat.CHANNEL_OUT_STEREO, // 2 channel
            AudioFormat.ENCODING_PCM_16BIT, // 16-bit
            960 * 2 * 2, AudioTrack.MODE_STREAM);
        audio.play();

        return registerRaplayerMediaConsumerFromJNI(this.raplayer_ctx, spawn_id, audio);
    }

    public native String stringFromJNI();

    public native long initRaplayerFromJNI();

    public native long spawnRaplayerFromJNI(long raplayer_ctx, boolean mode, String address, short port);

    public native long registerRaplayerMediaProviderFromJNI(long raplayer_ctx, long spawn_id, AudioRecord audio);

    public native long registerRaplayerMediaConsumerFromJNI(long raplayer_ctx, long spawn_id, AudioTrack audio);
}
