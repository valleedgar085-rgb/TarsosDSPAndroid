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

import android.os.Handler;
import android.os.Looper;

/**
 * A utility class to help update the Android UI from audio processing threads.
 * Since audio processing typically happens on a background thread, this class
 * provides a convenient way to post updates to the main UI thread.
 * 
 * @author Joren Six
 */
public class AndroidUIUpdater {

    private final Handler handler;

    /**
     * Create a new AndroidUIUpdater that posts to the main looper.
     */
    public AndroidUIUpdater() {
        this.handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Create a new AndroidUIUpdater with a custom handler.
     * 
     * @param handler The handler to use for posting updates.
     */
    public AndroidUIUpdater(Handler handler) {
        this.handler = handler;
    }

    /**
     * Post a runnable to the UI thread.
     * 
     * @param runnable The runnable to execute on the UI thread.
     */
    public void post(Runnable runnable) {
        handler.post(runnable);
    }

    /**
     * Post a runnable to the UI thread with a delay.
     * 
     * @param runnable The runnable to execute on the UI thread.
     * @param delayMillis The delay in milliseconds.
     */
    public void postDelayed(Runnable runnable, long delayMillis) {
        handler.postDelayed(runnable, delayMillis);
    }

    /**
     * Remove any pending posts of a runnable.
     * 
     * @param runnable The runnable to remove.
     */
    public void removeCallbacks(Runnable runnable) {
        handler.removeCallbacks(runnable);
    }
}
