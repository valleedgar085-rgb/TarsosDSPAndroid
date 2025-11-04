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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Main launcher activity that shows a list of TarsosDSP example applications.
 * 
 * @author Joren Six
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        createUI();
    }

    private void createUI() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // Title
        TextView title = new TextView(this);
        title.setText("TarsosDSP for Android");
        title.setTextSize(28);
        title.setPadding(0, 0, 0, 16);
        layout.addView(title);

        // Description
        TextView description = new TextView(this);
        description.setText("Audio Processing Examples\n\nSelect an example to get started:");
        description.setTextSize(16);
        description.setPadding(0, 0, 0, 32);
        layout.addView(description);

        // Pitch Detection Example
        Button pitchDetectionButton = createExampleButton(
            "Pitch Detection",
            "Real-time pitch detection from microphone",
            PitchDetectionActivity.class
        );
        layout.addView(pitchDetectionButton);

        // Sound Detector Example
        Button soundDetectorButton = createExampleButton(
            "Sound Detector",
            "Detect sound vs silence with adjustable threshold",
            SoundDetectorActivity.class
        );
        layout.addView(soundDetectorButton);

        // Audio Player Example
        Button audioPlayerButton = createExampleButton(
            "Audio Player",
            "Play audio files with adjustable gain",
            AudioPlayerActivity.class
        );
        layout.addView(audioPlayerButton);

        // Info text
        TextView infoText = new TextView(this);
        infoText.setText("\n\nTarsosDSP is a Java library for audio processing.\n\n" +
                "For more information visit:\nhttp://0110.be/tag/TarsosDSP");
        infoText.setTextSize(12);
        infoText.setPadding(0, 32, 0, 0);
        layout.addView(infoText);

        setContentView(layout);
    }

    private Button createExampleButton(final String title, String description, 
            final Class<?> activityClass) {
        Button button = new Button(this);
        button.setText(title + "\n" + description);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setPadding(16, 24, 16, 24);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        button.setLayoutParams(params);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, activityClass);
                startActivity(intent);
            }
        });

        return button;
    }
}
