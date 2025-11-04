# TarsosDSP for Android

Complete Android support for the TarsosDSP audio processing library.

## Overview

TarsosDSP is a Java library for audio processing. This Android implementation provides all the necessary components to use TarsosDSP in Android applications, including:

- **AndroidAudioPlayer**: Audio playback using Android's AudioTrack
- **AndroidAudioDispatcher**: File-based audio processing for Android
- **MicrophoneAudioDispatcher**: Real-time microphone audio processing
- **Complete example applications**: Pitch detection, sound detection, and audio playback

## Features

- ✅ Real-time audio processing from microphone
- ✅ Audio file processing and playback
- ✅ Pitch detection (YIN, MPM, FFT-YIN algorithms)
- ✅ Sound/silence detection
- ✅ Audio effects (gain, filters, etc.)
- ✅ FFT and spectral analysis
- ✅ MFCC extraction
- ✅ Onset detection
- ✅ Beat tracking
- ✅ Time stretching and pitch shifting

## Requirements

- Android SDK 21 (Android 5.0 Lollipop) or higher
- Android Studio (recommended)
- Gradle 7.0+

## Installation

### Option 1: Include as a Module

1. Copy the TarsosDSP source to your Android project
2. Add to your `settings.gradle`:
```gradle
include ':tarsos-dsp'
```

3. Add to your app's `build.gradle`:
```gradle
dependencies {
    implementation project(':tarsos-dsp')
}
```

### Option 2: Use as a JAR

1. Build the library:
```bash
./gradlew build
```

2. Copy `build/libs/tarsos-dsp-1.7.0.jar` to your Android project's `libs/` folder

3. Add to your app's `build.gradle`:
```gradle
dependencies {
    implementation files('libs/tarsos-dsp-1.7.0.jar')
}
```

## Permissions

Add these permissions to your `AndroidManifest.xml`:

```xml
<!-- For microphone access -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<!-- For audio file access -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

Remember to request runtime permissions on Android 6.0+ (API 23+):

```java
ActivityCompat.requestPermissions(this,
    new String[]{Manifest.permission.RECORD_AUDIO},
    PERMISSION_REQUEST_CODE);
```

## Quick Start Examples

### 1. Real-time Pitch Detection

```java
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;
import be.hogent.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

// Create dispatcher
int sampleRate = 22050;
int bufferSize = 1024;
int overlap = 0;
MicrophoneAudioDispatcher dispatcher = 
    new MicrophoneAudioDispatcher(sampleRate, bufferSize, overlap);

// Create pitch detection handler
PitchDetectionHandler pdh = new PitchDetectionHandler() {
    @Override
    public void handlePitch(PitchDetectionResult result, AudioEvent e) {
        float pitchInHz = result.getPitch();
        float probability = result.getProbability();
        
        // Update UI on main thread
        runOnUiThread(() -> {
            textView.setText(String.format("Pitch: %.2f Hz", pitchInHz));
        });
    }
};

// Add pitch processor
AudioProcessor pitchProcessor = new PitchProcessor(
    PitchEstimationAlgorithm.FFT_YIN, 
    sampleRate, 
    bufferSize, 
    pdh
);
dispatcher.addAudioProcessor(pitchProcessor);

// Start in background thread
new Thread(dispatcher, "Audio Dispatcher").start();
```

### 2. Sound Level Detection

```java
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher;
import be.hogent.tarsos.dsp.SilenceDetector;

int sampleRate = 22050;
int bufferSize = 1024;
int overlap = 0;

MicrophoneAudioDispatcher dispatcher = 
    new MicrophoneAudioDispatcher(sampleRate, bufferSize, overlap);

// Create silence detector with threshold in dB
double threshold = -70.0;
SilenceDetector silenceDetector = new SilenceDetector(threshold, false);

// Add custom processor to display sound level
AudioProcessor soundLevelProcessor = new AudioProcessor() {
    @Override
    public boolean process(AudioEvent audioEvent) {
        double currentSPL = silenceDetector.currentSPL(audioEvent);
        boolean isSilence = audioEvent.isSilence(threshold);
        
        runOnUiThread(() -> {
            soundLevelText.setText(String.format("%.2f dB", currentSPL));
            statusText.setText(isSilence ? "SILENCE" : "SOUND");
        });
        
        return true;
    }

    @Override
    public void processingFinished() {
        // Cleanup
    }
};

dispatcher.addAudioProcessor(silenceDetector);
dispatcher.addAudioProcessor(soundLevelProcessor);

