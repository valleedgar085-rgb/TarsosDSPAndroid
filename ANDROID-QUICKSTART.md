# TarsosDSP Android Quick Start Guide

Get up and running with TarsosDSP on Android in 5 minutes!

## 1. Add TarsosDSP to Your Project

### build.gradle (app level)
```gradle
android {
    compileSdk 34
    
    defaultConfig {
        minSdk 21
        targetSdk 34
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Option A: Include as module
    implementation project(':tarsos-dsp')
    
    // Option B: Include as JAR
    // implementation files('libs/tarsos-dsp-1.7.0.jar')
}
```

## 2. Add Permissions

### AndroidManifest.xml
```xml
<manifest>
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    
    <application>
        <!-- Your activities here -->
    </application>
</manifest>
```

## 3. Request Runtime Permissions

```java
private static final int PERMISSION_REQUEST = 1;

private void requestPermissions() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.RECORD_AUDIO},
            PERMISSION_REQUEST);
    }
}

@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == PERMISSION_REQUEST) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted - start audio processing
            startAudioProcessing();
        }
    }
}
```

## 4. Basic Pitch Detection

```java
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.AudioEvent;

public class MyActivity extends Activity {
    
    private MicrophoneAudioDispatcher dispatcher;
    private Thread audioThread;
    
    private void startPitchDetection() {
        // Configure audio
        int sampleRate = 22050;
        int bufferSize = 1024;
        int overlap = 0;
        
        // Create dispatcher
        dispatcher = new MicrophoneAudioDispatcher(sampleRate, bufferSize, overlap);
        
        // Create pitch detection handler
        PitchDetectionHandler handler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitch = result.getPitch();
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    if (pitch != -1) {
                        textView.setText(String.format("Pitch: %.2f Hz", pitch));
                    }
                });
            }
        };
        
        // Add processor
        dispatcher.addAudioProcessor(new PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
            sampleRate,
            bufferSize,
            handler
        ));
        
        // Start in background thread
        audioThread = new Thread(dispatcher, "Audio Dispatcher");
        audioThread.start();
    }
    
    private void stopPitchDetection() {
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPitchDetection();
    }
}
```

## 5. Basic Sound Detection

```java
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher;
import be.hogent.tarsos.dsp.SilenceDetector;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;

private void startSoundDetection() {
    int sampleRate = 22050;
    int bufferSize = 1024;
    
    dispatcher = new MicrophoneAudioDispatcher(sampleRate, bufferSize, 0);
    
    // Create silence detector (-70 dB threshold)
    SilenceDetector silenceDetector = new SilenceDetector(-70.0, false);
    dispatcher.addAudioProcessor(silenceDetector);
    
    // Monitor sound level
    dispatcher.addAudioProcessor(new AudioProcessor() {
        @Override
        public boolean process(AudioEvent audioEvent) {
            double soundLevel = silenceDetector.currentSPL(audioEvent);
            boolean isSilence = audioEvent.isSilence(-70.0);
            
            runOnUiThread(() -> {
                levelText.setText(String.format("%.1f dB", soundLevel));
                statusText.setText(isSilence ? "Silence" : "Sound");
            });
            
            return true;
        }
        
        @Override
        public void processingFinished() {}
    });
    
    new Thread(dispatcher).start();
}
```

## 6. Play Audio File with Effects

```java
import be.hogent.tarsos.dsp.AndroidAudioDispatcher;
import be.hogent.tarsos.dsp.AndroidAudioPlayer;
import be.hogent.tarsos.dsp.GainProcessor;
import be.hogent.tarsos.dsp.AudioFormat;
import java.io.File;

private void playAudioFile() {
    try {
        File audioFile = new File("/path/to/audio.wav");
        
        // Create dispatcher
        AndroidAudioDispatcher dispatcher = 
            new AndroidAudioDispatcher(audioFile, 4096, 0);
        
        // Add gain effect (1.5 = 150% volume)
        dispatcher.addAudioProcessor(new GainProcessor(1.5));
        
        // Add audio player
        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        dispatcher.addAudioProcessor(new AndroidAudioPlayer(format));
        
        // Start playback
        new Thread(dispatcher).start();
        
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

## Common Algorithms & Parameters

### Pitch Detection Algorithms
```java
// Fast and accurate (recommended)
PitchProcessor.PitchEstimationAlgorithm.FFT_YIN

// Very accurate but slower
PitchProcessor.PitchEstimationAlgorithm.YIN

// Good for clear tones
PitchProcessor.PitchEstimationAlgorithm.MPM

// Very fast but less accurate
PitchProcessor.PitchEstimationAlgorithm.AMDF
```

### Sample Rates
- **8000 Hz**: Phone quality
- **16000 Hz**: Speech
- **22050 Hz**: Music analysis (recommended)
- **44100 Hz**: CD quality

### Buffer Sizes
- **512-1024**: Low latency, less accurate pitch
- **1024-2048**: Good balance (recommended)
- **2048-4096**: Higher latency, more accurate
- **4096-8192**: File playback

### Overlap
- **0**: No overlap, faster processing
- **bufferSize / 2**: Standard overlap for FFT
- **bufferSize * 3/4**: High overlap for smooth results

## Complete Minimal Example

```java
package com.example.myapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler;

public class SimpleActivity extends Activity {
    
    private TextView pitchText;
    private MicrophoneAudioDispatcher dispatcher;
    private Thread audioThread;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        pitchText = new TextView(this);
        setContentView(pitchText);
        
        // Request permission first (not shown)
        startPitchDetection();
    }
    
    private void startPitchDetection() {
        dispatcher = new MicrophoneAudioDispatcher(22050, 1024, 0);
        
        dispatcher.addAudioProcessor(new PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
            22050, 1024,
            (result, e) -> runOnUiThread(() -> 
                pitchText.setText(String.format("%.2f Hz", result.getPitch()))
            )
        ));
        
        audioThread = new Thread(dispatcher);
        audioThread.start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dispatcher != null) dispatcher.stop();
    }
}
```

## Troubleshooting

**App crashes with SecurityException**
→ Request RECORD_AUDIO permission at runtime

**No audio output**
→ Check device volume, verify AudioFormat matches your audio

**Pitch detection returns -1**
→ Increase buffer size or try different algorithm

**UI freezes**
→ Make sure audio processing runs on background thread

**OutOfMemoryError**
→ Reduce buffer sizes and number of processors

## Next Steps

1. Check out the full examples in `/examples-android`
2. Read the complete documentation in `README-ANDROID.md`
3. Explore available AudioProcessors and effects
4. Customize for your specific use case

## Need Help?

- 📖 Full Documentation: `README-ANDROID.md`
- 🌐 Website: http://0110.be/tag/TarsosDSP
- 💻 GitHub: https://github.com/JorenSix/TarsosDSP
- 📧 Email: joren.six@ugent.be
