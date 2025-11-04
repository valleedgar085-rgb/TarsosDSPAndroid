package be.hogent.tarsos.dsp.io.android;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.IOException;
import java.io.InputStream;

/**
 * AndroidAudioInputStream reads audio from the Android microphone using AudioRecord.
 * It presents the audio data as an InputStream that can be used with TarsosDSP.
 * 
 * @author Joren Six
 */
public class AndroidAudioInputStream extends InputStream {
    
    private final AudioRecord audioRecord;
    private final int bufferSize;
    private final byte[] buffer;
    private int bufferReadPosition = 0;
    private int bufferWritePosition = 0;
    private boolean isRecording = false;
    
    /**
     * Constructs a new AndroidAudioInputStream.
     * 
     * @param sampleRate The sample rate in Hz
     * @param audioSource The audio source (e.g., MediaRecorder.AudioSource.MIC)
     * @param channelConfig The channel configuration
     * @param audioEncoding The audio encoding
     */
    public AndroidAudioInputStream(int sampleRate, int audioSource, int channelConfig, int audioEncoding) {
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding);
        
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            throw new IllegalArgumentException("Invalid audio parameters");
        }
        
        buffer = new byte[bufferSize * 2];
        
        try {
            audioRecord = new AudioRecord(
                audioSource,
                sampleRate,
                channelConfig,
                audioEncoding,
                bufferSize * 2
            );
            
            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                throw new IllegalStateException("AudioRecord failed to initialize");
            }
            
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to create AudioRecord", e);
        }
    }
    
    /**
     * Constructs a new AndroidAudioInputStream with default parameters.
     * Sample rate: 44100 Hz, Mono, 16-bit PCM
     */
    public AndroidAudioInputStream() {
        this(44100, MediaRecorder.AudioSource.MIC, 
             AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    }
    
    /**
     * Start recording audio
     */
    public void startRecording() {
        if (!isRecording) {
            audioRecord.startRecording();
            isRecording = true;
        }
    }
    
    /**
     * Stop recording audio
     */
    public void stopRecording() {
        if (isRecording) {
            audioRecord.stop();
            isRecording = false;
        }
    }
    
    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        int result = read(b, 0, 1);
        return result == -1 ? -1 : (b[0] & 0xff);
    }
    
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (!isRecording) {
            startRecording();
        }
        
        int bytesRead = audioRecord.read(b, off, len);
        
        if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION || 
            bytesRead == AudioRecord.ERROR_BAD_VALUE) {
            throw new IOException("Error reading from AudioRecord: " + bytesRead);
        }
        
        return bytesRead;
    }
    
    @Override
    public void close() throws IOException {
        stopRecording();
        if (audioRecord != null) {
            audioRecord.release();
        }
        super.close();
    }
    
    /**
     * Get the sample rate
     * @return the sample rate in Hz
     */
    public int getSampleRate() {
        return audioRecord.getSampleRate();
    }
    
    /**
     * Get the audio format
     * @return the audio format
     */
    public int getAudioFormat() {
        return audioRecord.getAudioFormat();
    }
    
    /**
     * Get the channel configuration
     * @return the channel configuration
     */
    public int getChannelConfiguration() {
        return audioRecord.getChannelConfiguration();
    }
}
