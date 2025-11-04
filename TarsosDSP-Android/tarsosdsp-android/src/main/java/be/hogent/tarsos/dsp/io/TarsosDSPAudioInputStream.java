package be.hogent.tarsos.dsp.io;

import java.io.IOException;
import java.io.InputStream;

import be.hogent.tarsos.dsp.AudioFormat;

/**
 * TarsosDSPAudioInputStream wraps an InputStream and provides audio format information.
 * 
 * @author Joren Six
 */
public class TarsosDSPAudioInputStream extends InputStream {
    
    private final InputStream inputStream;
    private final AudioFormat audioFormat;
    private long frameLength;
    
    /**
     * Construct a new TarsosDSPAudioInputStream
     * 
     * @param inputStream The underlying input stream
     * @param audioFormat The audio format
     */
    public TarsosDSPAudioInputStream(InputStream inputStream, AudioFormat audioFormat) {
        this.inputStream = inputStream;
        this.audioFormat = audioFormat;
        this.frameLength = -1; // Unknown length
    }
    
    /**
     * Get the audio format
     * @return the audio format
     */
    public AudioFormat getFormat() {
        return audioFormat;
    }
    
    /**
     * Get the frame length
     * @return the frame length, or -1 if unknown
     */
    public long getFrameLength() {
        return frameLength;
    }
    
    @Override
    public int read() throws IOException {
        return inputStream.read();
    }
    
    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }
    
    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }
    
    @Override
    public int available() throws IOException {
        return inputStream.available();
    }
    
    @Override
    public void close() throws IOException {
        inputStream.close();
    }
    
    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }
    
    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }
    
    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }
}
