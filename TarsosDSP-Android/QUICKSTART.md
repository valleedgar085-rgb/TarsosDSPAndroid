# TarsosDSP-Android Quick Start Guide

Get started with TarsosDSP-Android in minutes!

## Prerequisites

- Android Studio Arctic Fox (2020.3.1) or newer
- Android SDK API 21+
- Java 8 or higher

## Setup

### 1. Import the Project

```bash
git clone <repository-url>
cd TarsosDSP-Android
```

Open in Android Studio: File → Open → Select `TarsosDSP-Android` folder

### 2. Sync Gradle

Android Studio will automatically sync Gradle. If not, click "Sync Now" in the notification bar.

### 3. Run the Example App

1. Connect an Android device or start an emulator
2. Select `tarsosdsp-android-example` from the run configuration dropdown
3. Click the Run button (green play icon)
4. Grant microphone permission when prompted
5. Tap "Start Listening" to begin pitch detection

## Using TarsosDSP-Android in Your App

### Step 1: Add Dependency

In your app's `build.gradle`:

```gradle
dependencies {
    implementation project(':tarsosdsp-android')
}
```

In your `settings.gradle`:

```gradle
include ':app', ':tarsosdsp-android'
```

### Step 2: Add Permissions

In `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

### Step 3: Request Runtime Permission

```java
private static final int AUDIO_PERMISSION_REQUEST = 1;

private void checkAudioPermission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                AUDIO_PERMISSION_REQUEST);
    } else {
        startAudioProcessing();
    }
}

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                      @NonNull int[] grantResults) {
    if (requestCode == AUDIO_PERMISSION_REQUEST) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startAudioProcessing();
        }
    }
}
```

### Step 4: Basic Pitch Detection

```java
import be.hogent.tarsos.dsp.AudioDispatcher;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;
import be.hogent.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

private AudioDispatcher dispatcher;
private Thread audioThread;

private void startAudioProcessing() {
    // Create dispatcher from microphone
    dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
    
    // Create pitch detection handler
    PitchDetectionHandler pdh = new PitchDetectionHandler() {
        @Override
        public void handlePitch(PitchDetectionResult result, AudioEvent e) {
            final float pitchInHz = result.getPitch();
            
            runOnUiThread(() -> {
                if (result.isPitched()) {
                    Log.d("Pitch", "Detected: " + pitchInHz + " Hz");
                    // Update your UI here
                }
            });
        }
    };
    
    // Add pitch processor
    AudioProcessor pitchProcessor = new PitchProcessor(
        PitchEstimationAlgorithm.FFT_YIN, 
        22050, 
        1024, 
        pdh
    );
    dispatcher.addAudioProcessor(pitchProcessor);
    
    // Start processing in background
    audioThread = new Thread(dispatcher, "Audio Thread");
    audioThread.start();
}

private void stopAudioProcessing() {
    if (dispatcher != null) {
        dispatcher.stop();
    }
}

@Override
protected void onDestroy() {
    super.onDestroy();
    stopAudioProcessing();
}
```

## Common Examples

### 1. Sound Level Meter

```java
import be.hogent.tarsos.dsp.SilenceDetector;

SilenceDetector silenceDetector = new SilenceDetector();
dispatcher.addAudioProcessor(silenceDetector);

dispatcher.addAudioProcessor(new AudioProcessor() {
    @Override
    public boolean process(AudioEvent audioEvent) {
        double dB = silenceDetector.currentSPL();
        runOnUiThread(() -> {
            soundLevelTextView.setText(String.format("%.1f dB", dB));
        });
        return true;
    }
    
    @Override
    public void processingFinished() {}
});
```

### 2. Musical Note Detection

```java
private String frequencyToNote(float frequency) {
    final String[] NOTES = {"C", "C#", "D", "D#", "E", "F", 
                            "F#", "G", "G#", "A", "A#", "B"};
    
    double noteNumber = 12 * (Math.log(frequency / 440.0) / Math.log(2)) + 69;
    int midiNote = (int) Math.round(noteNumber);
    int noteIndex = midiNote % 12;
    int octave = (midiNote / 12) - 1;
    
    return NOTES[noteIndex] + octave;
}
```

### 3. Clap Detection (Onset Detection)

```java
import be.hogent.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.hogent.tarsos.dsp.onsets.OnsetHandler;

