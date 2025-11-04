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

/**
 * A helper class to locate FFMPEG on Android devices.
 * On Android, FFMPEG is typically not available by default, but can be 
 * included as a native library through projects like FFmpeg-Android or
 * through the native library approach.
 * 
 * @author Joren Six
 */
public class AndroidFFMPEGLocator {

    /**
     * The path to the FFMPEG binary on Android.
     */
    private static String ffmpegPath = null;

    /**
     * Set the path to the FFMPEG binary on Android.
     * 
     * @param path The path to the FFMPEG binary.
     */
    public static void setFFMPEGPath(String path) {
        ffmpegPath = path;
    }

    /**
     * Get the path to the FFMPEG binary.
     * 
     * @return The path to FFMPEG, or null if not set.
     */
    public static String ffmpegBinary() {
        return ffmpegPath;
    }

    /**
     * Check if FFMPEG is available on this Android device.
     * 
     * @return true if FFMPEG is available, false otherwise.
     */
    public static boolean isFFMPEGAvailable() {
        if (ffmpegPath == null) {
            return false;
        }
        File ffmpeg = new File(ffmpegPath);
        return ffmpeg.exists() && ffmpeg.canExecute();
    }
}
