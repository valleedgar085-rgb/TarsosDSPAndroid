# Getting Started with TarsosDSP on Android 🎵📱

Welcome! This guide will get you up and running with TarsosDSP on Android in minutes.

## What is TarsosDSP?

TarsosDSP is a powerful Java library for audio processing that now runs natively on Android. Use it to:

- 🎼 **Detect pitch** from audio (build a tuner app)
- 📊 **Analyze sound levels** (voice activity detection)  
- 🎵 **Apply audio effects** (filters, gain, time-stretch)
- 📈 **Extract features** (FFT, MFCC, onset detection)
- 🎹 **Build music apps** (transcription, training tools)

## Quick Navigation 🧭

**Choose your path:**

### 🏃‍♂️ I want to get started NOW
→ Read **[ANDROID-QUICKSTART.md](ANDROID-QUICKSTART.md)**  
5-minute guide with copy-paste examples

### 📚 I want complete documentation
→ Read **[README-ANDROID.md](README-ANDROID.md)**  
Comprehensive guide with everything you need

### 💻 I want to see working examples
→ Check **[examples-android/](examples-android/)**  
Complete demo apps you can run immediately

### 🔍 I want to know what's included
→ Read **[ANDROID-IMPLEMENTATION-SUMMARY.md](ANDROID-IMPLEMENTATION-SUMMARY.md)**  
Full overview of the Android implementation

## Super Quick Start ⚡

### 1. Add to your project

**build.gradle (app level):**
```gradle
dependencies {
    implementation project(':tarsos-dsp')
}
```

### 2. Add permission

**AndroidManifest.xml:**
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

### 3. Request runtime permission

```java
ActivityCompat.requestPermissions(this,
    new String[]{Manifest.permission.RECORD_AUDIO}, 1);
```

### 4. Detect pitch from microphone

```java
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;

// Create dispatcher
MicrophoneAudioDispatcher dispatcher = 
    new MicrophoneAudioDispatcher(22050, 1024, 0);

// Add pitch detection
dispatcher.addAudioProcessor(new PitchProcessor(
    PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
    22050, 1024,
    (result, e) -> {
        float pitch = result.getPitch();
        runOnUiThread(() -> 
            textView.setText("Pitch: " + pitch + " Hz")
        );
    }
));

// Start processing
new Thread(dispatcher).start();
```

**Done! You're detecting pitch from the microphone!** 🎉

## What Can I Build? 🛠️

### Music Apps
- Guitar/Instrument tuner
- Vocal pitch trainer
- Music transcription tool
- Karaoke pitch display

### Audio Tools
- Sound level meter
- Noise monitor
- Spectrum analyzer
- Audio recorder with effects

### Voice Apps
- Voice activity detector
- Speech analyzer
- Language learning tools
- Singing coach

### Creative Apps
- Real-time audio effects
- Loop station
- Beat detector
- Rhythm game

## Example Apps Included 📦

We've included **4 complete, working example apps**:

### 1. Pitch Detection
Real-time pitch detection from microphone
- Shows frequency in Hz
- Displays detection probability
- Perfect for tuner apps

### 2. Sound Detector
Monitor sound levels and detect silence
- dB SPL display
- Adjustable threshold
- Sound vs silence indicator

### 3. Audio Player
Play audio files with real-time effects
- Gain control
- Progress tracking
- Effect chaining

### 4. Main Launcher
Entry point showing all examples
- Easy navigation
- Clean interface
- Quick testing

**Run them now:**
```bash
# Uncomment in settings.gradle:
# include ':examples-android'

./gradlew examples-android:installDebug
```

## Core Features ✨

### Audio Input
- ✅ Microphone (real-time)
- ✅ Audio files (WAV)
- ✅ Streams

### Processing
- ✅ Pitch detection (5 algorithms)
- ✅ FFT analysis
- ✅ MFCC extraction
- ✅ Sound level detection
- ✅ Onset/beat detection

### Effects
- ✅ Filters (low-pass, high-pass, band-pass)
- ✅ Gain/volume control
- ✅ Delay/echo
- ✅ Flanger
- ✅ Time stretching
- ✅ Pitch shifting

### Output
- ✅ AudioTrack playback
- ✅ Low latency
- ✅ Streaming

## System Requirements 📋

- **Min Android Version**: API 21 (Android 5.0)
- **Target Version**: API 34 (Android 14)
- **Java Version**: Java 8
- **Permissions**: RECORD_AUDIO (for microphone)

## Documentation Files 📖