new Thread(dispatcher, "Audio Dispatcher").start();
```

### 3. Audio File Playback with Effects

```java
import be.hogent.tarsos.dsp.AndroidAudioDispatcher;
import be.hogent.tarsos.dsp.AndroidAudioPlayer;
import be.hogent.tarsos.dsp.GainProcessor;
import be.hogent.tarsos.dsp.AudioFormat;

File audioFile = new File("/path/to/audio.wav");

int bufferSize = 4096;
int overlap = 0;

// Create dispatcher
AndroidAudioDispatcher dispatcher = 
    new AndroidAudioDispatcher(audioFile, bufferSize, overlap);

// Add gain effect
double gain = 1.5; // 150% volume
GainProcessor gainProcessor = new GainProcessor(gain);
dispatcher.addAudioProcessor(gainProcessor);

// Add audio player
AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
AndroidAudioPlayer player = new AndroidAudioPlayer(format);
dispatcher.addAudioProcessor(player);

// Start playback
new Thread(dispatcher, "Audio Player").start();
```

### 4. FFT and Spectral Analysis

```java
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;
import be.hogent.tarsos.dsp.util.fft.FFT;

int sampleRate = 22050;
int bufferSize = 2048;
int overlap = bufferSize / 2;

MicrophoneAudioDispatcher dispatcher = 
    new MicrophoneAudioDispatcher(sampleRate, bufferSize, overlap);

final FFT fft = new FFT(bufferSize);

AudioProcessor fftProcessor = new AudioProcessor() {
    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] audioBuffer = audioEvent.getFloatBuffer().clone();
        
        // Perform FFT
        float[] amplitudes = new float[bufferSize / 2];
        fft.forwardTransform(audioBuffer);
        fft.modulus(audioBuffer, amplitudes);
        
        // Process amplitudes (e.g., display spectrum)
        runOnUiThread(() -> {
            // Update spectrum visualization
            spectrumView.updateSpectrum(amplitudes);
        });
        
        return true;
    }

    @Override
    public void processingFinished() {
        // Cleanup
    }
};

dispatcher.addAudioProcessor(fftProcessor);
new Thread(dispatcher, "Audio Dispatcher").start();
```

## Example Applications

The `examples-android` module contains complete, working example applications:

1. **PitchDetectionActivity**: Real-time pitch detection from microphone
2. **SoundDetectorActivity**: Sound level monitoring with adjustable threshold
3. **AudioPlayerActivity**: Audio file playback with gain control
4. **MainActivity**: Launcher that shows all examples

To run the examples:

```bash
# Build and install on device
./gradlew examples-android:installDebug

# Or open in Android Studio and run
```

## Android-Specific Classes

### MicrophoneAudioDispatcher

Captures audio from the device microphone using `AudioRecord`:

```java
MicrophoneAudioDispatcher(
    int sampleRate,           // e.g., 22050, 44100
    int audioBufferSize,      // e.g., 1024, 2048
    int bufferOverlap         // e.g., 0, 512
)
```

### AndroidAudioPlayer

Plays audio using `AudioTrack`:

```java
AndroidAudioPlayer(AudioFormat format)
AndroidAudioPlayer(AudioFormat format, int bufferSizeInBytes)
```

### AndroidAudioDispatcher

Processes audio from files or streams:

```java
AndroidAudioDispatcher(File audioFile, int bufferSize, int overlap)
AndroidAudioDispatcher(InputStream stream, long streamLength, int bufferSize, int overlap)
```

### AndroidUIUpdater

Helper for updating UI from audio processing threads:

```java
AndroidUIUpdater updater = new AndroidUIUpdater();
updater.post(() -> {
    textView.setText("Updated from audio thread");
});
```

## Best Practices

### 1. Threading

Always run audio processing on a background thread:

```java
Thread audioThread = new Thread(dispatcher, "Audio Processing");
audioThread.start();
```

### 2. UI Updates

Use `runOnUiThread()` or `AndroidUIUpdater` to update UI from audio processors:

```java
runOnUiThread(() -> {
    textView.setText("Update from audio thread");
});
```

### 3. Resource Cleanup

Always stop and cleanup in `onDestroy()`:

```java
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

### 4. Buffer Sizes

Choose appropriate buffer sizes based on your use case:

- **Pitch detection**: 1024-2048 samples
- **FFT analysis**: 2048-8192 samples (power of 2)
- **Low latency**: 512-1024 samples
- **File playback**: 4096-8192 samples

### 5. Sample Rates

Common sample rates on Android:

- **8000 Hz**: Telephone quality
- **16000 Hz**: Wideband speech
- **22050 Hz**: Good for music analysis (half of CD quality)
- **44100 Hz**: CD quality (most compatible)
- **48000 Hz**: Professional audio

## Troubleshooting

### "Permission Denial: recording audio"

