/*
*      _______                       _____   _____ _____  
*     |__   __|                     |  __ \ / ____|  __ \ 
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
*                                                         
* -----------------------------------------------------------
*
*  TarsosDSP is developed by Joren Six at 
*  The School of Arts,
*  University College Ghent,
*  Hoogpoort 64, 9000 Ghent - Belgium
*  
* -----------------------------------------------------------
*
*  Info: http://tarsos.0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://tarsos.0110.be/releases/TarsosDSP/
*  
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
* 
*/

package be.hogent.tarsos.dsp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import be.hogent.tarsos.dsp.util.AudioFloatConverter;

/**
 * This class plays an audio file and sends float arrays to registered AudioProcessor
 * implementations. This is the Android-compatible version that works with Android's 
 * file system and doesn't rely on javax.sound.sampled classes.
 * 
 * This dispatcher can be used to feed FFT's, pitch detectors, audio players, etc.
 * Using a (blocking) audio player, it is possible to synchronize execution of 
 * AudioProcessors and sound. This behavior can be used for visualization.
 * 
 * @author Joren Six
 */
public final class AndroidAudioDispatcher implements Runnable {

    /**
     * The audio stream (in bytes), conversion to float happens at the last moment.
     */
    private final InputStream audioInputStream;

    /**
     * This buffer is reused again and again to store audio data using the float data type.
     */
    private float[] audioFloatBuffer;

    /**
     * This buffer is reused again and again to store audio data using the byte data type.
     */
    private byte[] audioByteBuffer;

    /**
     * A list of registered audio processors. The audio processors are
     * responsible for actually doing the digital signal processing
     */
    private final List<AudioProcessor> audioProcessors;

    /**
     * Converter converts an array of floats to an array of bytes (and vice versa).
     */
    private final AudioFloatConverter converter;

    private final AudioFormat format;

    /**
     * The floatOverlap: the number of elements that are copied in the buffer
     * from the previous buffer. Overlap should be smaller (strict) than the
     * buffer size and can be zero. Defined in number of samples.
     */
    private int floatOverlap, floatStepSize;

    /**
     * The overlap and stepsize defined not in samples but in bytes. So it
     * depends on the bit depth. Since the int datatype is used only 8,16,24,...
     * bits or 1,2,3,... bytes are supported.
     */
    private int byteOverlap, byteStepSize;

    /**
     * Position in the stream in bytes. e.g. if 44100 bytes are processed and 16
     * bits per frame are used then you are 0.5 seconds into the stream.
     */
    private long bytesProcessed;

    /**
     * The length of the stream, expressed in sample frames rather than bytes
     */
    private long frameLength;

    private final AudioEvent audioEvent;

    /**
     * If true the dispatcher stops dispatching audio.
     */
    private boolean stopped;

    /**
     * If zero pad is true then the first buffer is only filled up to buffer
     * size - hop size. E.g. if the buffer is 2048 and the hop size is 48 then
     * you get 2000x0 and 48 filled audio samples
     */
    private boolean zeroPad;

    /**
     * Create a new AndroidAudioDispatcher connected to a file.
     * 
     * @param audioFile The audio file to read from.
     * @param audioBufferSize The size of the buffer defines how much samples
     *            are processed in one step. Common values are 1024, 2048.
     * @param bufferOverlap How much consecutive buffers overlap (in samples).
     *            Half of the AudioBufferSize is common (512, 1024) for an FFT.
     * @throws IOException When the file can't be read.
     */
    public AndroidAudioDispatcher(File audioFile, int audioBufferSize, int bufferOverlap) 
            throws IOException {
        this(new FileInputStream(audioFile), audioFile.length(), audioBufferSize, bufferOverlap);
    }