| File | Purpose | When to Read |
|------|---------|--------------|
| **ANDROID-QUICKSTART.md** | Quick start guide | Start here! |
| **README-ANDROID.md** | Complete documentation | For deep dive |
| **examples-android/README.md** | Example app guide | To run examples |
| **ANDROID-IMPLEMENTATION-SUMMARY.md** | Implementation overview | To see what's included |
| **GETTING-STARTED-ANDROID.md** | This file | You are here! |

## Common Use Cases 🎯

### "I want to build a tuner app"
1. Read ANDROID-QUICKSTART.md → Pitch Detection section
2. Run PitchDetectionActivity example
3. Customize for your instrument

### "I need to detect when someone speaks"
1. Read ANDROID-QUICKSTART.md → Sound Detection section
2. Run SoundDetectorActivity example
3. Adjust threshold for your use case

### "I want to add audio effects"
1. Read README-ANDROID.md → Advanced Usage section
2. Check AudioPlayerActivity example
3. Chain effects as needed

### "I need FFT spectrum analysis"
1. Read README-ANDROID.md → FFT section
2. See code example for FFT
3. Customize for your visualization

## Need Help? 🆘

### Quick Issues
Check **ANDROID-QUICKSTART.md** → Troubleshooting section

### Deep Issues
Check **README-ANDROID.md** → Troubleshooting section

### Still Stuck?
- 💻 GitHub Issues: https://github.com/JorenSix/TarsosDSP/issues
- 📧 Email: joren.six@ugent.be
- 🌐 Website: http://0110.be/tag/TarsosDSP

## Next Steps 🚀

**You're ready to go! Here's what to do:**

1. ✅ Read [ANDROID-QUICKSTART.md](ANDROID-QUICKSTART.md) (5 min)
2. ✅ Run the example apps (5 min)
3. ✅ Copy code for your use case (5 min)
4. ✅ Build something awesome! (∞)

## Tips for Success 💡

1. **Start simple**: Begin with one example
2. **Test on device**: Emulators have limited audio support
3. **Check permissions**: Runtime permissions are required on Android 6.0+
4. **Use background threads**: Never block the UI thread
5. **Clean up resources**: Always stop dispatchers in onDestroy()

## Algorithm Quick Reference 🔬

### Pitch Detection Algorithms

| Algorithm | Speed | Accuracy | Best For |
|-----------|-------|----------|----------|
| **FFT_YIN** | Fast | High | General use (recommended) |
| **YIN** | Medium | Very High | Studio quality |
| **MPM** | Medium | High | Pure tones |
| **AMDF** | Very Fast | Medium | Low-power devices |
| **DYNAMIC_WAVELET** | Medium | High | Alternative approach |

### Recommended Settings

| Use Case | Sample Rate | Buffer Size | Algorithm |
|----------|-------------|-------------|-----------|
| Tuner | 22050 | 1024 | FFT_YIN |
| Vocal pitch | 22050 | 2048 | YIN |
| Real-time effects | 22050 | 512 | - |
| Spectrum analysis | 44100 | 2048 | - |
| File processing | 44100 | 4096 | - |

## Code Snippets 📝

### Pitch Detection (copy-paste ready)
```java
MicrophoneAudioDispatcher dispatcher = 
    new MicrophoneAudioDispatcher(22050, 1024, 0);
dispatcher.addAudioProcessor(new PitchProcessor(
    PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
    22050, 1024,
    (result, e) -> runOnUiThread(() -> 
        textView.setText(result.getPitch() + " Hz")
    )
));
new Thread(dispatcher).start();
```

### Sound Detection (copy-paste ready)
```java
SilenceDetector detector = new SilenceDetector(-70, false);
dispatcher.addAudioProcessor(detector);
dispatcher.addAudioProcessor(audioEvent -> {
    double level = detector.currentSPL(audioEvent);
    runOnUiThread(() -> textView.setText(level + " dB"));
    return true;
});
```

### Audio Effects (copy-paste ready)
```java
dispatcher.addAudioProcessor(new GainProcessor(1.5)); // 150% volume
dispatcher.addAudioProcessor(new LowPassFS(1000, sampleRate)); // 1kHz cutoff
dispatcher.addAudioProcessor(new AndroidAudioPlayer(format));
```

## Success Stories 🌟

TarsosDSP is used in:
- Professional music apps
- Audio analysis tools
- Educational software
- Research projects
- Hobbyist applications

**You can build the next one!** 🚀

## License 📄

GPL - See license.txt

## Let's Go! 🎉

You now have everything you need to use TarsosDSP on Android!

**Start with:** [ANDROID-QUICKSTART.md](ANDROID-QUICKSTART.md)

**Questions?** Check the docs or reach out!

**Happy coding! 🎵**
