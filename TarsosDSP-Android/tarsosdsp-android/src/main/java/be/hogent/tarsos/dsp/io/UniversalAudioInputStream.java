package be.hogent.tarsos.dsp.io;

import java.io.IOException;
import java.io.InputStream;

import be.hogent.tarsos.dsp.AudioFormat;

/**
 * UniversalAudioInputStream provides a universal interface for audio input streams.
 * 
 * @author Joren Six
 */
public class UniversalAudioInputStream extends TarsosDSPAudioInputStream {
    
    /**
     * Construct a new UniversalAudioInputStream
     * 
     * @param inputStream The underlying input stream
     * @param audioFormat The audio format
     */
    public UniversalAudioInputStream(InputStream inputStream, AudioFormat audioFormat) {
        super(inputStream, audioFormat);
    }
}