    /**
     * Create a new AndroidAudioDispatcher connected to an InputStream.
     * 
     * @param stream The input stream to read from.
     * @param streamLength The length of the stream in bytes (for progress calculation).
     * @param audioBufferSize The size of the buffer defines how much samples
     *            are processed in one step. Common values are 1024, 2048.
     * @param bufferOverlap How much consecutive buffers overlap (in samples).
     *            Half of the AudioBufferSize is common (512, 1024) for an FFT.
     */
    public AndroidAudioDispatcher(InputStream stream, long streamLength, 
            int audioBufferSize, int bufferOverlap) {
        audioProcessors = new ArrayList<AudioProcessor>();
        audioInputStream = stream;

        // Assume 16-bit PCM, 44100 Hz, mono (can be customized as needed)
        format = new AudioFormat(44100, 16, 1, true, false);
        
        // Calculate frame length
        this.frameLength = streamLength / format.getFrameSize();

        audioEvent = new AudioEvent(format, frameLength);

        setStepSizeAndOverlap(audioBufferSize, bufferOverlap);
        converter = AudioFloatConverter.getConverter(format);
        stopped = false;
        bytesProcessed = 0;
        zeroPad = true;
    }

    /**
     * Create a new AndroidAudioDispatcher with custom format.
     * 
     * @param stream The input stream to read from.
     * @param streamLength The length of the stream in bytes.
     * @param audioBufferSize The size of the buffer.
     * @param bufferOverlap The buffer overlap.
     * @param format The audio format.
     */
    public AndroidAudioDispatcher(InputStream stream, long streamLength, 
            int audioBufferSize, int bufferOverlap, AudioFormat format) {
        audioProcessors = new ArrayList<AudioProcessor>();
        audioInputStream = stream;
        this.format = format;
        this.frameLength = streamLength / format.getFrameSize();
        audioEvent = new AudioEvent(format, frameLength);
        setStepSizeAndOverlap(audioBufferSize, bufferOverlap);
        converter = AudioFloatConverter.getConverter(format);
        stopped = false;
        bytesProcessed = 0;
        zeroPad = true;
    }

    /**
     * Set a new step size and overlap size. Both in number of samples.
     * 
     * @param audioBufferSize The size of the buffer defines how much samples
     *            are processed in one step. Common values are 1024, 2048.
     * @param bufferOverlap How much consecutive buffers overlap (in samples).
     *            Half of the AudioBufferSize is common (512, 1024) for an FFT.
     */
    public void setStepSizeAndOverlap(final int audioBufferSize, final int bufferOverlap) {
        audioFloatBuffer = new float[audioBufferSize];
        floatOverlap = bufferOverlap;
        floatStepSize = audioFloatBuffer.length - floatOverlap;

        audioByteBuffer = new byte[audioFloatBuffer.length * format.getFrameSize()];
        byteOverlap = floatOverlap * format.getFrameSize();
        byteStepSize = floatStepSize * format.getFrameSize();
    }

    /**
     * if zero pad is true then the first buffer is only filled up to buffer
     * size - hop size E.g. if the buffer is 2048 and the hop size is 48 then
     * you get 2000x0 and 48 filled audio samples
     * 
     * @param zeroPad true if the buffer should be zeropadded, false otherwise.
     */
    public void setZeroPad(boolean zeroPad) {
        this.zeroPad = zeroPad;
    }

    /**
     * Adds an AudioProcessor to the chain of processors.
     * 
     * @param audioProcessor The AudioProcessor to add.
     */
    public void addAudioProcessor(final AudioProcessor audioProcessor) {
        audioProcessors.add(audioProcessor);
    }

    /**
     * Removes an AudioProcessor from the chain of processors and calls
     * processingFinished.
     * 
     * @param audioProcessor The AudioProcessor to remove.
     */
    public void removeAudioProcessor(final AudioProcessor audioProcessor) {
        audioProcessors.remove(audioProcessor);
        audioProcessor.processingFinished();
    }