Make sure you've:
1. Added `RECORD_AUDIO` permission to AndroidManifest.xml
2. Requested runtime permission on Android 6.0+
3. User has granted the permission

### "No sound output"

Check:
1. Device volume is not muted
2. AudioFormat matches your audio data
3. Buffer sizes are appropriate
4. AudioTrack is properly initialized

### "Crackling or distorted audio"

Try:
1. Increasing buffer size
2. Using a background thread with higher priority
3. Avoiding heavy processing in the audio callback
4. Using smaller overlap values

### "OutOfMemoryError"

Reduce:
1. Buffer sizes
2. Number of concurrent processors
3. Frequency of UI updates

## Performance Tips

1. **Reuse buffers**: TarsosDSP already reuses buffers internally
2. **Minimize allocations**: Avoid creating objects in the audio callback
3. **Use appropriate algorithms**: FFT-YIN is faster than regular YIN
4. **Batch UI updates**: Don't update UI on every audio buffer
5. **Profile your code**: Use Android Profiler to identify bottlenecks

## Advanced Usage

### Custom Audio Processors

Create your own audio processor:

```java
public class MyProcessor implements AudioProcessor {
    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] buffer = audioEvent.getFloatBuffer();
        // Process audio data
        
        // Return false to stop the processing chain
        return true;
    }

    @Override
    public void processingFinished() {
        // Cleanup resources
    }
}
```

### Chaining Processors

Multiple processors can be chained:

```java
dispatcher.addAudioProcessor(new SilenceDetector(-70));
dispatcher.addAudioProcessor(new PitchProcessor(...));
dispatcher.addAudioProcessor(new GainProcessor(1.5));
dispatcher.addAudioProcessor(new AndroidAudioPlayer(format));
```

### Real-time Effects

Apply effects in real-time:

```java
// Pitch shifting
RateTransposer rateTransposer = new RateTransposer(0.9); // -10%
dispatcher.addAudioProcessor(rateTransposer);

// Low-pass filter
LowPassFS lowPass = new LowPassFS(1000, sampleRate); // 1kHz cutoff
dispatcher.addAudioProcessor(lowPass);

// Delay effect
DelayEffect delay = new DelayEffect(0.5, 0.3, sampleRate); // 500ms, 30% feedback
dispatcher.addAudioProcessor(delay);
```

## Architecture

```
┌─────────────────────────────────────────────────┐
│           Android Application Layer              │
│  (Activities, Services, UI)                      │
└───────────────────┬─────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────┐
│         TarsosDSP Android Layer                  │
│  - MicrophoneAudioDispatcher (AudioRecord)       │
│  - AndroidAudioPlayer (AudioTrack)               │
│  - AndroidAudioDispatcher (File I/O)             │
└───────────────────┬─────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────┐
│         TarsosDSP Core Library                   │
│  - AudioProcessors                               │
│  - Pitch Detection                               │
│  - FFT & Spectral Analysis                       │
│  - Filters & Effects                             │
│  - Time Stretching & Pitch Shifting              │
└─────────────────────────────────────────────────┘
```

## Additional Resources

- **Main TarsosDSP Website**: http://0110.be/tag/TarsosDSP
- **GitHub Repository**: https://github.com/JorenSix/TarsosDSP
- **API Documentation**: http://tarsos.0110.be/releases/TarsosDSP/
- **Research Paper**: "TarsosDSP, a Real-Time Audio Processing Framework in Java"

## Citation

If you use TarsosDSP in academic research, please cite:

```
@inproceedings{six2014tarsosdsp,
  author      = {Joren Six and Olmo Cornelis and Marc Leman},
  title       = {{TarsosDSP, a Real-Time Audio Processing Framework in Java}},
  booktitle   = {{Proceedings of the 53rd AES Conference (AES 53rd)}}, 
  year        =  2014
}
```

## License

TarsosDSP is licensed under the GNU General Public License (GPL). See the `license.txt` file for details.

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test on multiple Android devices
5. Submit a pull request

## Support

For questions and issues:

- GitHub Issues: https://github.com/JorenSix/TarsosDSP/issues
- Email: joren.six@ugent.be

## Changelog

### Version 1.7.0 - Android Support
- ✅ Added AndroidAudioPlayer using AudioTrack
- ✅ Added AndroidAudioDispatcher for file processing
- ✅ Added MicrophoneAudioDispatcher for real-time capture
- ✅ Added utility classes (AndroidUIUpdater, AndroidFFMPEGLocator)
- ✅ Added complete example applications
- ✅ Tested on Android 5.0+ (API 21+)

## Credits

TarsosDSP was developed by Joren Six at University College Ghent, School of Arts.

Android support and additional contributions by the TarsosDSP community.
