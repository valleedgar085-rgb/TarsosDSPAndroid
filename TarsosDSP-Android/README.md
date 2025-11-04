# TarsosDSP-Android

TarsosDSP-Android is a comprehensive Android port of the TarsosDSP audio processing library. It provides real-time audio processing capabilities for Android applications with a clean, easy-to-use API.

## Features

- **Real-time Audio Processing**: Process audio from microphone or audio files in real-time
- **Pitch Detection**: Multiple algorithms including YIN, FFT-YIN, McLeod Pitch Method (MPM), and Dynamic Wavelet
- **Audio Effects**: Delay, flanger, and other audio effects
- **Filters**: Band-pass, high-pass, and low-pass filters
- **Onset Detection**: Detect percussion onsets and musical events
- **Beat Detection**: Extract beat information from audio
- **MFCC**: Mel-frequency cepstral coefficients extraction
- **Time Stretching**: WSOLA-based time stretching without pitch change
- **Audio Synthesis**: Simple sound synthesis capabilities
- **Android-optimized**: Uses Android's AudioRecord and AudioTrack APIs

## Installation

### Using Gradle

Add the following to your project's `build.gradle`:

```gradle
dependencies {
    implementation project(':tarsosdsp-android')
}
```

Or build and publish to your local Maven repository:

```bash
./gradlew :tarsosdsp-android:publishToMavenLocal
```

Then add to your `build.gradle`:

```gradle
repositories {
    mavenLocal()
}

dependencies {
    implementation 'be.hogent.tarsos:tarsosdsp-android:2.5'
}
```

## Requirements

- **Minimum SDK**: API 21 (Android 5.0 Lollipop)
- **Target SDK**: API 34 (Android 14)
- **Java**: Java 8 or higher
- **Permissions**: `RECORD_AUDIO` (for microphone access)

## Quick Start

### Pitch Detection Example

```java
import be.hogent.tarsos.dsp.AudioDispatcher;
import be.hogent.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;
import be.hogent.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

// Create audio dispatcher from default microphone
AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

// Create pitch detection handler
PitchDetectionHandler pdh = new PitchDetectionHandler() {
    @Override
    public void handlePitch(PitchDetectionResult result, AudioEvent e) {
        final float pitchInHz = result.getPitch();
        final float probability = result.getProbability();
        
        if (result.isPitched()) {
            Log.d("Pitch", String.format("Detected pitch: %.2f Hz (%.2f probability)", 
                pitchInHz, probability));
        }
    }
};

// Add pitch processor with FFT-YIN algorithm
AudioProcessor pitchProcessor = new PitchProcessor(
    PitchEstimationAlgorithm.FFT_YIN, 
    22050, 
    1024, 
    pdh
);
dispatcher.addAudioProcessor(pitchProcessor);

// Start processing in background thread
Thread audioThread = new Thread(dispatcher, "Audio Dispatcher");
audioThread.start();
```

### Playing Audio

```java
import be.hogent.tarsos.dsp.io.android.AndroidAudioPlayer;

// Create audio player
AndroidAudioPlayer player = new AndroidAudioPlayer(audioFormat);

// Add to dispatcher
dispatcher.addAudioProcessor(player);
```

### Sound Detection

```java
import be.hogent.tarsos.dsp.SilenceDetector;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;

SilenceDetector silenceDetector = new SilenceDetector(-70, false);
dispatcher.addAudioProcessor(silenceDetector);

dispatcher.addAudioProcessor(new AudioProcessor() {
    @Override
    public boolean process(AudioEvent audioEvent) {
        double rms = silenceDetector.currentSPL();
        Log.d("Sound", "Current sound level: " + rms + " dB");
        return true;
    }
    
    @Override
    public void processingFinished() {}
});
```

### Onset Detection

```java
import be.hogent.tarsos.dsp.onsets.OnsetHandler;
import be.hogent.tarsos.dsp.onsets.ComplexOnsetDetector;

ComplexOnsetDetector onsetDetector = new ComplexOnsetDetector(
    1024,
    new OnsetHandler() {
        @Override
        public void handleOnset(double time, double salience) {
            Log.d("Onset", "Onset detected at: " + time + "s");
        }
    }
);

dispatcher.addAudioProcessor(onsetDetector);
```

## Permissions

Add the following to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

Request runtime permission for Android 6.0+:

```java
if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.RECORD_AUDIO},
            AUDIO_PERMISSION_REQUEST);
}
```

## Pitch Detection Algorithms

TarsosDSP-Android supports multiple pitch detection algorithms:

- **YIN**: Fast and accurate pitch detection
- **FFT_YIN**: FFT-based YIN (recommended for real-time)
- **MPM** (McLeod Pitch Method): High-quality pitch detection
- **DYNAMIC_WAVELET**: Wavelet-based pitch tracking
- **AMDF**: Average Magnitude Difference Function

Example:

```java
// Use different algorithms
PitchProcessor yinProcessor = new PitchProcessor(
    PitchEstimationAlgorithm.YIN, 22050, 1024, pdh);

PitchProcessor mpmProcessor = new PitchProcessor(
    PitchEstimationAlgorithm.MPM, 22050, 1024, pdh);
```

