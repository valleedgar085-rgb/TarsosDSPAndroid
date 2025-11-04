package be.hogent.tarsos.dsp.io.android;

import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaRecorder;

import java.io.IOException;

import be.hogent.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.hogent.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.hogent.tarsos.dsp.io.UniversalAudioInputStream;

/**
 * Factory for creating AudioDispatcher instances on Android.
 * This class provides convenient methods for creating audio dispatchers
 * from various sources on Android.
 * 
 * @author Joren Six
 */
public class AudioDispatcherFactory {
    
    /**
     * Create an AudioDispatcher from the default microphone with default settings
     * (44100 Hz, 16-bit, mono, 2048 buffer size, 0 overlap)
     * 
     * @return AudioDispatcher configured for microphone input
     */
    public static be.hogent.tarsos.dsp.AudioDispatcher fromDefaultMicrophone() {
        return fromDefaultMicrophone(44100, 2048, 0);
    }
    
    /**
     * Create an AudioDispatcher from the default microphone
     * 
     * @param sampleRate The sample rate in Hz
     * @param bufferSize The buffer size in samples
     * @param overlap The overlap in samples
     * @return AudioDispatcher configured for microphone input
     */
    public static be.hogent.tarsos.dsp.AudioDispatcher fromDefaultMicrophone(
            int sampleRate, int bufferSize, int overlap) {
        
        AndroidAudioInputStream audioInputStream = new AndroidAudioInputStream(
            sampleRate,
            MediaRecorder.AudioSource.MIC,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        );
        
        TarsosDSPAudioFormat audioFormat = new TarsosDSPAudioFormat(
            sampleRate,
            16,
            1,
            true,
            false
        );
        
        TarsosDSPAudioInputStream tarsosDSPInputStream = 
            new TarsosDSPAudioInputStream(audioInputStream, audioFormat);
        
        return new be.hogent.tarsos.dsp.AudioDispatcher(tarsosDSPInputStream, bufferSize, overlap);
    }
    
    /**
     * Create an AudioDispatcher from the microphone with custom settings
     * 
     * @param sampleRate The sample rate in Hz
     * @param audioSource The audio source (e.g., MediaRecorder.AudioSource.MIC)
     * @param channelConfig The channel configuration
     * @param audioEncoding The audio encoding
     * @param bufferSize The buffer size in samples
     * @param overlap The overlap in samples
     * @return AudioDispatcher configured for microphone input
     */
    public static be.hogent.tarsos.dsp.AudioDispatcher fromMicrophone(
            int sampleRate,
            int audioSource,
            int channelConfig,
            int audioEncoding,
            int bufferSize,
            int overlap) {
        
        AndroidAudioInputStream audioInputStream = new AndroidAudioInputStream(
            sampleRate,
            audioSource,
            channelConfig,
            audioEncoding
        );
        
        int channels = (channelConfig == AudioFormat.CHANNEL_IN_MONO) ? 1 : 2;
        int bits = (audioEncoding == AudioFormat.ENCODING_PCM_16BIT) ? 16 : 8;
        
        TarsosDSPAudioFormat audioFormat = new TarsosDSPAudioFormat(
            sampleRate,
            bits,
            channels,
            true,
            false
        );
        
        TarsosDSPAudioInputStream tarsosDSPInputStream = 
            new TarsosDSPAudioInputStream(audioInputStream, audioFormat);
        
        return new be.hogent.tarsos.dsp.AudioDispatcher(tarsosDSPInputStream, bufferSize, overlap);
    }
    
    /**
     * Create an AudioDispatcher from an Android asset file
     * 
     * @param context The Android context
     * @param assetPath The path to the asset file
     * @param bufferSize The buffer size in samples
     * @param overlap The overlap in samples
     * @return AudioDispatcher configured for the asset file
     * @throws IOException if the file cannot be read
     */
    public static be.hogent.tarsos.dsp.AudioDispatcher fromAsset(
            Context context,
            String assetPath,
            int bufferSize,
            int overlap) throws IOException {
        
        java.io.InputStream inputStream = context.getAssets().open(assetPath);
        
        TarsosDSPAudioFormat audioFormat = new TarsosDSPAudioFormat(
            44100,
            16,
            1,
            true,
            false
        );
        
        TarsosDSPAudioInputStream tarsosDSPInputStream = 
            new TarsosDSPAudioInputStream(inputStream, audioFormat);
        
        return new be.hogent.tarsos.dsp.AudioDispatcher(tarsosDSPInputStream, bufferSize, overlap);
    }
}
