# Changelog

All notable changes to TarsosDSP-Android will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.5.0] - 2024-11-04

### Added
- Initial Android port of TarsosDSP
- Android-specific audio input using AudioRecord
- Android-specific audio output using AudioTrack
- AudioDispatcherFactory for easy creation of audio dispatchers
- TarsosDSPAudioInputStream for Android compatibility
- Example application demonstrating real-time pitch detection
- Material Design 3 UI for example app
- Runtime permission handling for Android 6.0+
- Support for Android API 21 (Lollipop) and above
- Gradle build system for Android
- Comprehensive README with examples
- Quick Start Guide
- Complete Javadoc documentation

### Android-Specific Features
- `AndroidAudioPlayer` - Audio playback using AudioTrack
- `AndroidAudioInputStream` - Audio input using AudioRecord
- `AudioDispatcherFactory` - Convenient factory methods
- `AndroidFFMPEGLocator` - FFMPEG integration helper
- Example app with pitch detection, note display, and probability meter

### Included TarsosDSP Features
- Pitch detection algorithms (YIN, FFT_YIN, MPM, Dynamic Wavelet, AMDF)
- Audio effects (Delay, Flanger)
- Filters (BandPass, HighPass, LowPass)
- Onset detection (Complex, Percussion)
- Beat detection (BeatRoot)
- MFCC (Mel-frequency cepstral coefficients)
- Time stretching (WSOLA)
- Audio synthesis
- FFT with multiple window functions
- Sample rate conversion
- Gain processing
- Silence detection
- Envelope follower
- Constant-Q transform
- AutoCorrelation

### Technical Details
- Minimum SDK: API 21 (Android 5.0 Lollipop)
- Target SDK: API 34 (Android 14)
- Java 8 compatibility
- AndroidX support
- ProGuard rules included
- Consumer ProGuard rules for library consumers

### Documentation
- Complete README with usage examples
- Quick Start Guide
- API documentation
- Example application source code
- Architecture overview
- Performance tips
- Common use cases
- Troubleshooting guide

### Build System
- Gradle 8.0
- Android Gradle Plugin 8.1.0
- Maven publishing support
- Source and Javadoc JAR generation

## [Unreleased]

### Planned Features
- Audio file format support (MP3, OGG, FLAC)
- Real-time spectrum analyzer example
- Beat detection example application
- Audio recording and playback utilities
- Performance optimizations for low-end devices
- Additional example applications
- Kotlin DSL support
- Compose UI example
- Background service example
- Widget example

### Potential Improvements
- Native library integration for better performance
- OpenSL ES support for lower latency
- Audio focus handling
- Bluetooth audio device support
- USB audio device support
- Multi-channel audio support
- Real-time audio effects chain
- Audio session management

---

## Version History

Based on TarsosDSP version history:

### TarsosDSP 1.7 (2013-10-08)
- MFCC extraction
- Constant-Q transform example
- Visualization classes

### TarsosDSP 1.6 (2013-06-12)
- Onset detection algorithms
- Beat detection (BeatRoot integration)
- Constant-Q transform

### TarsosDSP 1.5 (2013-04-30)
- Maven support (Malaryta release)
- FFT window functions (from Minim)

### TarsosDSP 1.4 (2012-10-31)
- Resampling (libresample4j)
- Pitch shifting
- CLI and UI examples

### TarsosDSP 1.3 (2012-09-19)
- Audio synthesis
- Feature extraction
- Enhanced documentation

### TarsosDSP 1.2 (2012-08-21)
- Enhanced PitchDetectionResult
- Envelope follower

### TarsosDSP 1.1 (2012-06-04)
- StopAudioProcessor
- FastYin (Matthias Mauch)
- AMDF (Eder Souza)

### TarsosDSP 1.0 (2012-04-24)
- Initial release
- YIN pitch detection
- Time stretching (WSOLA)
- Basic audio processing

---

For more information about the original TarsosDSP versions, see:
http://tarsos.0110.be/releases/TarsosDSP/