    @Override
    public void run() {
        try {
            int bytesRead = 0;

            if (zeroPad) {
                bytesRead = slideBuffer();
            } else {
                bytesRead = processFirstBuffer();
            }

            // As long as the stream has not ended or the number of bytes
            // processed is smaller than the number of bytes to process: process bytes.
            audioLoop: while (bytesRead != -1 && !stopped) {
                // Makes sure the right buffers are processed, they can be
                // changed by audio processors.
                audioEvent.setOverlap(floatOverlap);
                audioEvent.setFloatBuffer(audioFloatBuffer);
                audioEvent.setBytesProcessed(bytesProcessed);

                for (final AudioProcessor processor : audioProcessors) {
                    if (!processor.process(audioEvent)) {
                        break audioLoop;
                    }
                }

                // Update the number of bytes processed;
                bytesProcessed += bytesRead;

                // Read, convert and process consecutive overlapping buffers.
                // Slide the buffer.
                bytesRead = slideBuffer();
            }

            // Notify all processors that no more data is available.
            if (!stopped) {
                stop();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int processFirstBuffer() throws IOException {
        // the overlap for the first buffer is zero.
        audioEvent.setOverlap(0);
        audioEvent.setFloatBuffer(audioFloatBuffer);
        audioEvent.setBytesProcessed(bytesProcessed);

        // Read, convert and process the first full buffer.
        int bytesRead = 0;
        int currentBytesRead = 0;
        while (bytesRead != -1 && currentBytesRead < audioByteBuffer.length) {
            bytesRead = audioInputStream.read(audioByteBuffer, currentBytesRead,
                    audioByteBuffer.length - currentBytesRead);
            if (bytesRead > 0) {
                currentBytesRead += bytesRead;
            }
        }
        bytesRead = currentBytesRead;

        if (bytesRead > 0 && !stopped) {
            converter.toFloatArray(audioByteBuffer, audioFloatBuffer);

            for (final AudioProcessor processor : audioProcessors) {
                if (!processor.process(audioEvent)) {
                    break;
                }
            }
            // Update the number of bytes processed;
            bytesProcessed += bytesRead;

            // Read, convert and process consecutive overlapping buffers.
            bytesRead = slideBuffer();
        }
        return bytesRead;
    }

    /**
     * Stops dispatching audio data.
     */
    public void stop() {
        stopped = true;
        for (final AudioProcessor processor : audioProcessors) {
            processor.processingFinished();
        }
        try {
            audioInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Slides a buffer with an overlap and reads new data from the stream.
     */
    private int slideBuffer() throws IOException {
        assert floatOverlap < audioFloatBuffer.length;

        // Copy the end of the buffer to the beginning
        System.arraycopy(audioFloatBuffer, floatStepSize, audioFloatBuffer, 0, floatOverlap);

        int bytesRead = 0;

        if (stopped) {
            bytesRead = -1;
        } else {
            int currentBytesRead = 0;
            while (bytesRead != -1 && currentBytesRead < byteStepSize) {
                bytesRead = audioInputStream.read(audioByteBuffer, byteOverlap + currentBytesRead,
                        byteStepSize - currentBytesRead);
                if (bytesRead > 0) {
                    currentBytesRead += bytesRead;
                }
            }
            bytesRead = currentBytesRead;
            
            if (bytesRead > 0) {
                converter.toFloatArray(audioByteBuffer, byteOverlap, audioFloatBuffer, 
                        floatOverlap, floatStepSize);
            }
        }
        return bytesRead;
    }

    public AudioFormat getFormat() {
        return format;
    }

    /**
     * @return The currently processed number of seconds.
     */
    public float secondsProcessed() {
        return bytesProcessed / (format.getSampleSizeInBits() / 8) / format.getSampleRate()
                / format.getChannels();
    }

    /**
     * Returns the duration of the stream in seconds.
     * 
     * @return The duration of the stream in seconds.
     */
    public double durationInSeconds() {
        return frameLength / format.getSampleRate();
    }

    /**
     * Returns the length of the stream, expressed in sample frames.
     * 
     * @return The length of the stream in frames.
     */
    public long durationInFrames() {
        return frameLength;
    }
}
