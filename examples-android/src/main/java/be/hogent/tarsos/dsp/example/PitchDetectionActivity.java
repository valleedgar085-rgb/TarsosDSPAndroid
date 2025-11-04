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
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher;
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;
import be.hogent.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

/**
 * Example Android Activity that demonstrates real-time pitch detection
 * using TarsosDSP on Android.
 * 
 * This activity captures audio from the microphone and displays the
 * detected pitch in real-time.
 * 
 * @author Joren Six
 */
public class PitchDetectionActivity extends Activity {

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;

    private TextView pitchText;
    private TextView probabilityText;
    private Button startButton;
    private Button stopButton;

    private MicrophoneAudioDispatcher dispatcher;
    private Thread audioThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create UI programmatically (alternatively, use XML layout)
        createUI();

        // Request microphone permission
        requestMicrophonePermission();
    }

    private void createUI() {
        // Create a simple vertical layout programmatically
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // Title
        TextView title = new TextView(this);
        title.setText("TarsosDSP Pitch Detection");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 32);
        layout.addView(title);

        // Pitch label
        TextView pitchLabel = new TextView(this);
        pitchLabel.setText("Pitch (Hz):");
        pitchLabel.setTextSize(18);
        layout.addView(pitchLabel);

        // Pitch value
        pitchText = new TextView(this);
        pitchText.setText("0.0");
        pitchText.setTextSize(48);
        pitchText.setPadding(0, 8, 0, 24);
        layout.addView(pitchText);

        // Probability label
        TextView probabilityLabel = new TextView(this);
        probabilityLabel.setText("Probability:");
        probabilityLabel.setTextSize(18);
        layout.addView(probabilityLabel);

        // Probability value
        probabilityText = new TextView(this);
        probabilityText.setText("0.0");
        probabilityText.setTextSize(32);
        probabilityText.setPadding(0, 8, 0, 32);
        layout.addView(probabilityText);

        // Start button
        startButton = new Button(this);
        startButton.setText("Start Detection");
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPitchDetection();
            }
        });
        layout.addView(startButton);

        // Stop button
        stopButton = new Button(this);
        stopButton.setText("Stop Detection");
        stopButton.setEnabled(false);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPitchDetection();
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

    private void startPitchDetection() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestMicrophonePermission();
            return;
        }

        // Create a new MicrophoneAudioDispatcher
        final int sampleRate = 22050;
        final int bufferSize = 1024;
        final int overlap = 0;

        dispatcher = new MicrophoneAudioDispatcher(sampleRate, bufferSize, overlap);

        // Create a pitch detection handler
        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
                final float probability = result.getProbability();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pitchInHz > 0) {
                            pitchText.setText(String.format("%.2f", pitchInHz));
                            probabilityText.setText(String.format("%.2f", probability));
                        } else {
                            pitchText.setText("--");
                            probabilityText.setText("--");
                        }
                    }
                });
            }
        };

        // Create a pitch processor
        AudioProcessor pitchProcessor = new PitchProcessor(
                PitchEstimationAlgorithm.FFT_YIN, 
                sampleRate, 
                bufferSize, 
                pdh
        );

        dispatcher.addAudioProcessor(pitchProcessor);

        // Start the audio processing in a background thread
        audioThread = new Thread(dispatcher, "Audio Dispatcher");
        audioThread.start();

        // Update UI
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
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

        // Update UI
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        pitchText.setText("0.0");
        probabilityText.setText("0.0");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPitchDetection();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now start detection
            } else {
                // Permission denied
                pitchText.setText("Permission denied");
            }
        }
    }
}
