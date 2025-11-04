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
import android.os.Environment;
import android.widget.TextView;
import android.widget.Button;
import android.widget.SeekBar;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;

import be.hogent.tarsos.dsp.AndroidAudioDispatcher;
import be.hogent.tarsos.dsp.AndroidAudioPlayer;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.AudioFormat;
import be.hogent.tarsos.dsp.AudioProcessor;
import be.hogent.tarsos.dsp.GainProcessor;

/**
 * Example Android Activity that demonstrates audio playback with effects
 * using TarsosDSP on Android.
 * 
 * This activity plays an audio file from storage with adjustable gain.
 * 
 * @author Joren Six
 */
public class AudioPlayerActivity extends Activity {

    private static final int PERMISSION_REQUEST_READ_STORAGE = 1;

    private TextView statusText;
    private TextView progressText;
    private SeekBar gainSeekBar;
    private TextView gainText;
    private Button playButton;
    private Button stopButton;

    private AndroidAudioDispatcher dispatcher;
    private Thread audioThread;
    private GainProcessor gainProcessor;

    private double gain = 1.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create UI programmatically
        createUI();

        // Request storage permission
        requestStoragePermission();
    }

    private void createUI() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // Title
        TextView title = new TextView(this);
        title.setText("TarsosDSP Audio Player");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 32);
        layout.addView(title);

        // Status text
        statusText = new TextView(this);
        statusText.setText("Ready");
        statusText.setTextSize(18);
        statusText.setPadding(0, 0, 0, 16);
        layout.addView(statusText);

        // Progress text
        progressText = new TextView(this);
        progressText.setText("0.0 / 0.0 seconds");
        progressText.setTextSize(18);
        progressText.setPadding(0, 0, 0, 32);
        layout.addView(progressText);

        // Gain label
        TextView gainLabel = new TextView(this);
        gainLabel.setText("Gain:");
        gainLabel.setTextSize(18);
        layout.addView(gainLabel);

        // Gain value
        gainText = new TextView(this);
        gainText.setText(String.format("%.2f", gain));
        gainText.setTextSize(24);
        gainText.setPadding(0, 8, 0, 8);
        layout.addView(gainText);

        // Gain seekbar
        gainSeekBar = new SeekBar(this);
        gainSeekBar.setMax(200);
        gainSeekBar.setProgress((int)(gain * 100));
        gainSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                gain = progress / 100.0;
                gainText.setText(String.format("%.2f", gain));
                if (gainProcessor != null) {
                    gainProcessor.setGain(gain);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        layout.addView(gainSeekBar);

        // Play button
        playButton = new Button(this);
        playButton.setText("Play Audio File");
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudioFile();
            }
        });
        android.widget.LinearLayout.LayoutParams playButtonParams = 
            new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            );
        playButtonParams.setMargins(0, 24, 0, 8);
        playButton.setLayoutParams(playButtonParams);
        layout.addView(playButton);

        // Stop button
        stopButton = new Button(this);
        stopButton.setText("Stop Playback");
        stopButton.setEnabled(false);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayback();
            }
        });
        layout.addView(stopButton);

        // Info text
        TextView infoText = new TextView(this);
        infoText.setText("\nPlace a 16-bit PCM WAV file named 'test.wav' in your Downloads folder to play it.");
        infoText.setTextSize(14);
        infoText.setPadding(0, 32, 0, 0);
        layout.addView(infoText);

        setContentView(layout);
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_STORAGE);
        }
    }

    private void playAudioFile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
            return;
        }

        // Get the audio file
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File audioFile = new File(downloadsDir, "test.wav");

        if (!audioFile.exists()) {
            statusText.setText("Error: test.wav not found in Downloads");
            return;
        }

        try {
            final int bufferSize = 4096;
            final int overlap = 0;

            // Create audio format (16-bit PCM, 44100 Hz, mono)
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);

            // Create dispatcher
            dispatcher = new AndroidAudioDispatcher(audioFile, bufferSize, overlap);

            // Create gain processor
            gainProcessor = new GainProcessor(gain);
            dispatcher.addAudioProcessor(gainProcessor);

            // Create audio player
            AndroidAudioPlayer player = new AndroidAudioPlayer(format);
            dispatcher.addAudioProcessor(player);

            // Create progress updater
            AudioProcessor progressUpdater = new AudioProcessor() {
                @Override
                public boolean process(AudioEvent audioEvent) {
                    final float currentTime = audioEvent.getTimeStamp();
                    final double duration = dispatcher.durationInSeconds();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressText.setText(String.format("%.1f / %.1f seconds", 
                                currentTime, duration));
                        }
                    });

                    return true;
                }

                @Override
                public void processingFinished() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusText.setText("Playback finished");
                            playButton.setEnabled(true);
                            stopButton.setEnabled(false);
                        }
                    });
                }
            };
            dispatcher.addAudioProcessor(progressUpdater);

            // Start playback
            audioThread = new Thread(dispatcher, "Audio Player");
            audioThread.start();

            // Update UI
            statusText.setText("Playing...");
            playButton.setEnabled(false);
            stopButton.setEnabled(true);
            gainSeekBar.setEnabled(true);

        } catch (IOException e) {
            statusText.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopPlayback() {
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
        statusText.setText("Stopped");
        playButton.setEnabled(true);
        stopButton.setEnabled(false);
        progressText.setText("0.0 / 0.0 seconds");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlayback();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                statusText.setText("Storage permission denied");
            }
        }
    }
}