## Audio Formats

TarsosDSP-Android works with standard audio formats:

```java
import be.hogent.tarsos.dsp.AudioFormat;

AudioFormat audioFormat = new AudioFormat(
    44100,  // Sample rate (Hz)
    16,     // Bits per sample
    1,      // Channels (1=mono, 2=stereo)
    true,   // Signed
    false   // Big endian
);
```

## Example Application

The library includes a complete example application demonstrating real-time pitch detection. See the `tarsosdsp-android-example` module for:

- Real-time microphone pitch detection
- Musical note display
- Probability visualization
- Runtime permission handling

To run the example:

```bash
./gradlew :tarsosdsp-android-example:installDebug
```

## Architecture

```
TarsosDSP-Android/
├── tarsosdsp-android/          # Core library
│   └── src/main/java/be/hogent/tarsos/dsp/
│       ├── AudioDispatcher.java
│       ├── AudioProcessor.java
│       ├── io/android/         # Android-specific implementations
│       │   ├── AndroidAudioPlayer.java
│       │   ├── AndroidAudioInputStream.java
│       │   └── AudioDispatcherFactory.java
│       ├── pitch/              # Pitch detection
│       ├── filters/            # Audio filters
│       ├── effects/            # Audio effects
│       ├── onsets/             # Onset detection
│       └── ...
└── tarsosdsp-android-example/  # Example app
    └── src/main/java/be/hogent/tarsos/dsp/example/
        └── MainActivity.java
```

## Performance Tips

1. **Buffer Size**: Larger buffers (1024-2048) are more accurate but have higher latency
2. **Sample Rate**: 22050 Hz is good for most pitch detection tasks
3. **Thread Management**: Always run audio processing in a background thread
4. **Algorithm Selection**: FFT_YIN offers the best balance of speed and accuracy

## Common Use Cases

### Music Tuner App
```java
// Real-time pitch detection for instrument tuning
AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
PitchProcessor processor = new PitchProcessor(
    PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
dispatcher.addAudioProcessor(processor);
```

### Audio Visualizer
```java
// Spectrum analysis for visualization
AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone();
FFT fft = new FFT(1024);
// Process FFT data for visualization
```

### Voice Activity Detection
```java
// Detect when someone is speaking
SilenceDetector detector = new SilenceDetector(-50, false);
dispatcher.addAudioProcessor(detector);
```

## Building from Source

```bash
# Clone the repository
git clone <repository-url>
cd TarsosDSP-Android

# Build the library
./gradlew :tarsosdsp-android:build

# Build the example app
./gradlew :tarsosdsp-android-example:assembleDebug

# Run tests
./gradlew test
```

## Credits

TarsosDSP-Android is based on [TarsosDSP](https://github.com/JorenSix/TarsosDSP) by Joren Six.

Original TarsosDSP credits:
- YIN algorithm: Alain de Cheveigné and Hideki Kawahara
- MPM algorithm: Philip McLeod and Geoff Wyvill
- WSOLA: Werner Verhelst and Marc Roelands
- And many other contributors (see original TarsosDSP documentation)

## License

TarsosDSP-Android maintains the same license as the original TarsosDSP library. Please refer to the LICENSE file for details.

## Citation

If you use TarsosDSP in academic research, please cite:

```
@inproceedings{six2014tarsosdsp,
  author      = {Joren Six and Olmo Cornelis and Marc Leman},
  title       = {{TarsosDSP, a Real-Time Audio Processing Framework in Java}},
  booktitle   = {{Proceedings of the 53rd AES Conference (AES 53rd)}}, 
  year        = 2014
}
```

## Support

For issues, questions, or contributions:
- Original TarsosDSP: https://github.com/JorenSix/TarsosDSP
- Android-specific issues: Please file issues in this repository

## FAQ

**Q: What's the minimum Android version?**  
A: Android 5.0 (API 21) and above.

**Q: Does it work with audio files?**  
A: Yes, you can create an AudioDispatcher from an InputStream.

**Q: Can I use multiple algorithms simultaneously?**  
A: Yes, add multiple PitchProcessor instances with different algorithms.

**Q: How do I reduce latency?**  
A: Use smaller buffer sizes (512-1024) and the FFT_YIN algorithm.

**Q: Does it support stereo audio?**  
A: Yes, but most algorithms work best with mono audio.

## Changelog

### Version 2.5 (2024)
- Initial Android port
- Android-specific AudioRecord/AudioTrack implementations
- Example application with pitch detection
- Gradle build system
- Android SDK 34 support
- Material Design 3 example app

## Roadmap

- [ ] Audio file format support (MP3, OGG, etc.)
- [ ] Real-time spectrum analyzer example
- [ ] Beat detection example
- [ ] Audio recording and playback utilities
- [ ] Performance optimizations for low-end devices
- [ ] Additional example applications

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

---

Made with ♥ for the Android audio processing community
