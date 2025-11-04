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

import android.media.AudioTrack;
import android.media.AudioManager;

/**
 * This AudioProcessor can be used to sync events with sound on Android. 
 * It uses Android's AudioTrack to play audio data. The write method on 
 * AudioTrack blocks until it is ready for more data, which helps keep 
 * everything synchronized.
 * 
 * If this AudioProcessor is chained with other AudioProcessors, the others 
 * should be able to operate in real time or process the signal on a separate thread.
 * 
 * @author Joren Six
 */
public final class AndroidAudioPlayer implements AudioProcessor {

    /**
     * The AudioTrack instance used to play audio on Android.
     */
    private AudioTrack audioTrack;

    private final AudioFormat format;

    /**
     * Creates a new Android audio player.
     * 
     * @param format The AudioFormat of the buffer.
     * @param bufferSizeInBytes The buffer size in bytes.
     */
    public AndroidAudioPlayer(final AudioFormat format, int bufferSizeInBytes) {
        this.format = format;

        int channelConfig = format.getChannels() == 1 ? 
            android.media.AudioFormat.CHANNEL_OUT_MONO : 
            android.media.AudioFormat.CHANNEL_OUT_STEREO;

        int encoding = android.media.AudioFormat.ENCODING_PCM_16BIT;

        // Create the AudioTrack
        audioTrack = new AudioTrack(
            AudioManager.STREAM_MUSIC,
            (int) format.getSampleRate(),
            channelConfig,
            encoding,
            bufferSizeInBytes,
            AudioTrack.MODE_STREAM
        );

        audioTrack.play();
    }

    /**
     * Creates a new Android audio player with a default buffer size.
     * 
     * @param format The AudioFormat of the buffer.
     */
    public AndroidAudioPlayer(final AudioFormat format) {
        this(format, AudioTrack.getMinBufferSize(
            (int) format.getSampleRate(),
            format.getChannels() == 1 ? 
                android.media.AudioFormat.CHANNEL_OUT_MONO : 
                android.media.AudioFormat.CHANNEL_OUT_STEREO,
            android.media.AudioFormat.ENCODING_PCM_16BIT
        ));
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        // overlap in samples * nr of bytes / sample = bytes overlap
        int byteOverlap = audioEvent.getOverlap() * format.getFrameSize();
        int byteStepSize = audioEvent.getBufferSize() * format.getFrameSize() - byteOverlap;

        // Play only the audio that has not been played already.
        audioTrack.write(audioEvent.getByteBuffer(), byteOverlap, byteStepSize);
        return true;
    }

    @Override
    public void processingFinished() {
        // cleanup
        if (audioTrack != null) {
            audioTrack.flush();
            audioTrack.release();
            audioTrack = null;
        }
    }
}
