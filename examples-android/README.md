# TarsosDSP Android Examples

This module contains complete, working example applications demonstrating TarsosDSP on Android.

## Examples Included

### 1. Pitch Detection (PitchDetectionActivity)
Real-time pitch detection from the device microphone.

**Features:**
- Displays detected pitch in Hz
- Shows probability of detection
- Uses FFT-YIN algorithm
- Start/Stop controls

**Use Cases:**
- Musical tuner apps
- Vocal training applications
- Music transcription
- Instrument tuning

### 2. Sound Detector (SoundDetectorActivity)
Monitors sound level and detects sound vs silence.

**Features:**
- Displays sound level in dB SPL
- Adjustable threshold with slider
- Real-time status (Sound/Silence)
- Visual feedback

**Use Cases:**
- Voice activity detection
- Noise monitoring
- Automatic recording triggers
- Sound level meter

### 3. Audio Player (AudioPlayerActivity)
Plays audio files with real-time effects.

**Features:**
- Plays 16-bit PCM WAV files
- Adjustable gain control
- Playback progress display
- Start/Stop controls

**Use Cases:**
- Custom audio players
- Audio effects apps
- Educational audio tools
- Music practice apps

## Building and Running

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK with API 21+ (Android 5.0+)
- Physical device or emulator with microphone (for pitch/sound detection)

### Build Steps

1. **Open in Android Studio:**
   ```bash
   # From project root
   # Uncomment the include line in settings.gradle
   # Then open the project in Android Studio
   ```

2. **Sync Gradle:**
   - Click "Sync Project with Gradle Files"
   - Wait for dependencies to download

3. **Connect Device:**
   - Enable USB debugging on your Android device
   - Connect via USB or use emulator

4. **Run:**
   - Select `examples-android` configuration
   - Click Run (▶️) button
   - Grant microphone permission when prompted

### Command Line Build

```bash
# Build the APK
./gradlew examples-android:assembleDebug

# Install on connected device
./gradlew examples-android:installDebug

# Build and run
./gradlew examples-android:installDebug
adb shell am start -n be.hogent.tarsos.dsp.example/.MainActivity
```

## Project Structure

```
examples-android/
├── build.gradle                 # Module build configuration
├── proguard-rules.pro          # ProGuard rules for release builds
└── src/main/
    ├── AndroidManifest.xml     # App manifest with permissions
    ├── java/be/hogent/tarsos/dsp/example/
    │   ├── MainActivity.java              # Launcher activity
    │   ├── PitchDetectionActivity.java    # Pitch detection example
    │   ├── SoundDetectorActivity.java     # Sound detection example
    │   └── AudioPlayerActivity.java       # Audio playback example
    └── res/
        └── values/
            ├── strings.xml     # App strings
            └── themes.xml      # App theme
```

## Permissions

The examples require these permissions (already in AndroidManifest.xml):

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

Runtime permissions are requested automatically when needed.

## Testing Audio Playback Example

To test the AudioPlayerActivity:

1. Create a 16-bit PCM WAV file (mono or stereo, 44100 Hz recommended)
2. Name it `test.wav`
3. Place it in the device's Downloads folder
4. Launch the Audio Player example
5. Tap "Play Audio File"

**Generate a test file using FFmpeg:**
```bash
ffmpeg -i input.mp3 -acodec pcm_s16le -ar 44100 -ac 1 test.wav
```

## Customizing the Examples

### Change Pitch Detection Algorithm

In `PitchDetectionActivity.java`:
```java
// Replace FFT_YIN with:
PitchEstimationAlgorithm.YIN          // More accurate, slower
PitchEstimationAlgorithm.MPM          // Good for pure tones
PitchEstimationAlgorithm.AMDF         // Faster, less accurate
PitchEstimationAlgorithm.DYNAMIC_WAVELET  // Alternative approach
```

### Adjust Audio Buffer Size

```java
// Smaller buffer = lower latency, less accurate
final int bufferSize = 512;

// Larger buffer = higher latency, more accurate
final int bufferSize = 4096;

// Recommended for pitch detection
final int bufferSize = 1024;
```

### Change Sample Rate

