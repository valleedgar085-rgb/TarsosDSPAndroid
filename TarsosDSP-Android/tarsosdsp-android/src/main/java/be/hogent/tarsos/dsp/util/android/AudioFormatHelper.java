package be.hogent.tarsos.dsp.util.android;

import android.media.AudioFormat;
import android.media.AudioRecord;

/**
 * Helper class for working with audio formats on Android.
 * 
 * @author Joren Six
 */
public class AudioFormatHelper {
    
    /**
     * Common sample rates for Android audio
     */
    public static final int[] COMMON_SAMPLE_RATES = {
        8000, 11025, 16000, 22050, 44100, 48000
    };
    
    /**
     * Get the optimal sample rate for the device
     * 
     * @param preferredRate The preferred sample rate
     * @return The optimal sample rate, or preferredRate if it's supported
     */
    public static int getOptimalSampleRate(int preferredRate) {
        // Try the preferred rate first
        if (isSampleRateSupported(preferredRate)) {
            return preferredRate;
        }
        
        // Try common rates
        for (int rate : COMMON_SAMPLE_RATES) {
            if (isSampleRateSupported(rate)) {
                return rate;
            }
        }
        
        // Default to 44100 if nothing else works
        return 44100;
    }
    
    /**
     * Check if a sample rate is supported
     * 
     * @param sampleRate The sample rate to check
     * @return true if supported, false otherwise
     */
    public static boolean isSampleRateSupported(int sampleRate) {
        int bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        );
        
        return bufferSize != AudioRecord.ERROR && 
               bufferSize != AudioRecord.ERROR_BAD_VALUE;
    }
    
    /**
     * Get the minimum buffer size for the given parameters
     * 
     * @param sampleRate The sample rate in Hz
     * @param channelConfig The channel configuration
     * @param audioFormat The audio format
     * @return The minimum buffer size in bytes, or -1 if error
     */
    public static int getMinBufferSize(int sampleRate, int channelConfig, int audioFormat) {
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            return -1;
        }
        
        return bufferSize;
    }
    
    /**
     * Convert Android AudioFormat channel config to number of channels
     * 
     * @param channelConfig The Android channel configuration
     * @return The number of channels
     */
    public static int channelConfigToChannelCount(int channelConfig) {
        switch (channelConfig) {
            case AudioFormat.CHANNEL_IN_MONO:
            case AudioFormat.CHANNEL_OUT_MONO:
                return 1;
            case AudioFormat.CHANNEL_IN_STEREO:
            case AudioFormat.CHANNEL_OUT_STEREO:
                return 2;
            default:
                return 1;
        }
    }
    
    /**
     * Convert number of channels to Android channel config for recording
     * 
     * @param channels The number of channels
     * @return The Android channel configuration
     */
    public static int channelCountToInputConfig(int channels) {
        return channels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
    }
    
    /**
     * Convert number of channels to Android channel config for playback
     * 
     * @param channels The number of channels
     * @return The Android channel configuration
     */
    public static int channelCountToOutputConfig(int channels) {
        return channels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
    }
    
    /**
     * Convert Android audio encoding to bits per sample
     * 
     * @param encoding The Android audio encoding
     * @return The bits per sample
     */
    public static int encodingToBitsPerSample(int encoding) {
        switch (encoding) {
            case AudioFormat.ENCODING_PCM_8BIT:
                return 8;
            case AudioFormat.ENCODING_PCM_16BIT:
                return 16;
            case AudioFormat.ENCODING_PCM_FLOAT:
                return 32;
            default:
                return 16;
        }
    }
    
    /**
     * Convert bits per sample to Android audio encoding
     * 
     * @param bits The bits per sample
     * @return The Android audio encoding
     */
    public static int bitsPerSampleToEncoding(int bits) {
        switch (bits) {
            case 8:
                return AudioFormat.ENCODING_PCM_8BIT;
            case 16:
                return AudioFormat.ENCODING_PCM_16BIT;
            case 32:
                return AudioFormat.ENCODING_PCM_FLOAT;
            default:
                return AudioFormat.ENCODING_PCM_16BIT;
        }
    }
    
    /**
     * Create a TarsosDSP AudioFormat from Android parameters
     * 
     * @param sampleRate The sample rate in Hz
     * @param channelConfig The Android channel configuration
     * @param encoding The Android audio encoding
     * @return A TarsosDSP AudioFormat
     */
    public static be.hogent.tarsos.dsp.AudioFormat createTarsosDSPFormat(
            int sampleRate, int channelConfig, int encoding) {
        
        int channels = channelConfigToChannelCount(channelConfig);
        int bits = encodingToBitsPerSample(encoding);
        
        return new be.hogent.tarsos.dsp.AudioFormat(
            sampleRate,
            bits,
            channels,
            true,  // signed
            false  // little endian
        );
    }
    
    /**
     * Calculate the duration in seconds for a given number of bytes
     * 
     * @param bytes The number of bytes
     * @param sampleRate The sample rate in Hz
     * @param channels The number of channels
     * @param bitsPerSample The bits per sample
     * @return The duration in seconds
     */
    public static double bytesToSeconds(long bytes, int sampleRate, int channels, int bitsPerSample) {
        int bytesPerSample = bitsPerSample / 8;
        long totalSamples = bytes / (bytesPerSample * channels);
        return (double) totalSamples / sampleRate;
    }
    
    /**
     * Calculate the number of bytes for a given duration
     * 
     * @param seconds The duration in seconds
     * @param sampleRate The sample rate in Hz
     * @param channels The number of channels
     * @param bitsPerSample The bits per sample
     * @return The number of bytes
     */
    public static long secondsToBytes(double seconds, int sampleRate, int channels, int bitsPerSample) {
        int bytesPerSample = bitsPerSample / 8;
        long totalSamples = (long) (seconds * sampleRate);
        return totalSamples * bytesPerSample * channels;
    }
}