ComplexOnsetDetector onsetDetector = new ComplexOnsetDetector(1024, 
    new OnsetHandler() {
        @Override
        public void handleOnset(double time, double salience) {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Clap detected!", 
                    Toast.LENGTH_SHORT).show();
            });
        }
    });

dispatcher.addAudioProcessor(onsetDetector);
```

### 4. Audio Playback

```java
import be.hogent.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.hogent.tarsos.dsp.AudioFormat;

AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
AndroidAudioPlayer player = new AndroidAudioPlayer(format);

dispatcher.addAudioProcessor(player);
// Audio will now be played through speakers
```

## Tips & Best Practices

### 1. Choose the Right Sample Rate

- **22050 Hz**: Good for pitch detection, lower CPU usage
- **44100 Hz**: CD quality, better for music analysis
- **48000 Hz**: Professional audio quality

### 2. Buffer Size Selection

- **512**: Low latency, real-time response, higher CPU
- **1024**: Balanced (recommended)
- **2048**: More accurate, higher latency
- **4096**: Very accurate, not suitable for real-time

### 3. Algorithm Selection

| Algorithm | Speed | Accuracy | Best For |
|-----------|-------|----------|----------|
| FFT_YIN | Fast | Good | Real-time apps |
| YIN | Medium | Very Good | Music analysis |
| MPM | Slow | Excellent | High quality required |
| DYNAMIC_WAVELET | Fast | Good | Speech processing |

### 4. Thread Management

```java
// Always process audio in background thread
audioThread = new Thread(dispatcher, "Audio Dispatcher");
audioThread.start();

// Stop properly
@Override
protected void onDestroy() {
    super.onDestroy();
    if (dispatcher != null) {
        dispatcher.stop();
    }
    if (audioThread != null) {
        try {
            audioThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

### 5. UI Updates

```java
// Always update UI on main thread
runOnUiThread(() -> {
    textView.setText("Pitch: " + pitch);
});

// Or use Handler
Handler mainHandler = new Handler(Looper.getMainLooper());
mainHandler.post(() -> {
    textView.setText("Pitch: " + pitch);
});
```

## Troubleshooting

### No Audio Input

1. Check microphone permission is granted
2. Test on physical device (emulator microphone may not work well)
3. Check device microphone is working

### Inaccurate Pitch Detection

1. Use larger buffer size (2048 or 4096)
2. Try different algorithm (YIN or MPM)
3. Filter out low-probability results
4. Use proper sample rate for your use case

### High CPU Usage

1. Use smaller buffer size
2. Use FFT_YIN algorithm
3. Lower sample rate (22050 Hz)
4. Don't update UI too frequently

### App Crashes

1. Always request RECORD_AUDIO permission
2. Handle permission denial gracefully
3. Stop dispatcher in onDestroy()
4. Catch exceptions in audio thread

## Build & Deploy

### Debug Build

```bash
./gradlew assembleDebug
```

### Release Build

```bash
./gradlew assembleRelease
```

### Install on Device

```bash
./gradlew installDebug
```

## Next Steps

- Explore the example app source code
- Read the full [README.md](README.md) for detailed documentation
- Check out additional audio processors (filters, effects, etc.)
- Implement your own AudioProcessor for custom processing

## Resources

- [TarsosDSP Original Documentation](http://tarsos.0110.be/tag/TarsosDSP)
- [Android Audio Developer Guide](https://developer.android.com/guide/topics/media/audio-capture)
- Example app source: `tarsosdsp-android-example/src/main/java/`

## Support

For issues or questions:
- Check the FAQ in README.md
- Review example app code
- File an issue on GitHub

---

Happy coding! 🎵🎶