```java
// Lower sample rate = less CPU usage, lower frequency range
final int sampleRate = 16000;

// Higher sample rate = more CPU usage, wider frequency range
final int sampleRate = 44100;

// Recommended balance
final int sampleRate = 22050;
```

## Common Modifications

### Add a Low-Pass Filter

```java
import be.hogent.tarsos.dsp.filters.LowPassFS;

// In your dispatcher setup:
LowPassFS lowPass = new LowPassFS(1000, sampleRate); // 1kHz cutoff
dispatcher.addAudioProcessor(lowPass);
```

### Add Percussion Detection

```java
import be.hogent.tarsos.dsp.onsets.PercussionOnsetDetector;
import be.hogent.tarsos.dsp.onsets.OnsetHandler;

PercussionOnsetDetector detector = new PercussionOnsetDetector(
    sampleRate, bufferSize, 
    new OnsetHandler() {
        @Override
        public void handleOnset(double time, double salience) {
            runOnUiThread(() -> {
                // Handle percussion onset
                Log.d("Onset", "Detected at: " + time + "s");
            });
        }
    },
    40, 3  // Sensitivity, threshold
);
dispatcher.addAudioProcessor(detector);
```

### Display Waveform

```java
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;

AudioProcessor waveformProcessor = new AudioProcessor() {
    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] buffer = audioEvent.getFloatBuffer();
        
        runOnUiThread(() -> {
            // Update your custom WaveformView
            waveformView.updateWaveform(buffer);
        });
        
        return true;
    }

    @Override
    public void processingFinished() {}
};
dispatcher.addAudioProcessor(waveformProcessor);
```

## Performance Tips

1. **Use appropriate buffer sizes:**
   - Too small: High CPU usage, inaccurate results
   - Too large: High latency, memory usage
   - Sweet spot: 1024-2048 for most applications

2. **Limit UI updates:**
   ```java
   private long lastUpdate = 0;
   private static final long UPDATE_INTERVAL_MS = 100; // 10 FPS
   
   if (System.currentTimeMillis() - lastUpdate > UPDATE_INTERVAL_MS) {
       runOnUiThread(() -> updateUI());
       lastUpdate = System.currentTimeMillis();
   }
   ```

3. **Use release builds for testing performance:**
   ```bash
   ./gradlew examples-android:assembleRelease
   ```

4. **Profile with Android Profiler:**
   - Open Android Profiler in Android Studio
   - Monitor CPU, memory, and energy usage
   - Identify bottlenecks

## Troubleshooting

### "No pitch detected" (always returns -1)
- Increase buffer size to 2048
- Try different algorithm (e.g., YIN instead of FFT_YIN)
- Check microphone input level
- Ensure clear audio signal (sing/play sustained note)

### Crackling or distorted audio playback
- Increase buffer size for AudioPlayer
- Check that audio file format matches AudioFormat
- Reduce number of concurrent processors
- Use higher thread priority

### High battery drain
- Reduce sample rate (22050 instead of 44100)
- Increase buffer size
- Limit UI update frequency
- Stop processing when app is in background

### Permission errors
- Check AndroidManifest.xml has required permissions
- Verify runtime permission request code
- Test on Android 6.0+ device
- Check logcat for permission denials

## Further Development

### Create Your Own Activity

1. Copy an existing activity as template
2. Modify the UI layout
3. Customize audio processing chain
4. Add to AndroidManifest.xml
5. Add launcher button to MainActivity

### Add New Audio Processor

```java
public class MyCustomProcessor implements AudioProcessor {
    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] buffer = audioEvent.getFloatBuffer();
        
        // Your custom processing here
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] *= 0.5f; // Example: reduce volume
        }
        
        return true;
    }

    @Override
    public void processingFinished() {
        // Cleanup
    }
}
```

## Resources

- **Main Documentation**: `../README-ANDROID.md`
- **Quick Start**: `../ANDROID-QUICKSTART.md`
- **TarsosDSP Website**: http://0110.be/tag/TarsosDSP
- **API Documentation**: http://tarsos.0110.be/releases/TarsosDSP/

## Support

Questions or issues with the examples?

- Open an issue on GitHub
- Check existing issues for solutions
- Email: joren.six@ugent.be

## License

GPL - See `../license.txt` for details
