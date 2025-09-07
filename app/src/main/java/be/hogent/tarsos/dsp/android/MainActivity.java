package be.hogent.tarsos.dsp.android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher;
import be.hogent.tarsos.dsp.SilenceDetector;
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;
    
    private MicrophoneAudioDispatcher dispatcher;
    private boolean isRecording = false;
    
    private Button pitchDetectorButton;
    private Button soundDetectorButton;
    private TextView pitchResultText;
    private TextView soundResultText;
    private TextView statusText;
    
    private boolean isPitchDetectionActive = false;
    private boolean isSoundDetectionActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();
        
        // Check for microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
        }
    }

    private void initializeViews() {
        pitchDetectorButton = findViewById(R.id.pitchDetectorButton);
        soundDetectorButton = findViewById(R.id.soundDetectorButton);
        pitchResultText = findViewById(R.id.pitchResultText);
        soundResultText = findViewById(R.id.soundResultText);
        statusText = findViewById(R.id.statusText);
    }

    private void setupClickListeners() {
        pitchDetectorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePitchDetection();
            }
        });

        soundDetectorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSoundDetection();
            }
        });
    }

    private void togglePitchDetection() {
        if (checkAudioPermission()) {
            if (!isPitchDetectionActive) {
                startPitchDetection();
            } else {
                stopPitchDetection();
            }
        }
    }

    private void toggleSoundDetection() {
        if (checkAudioPermission()) {
            if (!isSoundDetectionActive) {
                startSoundDetection();
            } else {
                stopSoundDetection();
            }
        }
    }

    private void startPitchDetection() {
        try {
            stopAllDetection(); // Stop any existing detection
            
            dispatcher = new MicrophoneAudioDispatcher(22050, 1024, 512);
            
            PitchProcessor.PitchEstimationAlgorithm algorithm = PitchProcessor.PitchEstimationAlgorithm.YIN;
            PitchProcessor pitchProcessor = new PitchProcessor(algorithm, 22050, 1024, new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                    final float pitchInHz = pitchDetectionResult.getPitch();
                    final float probability = pitchDetectionResult.getProbability();
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (pitchInHz > 0 && probability > 0.9) {
                                pitchResultText.setText(String.format("Pitch: %.2f Hz (%.1f%% confidence)", pitchInHz, probability * 100));
                            } else {
                                pitchResultText.setText(getString(R.string.no_pitch_detected));
                            }
                        }
                    });
                }
            });

            dispatcher.addAudioProcessor(pitchProcessor);
            
            Thread audioThread = new Thread(dispatcher, "Audio Thread");
            audioThread.start();
            
            isPitchDetectionActive = true;
            pitchDetectorButton.setText("Stop Pitch Detection");
            statusText.setText("Pitch detection active");
            
        } catch (Exception e) {
            Toast.makeText(this, "Error starting pitch detection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void startSoundDetection() {
        try {
            stopAllDetection(); // Stop any existing detection
            
            dispatcher = new MicrophoneAudioDispatcher(22050, 1024, 512);
            
            SilenceDetector silenceDetector = new SilenceDetector() {
                @Override
                public boolean process(AudioEvent audioEvent) {
                    super.process(audioEvent);
                    final double currentSPL = super.currentSPL();
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            soundResultText.setText(String.format("Sound Level: %.1f dB", currentSPL));
                        }
                    });
                    return true;
                }
            };

            dispatcher.addAudioProcessor(silenceDetector);
            
            Thread audioThread = new Thread(dispatcher, "Audio Thread");
            audioThread.start();
            
            isSoundDetectionActive = true;
            soundDetectorButton.setText("Stop Sound Detection");
            statusText.setText("Sound detection active");
            
        } catch (Exception e) {
            Toast.makeText(this, "Error starting sound detection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void stopPitchDetection() {
        isPitchDetectionActive = false;
        pitchDetectorButton.setText("Start Pitch Detection");
        pitchResultText.setText(getString(R.string.no_pitch_detected));
        stopDispatcher();
    }

    private void stopSoundDetection() {
        isSoundDetectionActive = false;
        soundDetectorButton.setText("Start Sound Detection");
        soundResultText.setText("Sound Level: 0.0 dB");
        stopDispatcher();
    }

    private void stopAllDetection() {
        if (isPitchDetectionActive) {
            stopPitchDetection();
        }
        if (isSoundDetectionActive) {
            stopSoundDetection();
        }
    }

    private void stopDispatcher() {
        if (dispatcher != null) {
            dispatcher.stop();
            dispatcher = null;
        }
        if (!isPitchDetectionActive && !isSoundDetectionActive) {
            statusText.setText("Ready");
        }
    }

    private boolean checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAllDetection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAllDetection();
    }
}