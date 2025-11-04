package be.hogent.tarsos.dsp.example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import be.hogent.tarsos.dsp.AudioDispatcher;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioProcessor;
import be.hogent.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;
import be.hogent.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

/**
 * MainActivity demonstrates real-time pitch detection using TarsosDSP on Android.
 * 
 * @author Joren Six
 */
public class MainActivity extends AppCompatActivity {
    
    private static final int AUDIO_PERMISSION_REQUEST = 1;
    
    private AudioDispatcher dispatcher;
    private Thread audioThread;
    private boolean isListening = false;
    
    private TextView frequencyTextView;
    private TextView noteTextView;
    private TextView probabilityTextView;
    private ProgressBar probabilityProgressBar;
    private Button toggleButton;
    
    private Handler mainHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize views
        frequencyTextView = findViewById(R.id.frequencyTextView);
        noteTextView = findViewById(R.id.noteTextView);
        probabilityTextView = findViewById(R.id.probabilityTextView);
        probabilityProgressBar = findViewById(R.id.probabilityProgressBar);
        toggleButton = findViewById(R.id.toggleButton);
        
        // Set up button click listener
        toggleButton.setOnClickListener(v -> {
            if (isListening) {
                stopListening();
            } else {
                startListening();
            }
        });
    }
    
    private void startListening() {
        // Check for audio permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    AUDIO_PERMISSION_REQUEST);
            return;
        }
        
        try {
            // Create audio dispatcher
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
            
            // Create pitch detection handler
            PitchDetectionHandler pdh = new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                    final float pitchInHz = result.getPitch();
                    final float probability = result.getProbability();
                    final boolean isPitched = result.isPitched();
                    
                    // Update UI on main thread
                    mainHandler.post(() -> updatePitchUI(pitchInHz, probability, isPitched));
                }
            };
            
            // Add pitch processor (using FFT_YIN algorithm)
            AudioProcessor pitchProcessor = new PitchProcessor(
                    PitchEstimationAlgorithm.FFT_YIN, 
                    22050, 
                    1024, 
                    pdh
            );
            dispatcher.addAudioProcessor(pitchProcessor);
            
            // Start audio processing in background thread
            audioThread = new Thread(dispatcher, "Audio Dispatcher Thread");
            audioThread.start();
            
            isListening = true;
            toggleButton.setText(R.string.stop_listening);
            
            Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void stopListening() {
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
        
        isListening = false;
        toggleButton.setText(R.string.start_listening);
        
        // Reset UI
        frequencyTextView.setText(R.string.no_pitch);
        noteTextView.setText(R.string.no_pitch);
        probabilityTextView.setText("0%");
        probabilityProgressBar.setProgress(0);
        
        Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
    }
    
    private void updatePitchUI(float pitchInHz, float probability, boolean isPitched) {
        if (isPitched && pitchInHz > 0) {
            // Update frequency
            frequencyTextView.setText(String.format("%.2f Hz", pitchInHz));
            
            // Convert pitch to note name
            String noteName = pitchToNote(pitchInHz);
            noteTextView.setText(noteName);
            
            // Update probability
            int probabilityPercent = (int) (probability * 100);
            probabilityTextView.setText(probabilityPercent + "%");
            probabilityProgressBar.setProgress(probabilityPercent);
        } else {
            frequencyTextView.setText(R.string.no_pitch);
            noteTextView.setText(R.string.no_pitch);
            probabilityTextView.setText("0%");
            probabilityProgressBar.setProgress(0);
        }
    }
    
    /**
     * Convert a frequency in Hz to a musical note name
     */
    private String pitchToNote(float frequency) {
        final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        
        // Calculate MIDI note number
        double noteNumber = 12 * (Math.log(frequency / 440.0) / Math.log(2)) + 69;
        int midiNote = (int) Math.round(noteNumber);
        
        // Get note name and octave
        int noteIndex = midiNote % 12;
        int octave = (midiNote / 12) - 1;
        
        return NOTE_NAMES[noteIndex] + octave;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == AUDIO_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListening();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (isListening) {
            stopListening();
        }
    }
}
