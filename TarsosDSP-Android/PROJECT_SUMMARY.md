# TarsosDSP-Android Project Summary

## Overview

TarsosDSP-Android is a complete Android port of the TarsosDSP audio processing library, providing real-time audio processing capabilities for Android applications.

## Project Structure

```
TarsosDSP-Android/
├── tarsosdsp-android/              # Core library module
│   ├── src/main/
│   │   ├── java/be/hogent/tarsos/dsp/
│   │   │   ├── AudioDispatcher.java
│   │   │   ├── AudioEvent.java
│   │   │   ├── AudioFormat.java
│   │   │   ├── AudioProcessor.java
│   │   │   ├── io/                # I/O classes
│   │   │   │   ├── android/       # Android-specific implementations
│   │   │   │   │   ├── AndroidAudioPlayer.java
│   │   │   │   │   ├── AndroidAudioInputStream.java
│   │   │   │   │   ├── AndroidFFMPEGLocator.java
│   │   │   │   │   └── AudioDispatcherFactory.java
│   │   │   │   ├── TarsosDSPAudioFormat.java
│   │   │   │   ├── TarsosDSPAudioInputStream.java
│   │   │   │   └── UniversalAudioInputStream.java
│   │   │   ├── pitch/             # Pitch detection algorithms
│   │   │   │   ├── YIN.java
│   │   │   │   ├── FastYin.java
│   │   │   │   ├── McLeodPitchMethod.java
│   │   │   │   ├── DynamicWavelet.java
│   │   │   │   ├── AMDF.java
│   │   │   │   ├── PitchProcessor.java
│   │   │   │   └── PitchDetectionHandler.java
│   │   │   ├── filters/           # Audio filters
│   │   │   ├── effects/           # Audio effects
│   │   │   ├── onsets/            # Onset detection
│   │   │   ├── beatroot/          # Beat detection
│   │   │   ├── mfcc/              # MFCC extraction
│   │   │   ├── synthesis/         # Audio synthesis
│   │   │   ├── resample/          # Resampling
│   │   │   └── util/              # Utilities
│   │   │       ├── android/       # Android helpers
│   │   │       │   ├── PermissionHelper.java
│   │   │       │   ├── PitchConverter.java
│   │   │       │   └── AudioFormatHelper.java
│   │   │       └── fft/           # FFT & windowing
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── consumer-rules.pro
│
├── tarsosdsp-android-example/     # Example application
│   ├── src/main/
│   │   ├── java/be/hogent/tarsos/dsp/example/
│   │   │   └── MainActivity.java
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml
│   │   │   ├── values/
│   │   │   │   ├── strings.xml
│   │   │   │   ├── colors.xml
│   │   │   │   └── themes.xml
│   │   │   └── xml/
│   │   │       ├── backup_rules.xml
│   │   │       └── data_extraction_rules.xml
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
│
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
├── build.gradle               # Root build file
├── settings.gradle            # Project settings
├── gradle.properties          # Gradle properties
│
├── README.md                  # Main documentation
├── QUICKSTART.md              # Quick start guide
├── CHANGELOG.md               # Version history
├── LICENSE.txt                # License information
├── CONTRIBUTING.md            # Contribution guidelines
├── BUILD_INSTRUCTIONS.md      # Build instructions
├── PROJECT_SUMMARY.md         # This file
└── .gitignore                 # Git ignore rules
```

## Key Components

### Core Library (tarsosdsp-android)

#### Audio I/O
- **AndroidAudioPlayer**: Audio playback using AudioTrack
- **AndroidAudioInputStream**: Audio capture using AudioRecord
- **AudioDispatcherFactory**: Convenient factory methods
- **AudioDispatcher**: Audio processing pipeline

#### Pitch Detection
- **YIN**: Time-domain pitch detector
- **FFT_YIN**: FFT-based YIN (faster)
- **MPM**: McLeod Pitch Method
- **DynamicWavelet**: Wavelet-based detection
- **AMDF**: Average Magnitude Difference Function

#### Audio Processing
- **Filters**: BandPass, HighPass, LowPass
- **Effects**: Delay, Flanger
- **Onset Detection**: Complex, Percussion
- **Beat Detection**: BeatRoot integration
- **MFCC**: Feature extraction
- **Time Stretching**: WSOLA algorithm
- **Synthesis**: Simple waveform generation

#### Utilities
- **PermissionHelper**: Android permission management
- **PitchConverter**: Frequency/MIDI/note conversion
- **AudioFormatHelper**: Format conversion utilities
- **FFT**: Fast Fourier Transform with windowing

### Example Application (tarsosdsp-android-example)

A complete example app demonstrating:
- Real-time pitch detection
- Musical note display
- Confidence/probability visualization
- Runtime permission handling
- Material Design UI
- Proper lifecycle management

