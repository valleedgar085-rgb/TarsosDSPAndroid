package be.hogent.tarsos.dsp.io;

import be.hogent.tarsos.dsp.AudioFormat;

/**
 * TarsosDSPAudioFormat wraps the TarsosDSP AudioFormat for easier construction.
 * 
 * @author Joren Six
 */
public class TarsosDSPAudioFormat extends AudioFormat {
    
    /**
     * Construct a new TarsosDSPAudioFormat
     * 
     * @param sampleRate The sample rate in Hz
     * @param sampleSizeInBits The sample size in bits (8 or 16)
     * @param channels The number of channels (1 for mono, 2 for stereo)
     * @param signed Whether the samples are signed
     * @param bigEndian Whether the byte order is big-endian
     */
    public TarsosDSPAudioFormat(float sampleRate, int sampleSizeInBits, 
                                 int channels, boolean signed, boolean bigEndian) {
        super(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
