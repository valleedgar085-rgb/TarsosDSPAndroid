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

package be.hogent.tarsos.dsp.example;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.SeekBar;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher;
import be.hogent.tarsos.dsp.SilenceDetector;

/**
 * Example Android Activity that demonstrates sound/silence detection
 * using TarsosDSP on Android.
 * 
 * This activity captures audio from the microphone and displays the
 * current sound level and whether sound or silence is detected.
 * 
 * @author Joren Six
 */
public class SoundDetectorActivity extends Activity {

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;

    private TextView soundLevelText;
    private TextView statusText;
    private SeekBar thresholdSeekBar;
    private TextView thresholdText;
    private Button startButton;
    private Button stopButton;

    private MicrophoneAudioDispatcher dispatcher;
    private Thread audioThread;
    private SilenceDetector silenceDetector;

    private double threshold = -70.0; // dB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create UI programmatically
        createUI();

        // Request microphone permission
        requestMicrophonePermission();
    }

    private void createUI() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // Title
        TextView title = new TextView(this);
        title.setText("TarsosDSP Sound Detector");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 32);
        layout.addView(title);

        // Sound level label
        TextView soundLevelLabel = new TextView(this);
        soundLevelLabel.setText("Sound Level (dB SPL):");
        soundLevelLabel.setTextSize(18);
        layout.addView(soundLevelLabel);

        // Sound level value
        soundLevelText = new TextView(this);
        soundLevelText.setText("0.0");
        soundLevelText.setTextSize(48);
        soundLevelText.setPadding(0, 8, 0, 24);
        layout.addView(soundLevelText);

        // Status text
        statusText = new TextView(this);
        statusText.setText("SILENCE");
        statusText.setTextSize(32);
        statusText.setPadding(0, 8, 0, 32);
        layout.addView(statusText);

        // Threshold label
        TextView thresholdLabel = new TextView(this);
        thresholdLabel.setText("Threshold (dB):");
        thresholdLabel.setTextSize(18);
        layout.addView(thresholdLabel);

        // Threshold value
        thresholdText = new TextView(this);
        thresholdText.setText(String.format("%.1f", threshold));
        thresholdText.setTextSize(24);
        thresholdText.setPadding(0, 8, 0, 8);
        layout.addView(thresholdText);

        // Threshold seekbar
        thresholdSeekBar = new SeekBar(this);
        thresholdSeekBar.setMax(100);
        thresholdSeekBar.setProgress((int)(threshold + 100));
        thresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold = progress - 100.0;
                thresholdText.setText(String.format("%.1f", threshold));
                if (silenceDetector != null) {
                    silenceDetector.setThreshold(threshold);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        layout.addView(thresholdSeekBar);

        // Start button
        startButton = new Button(this);
        startButton.setText("Start Detection");
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSoundDetection();
            }
        });
        android.widget.LinearLayout.LayoutParams startButtonParams = 
            new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            );
        startButtonParams.setMargins(0, 24, 0, 8);
        startButton.setLayoutParams(startButtonParams);
        layout.addView(startButton);

        // Stop button
        stopButton = new Button(this);
        stopButton.setText("Stop Detection");
        stopButton.setEnabled(false);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSoundDetection();
            }
        });
        layout.addView(stopButton);

        setContentView(layout);
    }

    private void requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
        }
    }

    private void startSoundDetection() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestMicrophonePermission();
            return;
        }

        final int sampleRate = 22050;
        final int bufferSize = 1024;
        final int overlap = 0;

        dispatcher = new MicrophoneAudioDispatcher(sampleRate, bufferSize, overlap);

        // Create silence detector
        silenceDetector = new SilenceDetector(threshold, false);

        // Create audio processor to display sound level
        AudioProcessor soundLevelProcessor = new AudioProcessor() {
            @Override
            public boolean process(AudioEvent audioEvent) {
                final double currentSoundLevel = silenceDetector.currentSPL(audioEvent);
                final boolean isSilence = audioEvent.isSilence(threshold);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        soundLevelText.setText(String.format("%.2f", currentSoundLevel));
                        if (isSilence) {
                            statusText.setText("SILENCE");
                            statusText.setTextColor(0xFF666666);
                        } else {
                            statusText.setText("SOUND DETECTED");
                            statusText.setTextColor(0xFF00AA00);
                        }
                    }
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

        // Start the audio processing in a background thread
        audioThread = new Thread(dispatcher, "Audio Dispatcher");
        audioThread.start();

        // Update UI
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        thresholdSeekBar.setEnabled(true);
    }

    private void stopSoundDetection() {
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

        // Update UI
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        soundLevelText.setText("0.0");
        statusText.setText("SILENCE");
        statusText.setTextColor(0xFF666666);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSoundDetection();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                soundLevelText.setText("Permission denied");
            }
        }
    }
}
