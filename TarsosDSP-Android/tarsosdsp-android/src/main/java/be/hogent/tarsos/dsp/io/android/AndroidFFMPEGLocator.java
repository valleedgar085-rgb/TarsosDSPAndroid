package be.hogent.tarsos.dsp.io.android;

/**
 * AndroidFFMPEGLocator provides the location of ffmpeg on Android.
 * Since Android doesn't typically have ffmpeg in the system path,
 * this class would need to be configured to point to a bundled ffmpeg binary.
 * 
 * @author Joren Six
 */
public class AndroidFFMPEGLocator {
    
    private static String ffmpegPath = null;
    
    /**
     * Set the path to the ffmpeg binary
     * @param path The path to ffmpeg
     */
    public static void setFFMPEGPath(String path) {
        ffmpegPath = path;
    }
    
    /**
     * Get the path to the ffmpeg binary
     * @return The path to ffmpeg, or null if not set
     */
    public static String ffmpegBinary() {
        return ffmpegPath;
    }
    
    /**
     * Check if ffmpeg is available
     * @return true if ffmpeg path is set, false otherwise
     */
    public static boolean isAvailable() {
        return ffmpegPath != null && !ffmpegPath.isEmpty();
    }
}
