package be.hogent.tarsos.dsp.io.android;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;

/**
 * AndroidAudioPlayer writes audio to the Android audio system using AudioTrack.
 * 
 * @author Joren Six
 */
public class AndroidAudioPlayer implements AudioProcessor {
    
    private AudioTrack audioTrack;
    private final int sampleRate;
    private final int bufferSize;
    
    /**
     * Constructs a new AndroidAudioPlayer.
     * 
     * @param audioFormat The audio format to use
     */
    public AndroidAudioPlayer(be.hogent.tarsos.dsp.AudioFormat audioFormat) {
        this(audioFormat, AudioTrack.MODE_STREAM);
    }
    
    /**
     * Constructs a new AndroidAudioPlayer.
     * 
     * @param audioFormat The audio format to use
     * @param mode The AudioTrack mode (MODE_STREAM or MODE_STATIC)
     */
    public AndroidAudioPlayer(be.hogent.tarsos.dsp.AudioFormat audioFormat, int mode) {
        this.sampleRate = (int) audioFormat.getSampleRate();
        
        int channelConfig = audioFormat.getChannels() == 1 ? 
            AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        
        this.bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioEncoding);
        
        audioTrack = new AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            channelConfig,
            audioEncoding,
            bufferSize,
            mode
        );
        
        audioTrack.play();
    }
    
    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] audioFloatBuffer = audioEvent.getFloatBuffer();
        byte[] audioByteBuffer = new byte[audioFloatBuffer.length * 2];
        
        // Convert float samples to 16-bit PCM
        for (int i = 0; i < audioFloatBuffer.length; i++) {
            int sample = (int) (audioFloatBuffer[i] * 32767);
            // Clamp to 16-bit range
            if (sample > 32767) sample = 32767;
            if (sample < -32768) sample = -32768;
            
            // Little-endian format
            audioByteBuffer[i * 2] = (byte) (sample & 0xff);
            audioByteBuffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xff);
        }
        
        int written = audioTrack.write(audioByteBuffer, 0, audioByteBuffer.length);
        return written > 0;
    }
    
    @Override
    public void processingFinished() {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }
    
    /**
     * Pause audio playback
     */
    public void pause() {
        if (audioTrack != null) {
            audioTrack.pause();
        }
    }
    
    /**
     * Resume audio playback
     */
    public void play() {
        if (audioTrack != null) {
            audioTrack.play();
        }
    }
    
    /**
     * Check if the audio track is playing
     * @return true if playing, false otherwise
     */
    public boolean isPlaying() {
        return audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
    }
}
