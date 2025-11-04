package be.hogent.tarsos.dsp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import be.hogent.tarsos.dsp.io.TarsosDSPAudioInputStream;

/**
 * AudioDispatcher reads audio data from an input stream and dispatches it to
 * registered AudioProcessor instances.
 * 
 * @author Joren Six
 */
public class AudioDispatcher implements Runnable {
    
    private static final Logger LOG = Logger.getLogger(AudioDispatcher.class.getName());
    
    private final TarsosDSPAudioInputStream audioInputStream;
    private final AudioFormat audioFormat;
    private final int audioBufferSize;
    private final int bufferOverlap;
    private final List<AudioProcessor> audioProcessors;
    
    private float[] audioFloatBuffer;
    private byte[] audioByteBuffer;
    
    private long bytesProcessed;
    private boolean stopped;
    
    /**
     * Construct a new AudioDispatcher
     * 
     * @param stream The audio input stream
     * @param audioBufferSize The buffer size in samples
     * @param bufferOverlap The overlap in samples
     */
    public AudioDispatcher(TarsosDSPAudioInputStream stream, int audioBufferSize, int bufferOverlap) {
        this.audioInputStream = stream;
        this.audioFormat = stream.getFormat();
        this.audioBufferSize = audioBufferSize;
        this.bufferOverlap = bufferOverlap;
        this.audioProcessors = new ArrayList<>();
        
        this.audioFloatBuffer = new float[audioBufferSize];
        int bytesPerSample = audioFormat.getSampleSizeInBits() / 8;
        this.audioByteBuffer = new byte[audioBufferSize * bytesPerSample * audioFormat.getChannels()];
        
        this.bytesProcessed = 0;
        this.stopped = false;
    }
    
    /**
     * Add an AudioProcessor
     * @param audioProcessor The processor to add
     */
    public void addAudioProcessor(AudioProcessor audioProcessor) {
        audioProcessors.add(audioProcessor);
    }
    
    /**
     * Remove an AudioProcessor
     * @param audioProcessor The processor to remove
     */
    public void removeAudioProcessor(AudioProcessor audioProcessor) {
        audioProcessors.remove(audioProcessor);
    }
    
    /**
     * Run the audio dispatcher
     */
    @Override
    public void run() {
        int bytesPerSample = audioFormat.getSampleSizeInBits() / 8;
        int bytesToRead = audioBufferSize * bytesPerSample * audioFormat.getChannels();
        int bytesToOverlap = bufferOverlap * bytesPerSample * audioFormat.getChannels();
        
        try {
            while (!stopped) {
                int bytesRead = audioInputStream.read(audioByteBuffer, 0, bytesToRead);
                
                if (bytesRead == -1 || bytesRead == 0) {
                    // End of stream
                    break;
                }
                
                // Convert bytes to floats
                convertBytesToFloats();
                
                // Create audio event
                AudioEvent audioEvent = new AudioEvent(audioFormat, bytesProcessed);
                audioEvent.setFloatBuffer(audioFloatBuffer);
                audioEvent.setOverlap(bufferOverlap);
                
                // Process audio
                for (AudioProcessor processor : audioProcessors) {
                    if (!processor.process(audioEvent)) {
                        // Processor requested stop
                        stopped = true;
                        break;
                    }
                }
                
                bytesProcessed += bytesRead;
                
                // Handle overlap
                if (bufferOverlap > 0 && bytesRead == bytesToRead) {
                    System.arraycopy(audioByteBuffer, bytesToRead - bytesToOverlap, 
                                   audioByteBuffer, 0, bytesToOverlap);
                }
            }
            
            // Notify processors that processing is finished
            for (AudioProcessor processor : audioProcessors) {
                processor.processingFinished();
            }
            
        } catch (IOException e) {
            LOG.severe("Error reading audio: " + e.getMessage());
        } finally {
            try {
                audioInputStream.close();
            } catch (IOException e) {
                LOG.severe("Error closing audio stream: " + e.getMessage());
            }
        }
    }
    
    /**
     * Convert byte buffer to float buffer
     */
    private void convertBytesToFloats() {
        int bytesPerSample = audioFormat.getSampleSizeInBits() / 8;
        boolean isBigEndian = audioFormat.isBigEndian();
        
        for (int i = 0; i < audioFloatBuffer.length; i++) {
            int offset = i * bytesPerSample * audioFormat.getChannels();
            
            if (bytesPerSample == 2) {
                // 16-bit samples
                int sample;
                if (isBigEndian) {
                    sample = ((audioByteBuffer[offset] << 8) | (audioByteBuffer[offset + 1] & 0xFF));
                } else {
                    sample = ((audioByteBuffer[offset + 1] << 8) | (audioByteBuffer[offset] & 0xFF));
                }
                audioFloatBuffer[i] = sample / 32768.0f;
            } else {
                // 8-bit samples
                int sample = audioByteBuffer[offset] & 0xFF;
                audioFloatBuffer[i] = (sample - 128) / 128.0f;
            }
        }
    }
    
    /**
     * Stop the audio dispatcher
     */
    public void stop() {
        stopped = true;
    }
    
    /**
     * Check if the dispatcher is stopped
     * @return true if stopped, false otherwise
     */
    public boolean isStopped() {
        return stopped;
    }
    
    /**
     * Get the audio format
     * @return the audio format
     */
    public AudioFormat getFormat() {
        return audioFormat;
    }
    
    /**
     * Get seconds processed
     * @return seconds of audio processed
     */
    public float secondsProcessed() {
        return bytesProcessed / (audioFormat.getSampleRate() * 
                                audioFormat.getSampleSizeInBits() / 8 * 
                                audioFormat.getChannels());
    }
}