## Technical Specifications

### Requirements
- **Minimum SDK**: API 21 (Android 5.0)
- **Target SDK**: API 34 (Android 14)
- **Language**: Java 8
- **Build System**: Gradle 8.0
- **UI Framework**: AndroidX, Material Components

### Permissions
- `RECORD_AUDIO`: Required for microphone access
- `MODIFY_AUDIO_SETTINGS`: Optional for audio configuration

### Dependencies
- AndroidX AppCompat
- Material Components
- JUnit (testing)
- Espresso (Android testing)

## Features

### Audio Input
- Microphone capture via AudioRecord
- Configurable sample rates (8kHz - 48kHz)
- Mono and stereo support
- Configurable buffer sizes

### Audio Output
- AudioTrack playback
- Streaming and static modes
- Real-time audio processing

### Signal Processing
- 5+ pitch detection algorithms
- Multiple filter types
- Audio effects
- Onset/beat detection
- Feature extraction (MFCC)
- Time/pitch manipulation

### Android Integration
- Permission helpers
- Format converters
- Lifecycle-aware components
- Background processing support

## Usage Patterns

### Basic Pitch Detection

```java
AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
PitchProcessor processor = new PitchProcessor(
    PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pitchHandler);
dispatcher.addAudioProcessor(processor);
new Thread(dispatcher).start();
```

### Audio Effects Chain

```java
dispatcher.addAudioProcessor(new LowPassFS(1000, 22050));
dispatcher.addAudioProcessor(new DelayEffect(0.3, 0.5, 22050));
dispatcher.addAudioProcessor(new AndroidAudioPlayer(audioFormat));
```

### Onset Detection

```java
ComplexOnsetDetector detector = new ComplexOnsetDetector(1024, onsetHandler);
dispatcher.addAudioProcessor(detector);
```

## Build & Distribution

### Building
```bash
./gradlew build                          # Build everything
./gradlew :tarsosdsp-android:build      # Build library only
./gradlew :tarsosdsp-android-example:assembleDebug  # Build example
```

### Publishing
```bash
./gradlew :tarsosdsp-android:publishToMavenLocal
```

### Testing
```bash
./gradlew test                           # Unit tests
./gradlew connectedAndroidTest          # Instrumentation tests
```

## Documentation

### User Documentation
- **README.md**: Comprehensive library documentation
- **QUICKSTART.md**: Quick start guide with examples
- **BUILD_INSTRUCTIONS.md**: Detailed build instructions

### Developer Documentation
- **CONTRIBUTING.md**: Contribution guidelines
- **CHANGELOG.md**: Version history and changes
- **Javadoc**: Generated API documentation

### Example Code
- Complete example application
- Code samples in README
- Inline code documentation

## Performance Characteristics

### CPU Usage
- FFT_YIN: Low (recommended for real-time)
- YIN: Medium
- MPM: High (highest quality)

### Memory Usage
- Buffer size dependent
- Typical: 2-8 MB for 1024 sample buffer

### Latency
- 512 samples: ~23ms @ 22050 Hz (low latency)
- 1024 samples: ~46ms @ 22050 Hz (balanced)
- 2048 samples: ~93ms @ 22050 Hz (high accuracy)

## Common Use Cases

1. **Music Tuner**: Real-time pitch detection for instruments
2. **Karaoke Apps**: Pitch tracking for singing
3. **Audio Analysis**: Spectrum analysis and visualization
4. **Sound Detection**: Clap detection, voice activity
5. **Audio Effects**: Real-time effects processing
6. **Music Education**: Note recognition and training
7. **Audio Recording**: With effects and filters
8. **Beat Detection**: Tempo and rhythm analysis

## Limitations

- No native codec support (MP3, AAC, etc.) without additional libraries
- FFT-based algorithms require power-of-2 buffer sizes
- High CPU usage on low-end devices with complex processing
- Microphone quality varies across devices

## Future Enhancements

- Kotlin support and Kotlin DSL
- Jetpack Compose UI examples
- Audio file I/O helpers
- Additional algorithms
- Performance optimizations
- Background service examples
- Widget examples
- OpenSL ES support

## License

GPL v3 (compatible with original TarsosDSP)

## Credits

Based on TarsosDSP by Joren Six
Android port: 2024

## Related Projects

- **TarsosDSP**: Original Java library
- **aubio**: Similar C library
- **librosa**: Python audio analysis
- **Essentia**: C++ audio analysis

## Support & Contact

- GitHub Issues: For bug reports
- Documentation: README.md and wiki
- Examples: tarsosdsp-android-example module

---

**Version**: 2.5.0  
**Date**: 2024-11-04  
**Status**: Production Ready
