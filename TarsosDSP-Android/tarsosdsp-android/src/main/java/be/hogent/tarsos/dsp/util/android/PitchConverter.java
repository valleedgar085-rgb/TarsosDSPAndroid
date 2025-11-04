package be.hogent.tarsos.dsp.util.android;

/**
 * Utility class for converting between pitch (frequency), MIDI notes, and note names.
 * 
 * @author Joren Six
 */
public class PitchConverter {
    
    private static final String[] NOTE_NAMES = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };
    
    private static final String[] NOTE_NAMES_FLAT = {
        "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"
    };
    
    /**
     * Convert frequency in Hz to MIDI note number
     * 
     * @param frequency The frequency in Hz
     * @return The MIDI note number
     */
    public static int frequencyToMidiNote(float frequency) {
        if (frequency <= 0) {
            return -1;
        }
        double noteNumber = 12 * (Math.log(frequency / 440.0) / Math.log(2)) + 69;
        return (int) Math.round(noteNumber);
    }
    
    /**
     * Convert MIDI note number to frequency in Hz
     * 
     * @param midiNote The MIDI note number (0-127)
     * @return The frequency in Hz
     */
    public static float midiNoteToFrequency(int midiNote) {
        return (float) (440.0 * Math.pow(2, (midiNote - 69) / 12.0));
    }
    
    /**
     * Convert frequency to note name with octave
     * 
     * @param frequency The frequency in Hz
     * @return The note name (e.g., "A4", "C#5")
     */
    public static String frequencyToNoteName(float frequency) {
        return frequencyToNoteName(frequency, false);
    }
    
    /**
     * Convert frequency to note name with octave
     * 
     * @param frequency The frequency in Hz
     * @param useFlats Use flat notation instead of sharps
     * @return The note name (e.g., "A4", "Db5")
     */
    public static String frequencyToNoteName(float frequency, boolean useFlats) {
        int midiNote = frequencyToMidiNote(frequency);
        if (midiNote < 0) {
            return "---";
        }
        return midiNoteToNoteName(midiNote, useFlats);
    }
    
    /**
     * Convert MIDI note to note name with octave
     * 
     * @param midiNote The MIDI note number (0-127)
     * @return The note name (e.g., "A4", "C#5")
     */
    public static String midiNoteToNoteName(int midiNote) {
        return midiNoteToNoteName(midiNote, false);
    }
    
    /**
     * Convert MIDI note to note name with octave
     * 
     * @param midiNote The MIDI note number (0-127)
     * @param useFlats Use flat notation instead of sharps
     * @return The note name (e.g., "A4", "Db5")
     */
    public static String midiNoteToNoteName(int midiNote, boolean useFlats) {
        if (midiNote < 0 || midiNote > 127) {
            return "---";
        }
        String[] names = useFlats ? NOTE_NAMES_FLAT : NOTE_NAMES;
        int noteIndex = midiNote % 12;
        int octave = (midiNote / 12) - 1;
        return names[noteIndex] + octave;
    }
    
    /**
     * Get the note name without octave
     * 
     * @param frequency The frequency in Hz
     * @return The note name without octave (e.g., "A", "C#")
     */
    public static String frequencyToNoteNameOnly(float frequency) {
        return frequencyToNoteNameOnly(frequency, false);
    }
    
    /**
     * Get the note name without octave
     * 
     * @param frequency The frequency in Hz
     * @param useFlats Use flat notation instead of sharps
     * @return The note name without octave (e.g., "A", "Db")
     */
    public static String frequencyToNoteNameOnly(float frequency, boolean useFlats) {
        int midiNote = frequencyToMidiNote(frequency);
        if (midiNote < 0) {
            return "---";
        }
        String[] names = useFlats ? NOTE_NAMES_FLAT : NOTE_NAMES;
        return names[midiNote % 12];
    }
    
    /**
     * Get the octave number for a frequency
     * 
     * @param frequency The frequency in Hz
     * @return The octave number
     */
    public static int frequencyToOctave(float frequency) {
        int midiNote = frequencyToMidiNote(frequency);
        if (midiNote < 0) {
            return -1;
        }
        return (midiNote / 12) - 1;
    }
    
    /**
     * Calculate the cents deviation from the nearest note
     * 
     * @param frequency The frequency in Hz
     * @return The cents deviation (positive = sharp, negative = flat)
     */
    public static double getCentsDeviation(float frequency) {
        if (frequency <= 0) {
            return 0;
        }
        int midiNote = frequencyToMidiNote(frequency);
        float targetFrequency = midiNoteToFrequency(midiNote);
        return 1200 * Math.log(frequency / targetFrequency) / Math.log(2);
    }
    
    /**
     * Check if a frequency is close to a musical note
     * 
     * @param frequency The frequency in Hz
     * @param toleranceCents The tolerance in cents (e.g., 50 cents = quarter tone)
     * @return true if frequency is within tolerance of a note, false otherwise
     */
    public static boolean isInTune(float frequency, double toleranceCents) {
        return Math.abs(getCentsDeviation(frequency)) <= toleranceCents;
    }
    
    /**
     * Get a formatted string showing the cents deviation
     * 
     * @param frequency The frequency in Hz
     * @return A formatted string (e.g., "+23¢", "-10¢")
     */
    public static String getCentsDeviationString(float frequency) {
        double cents = getCentsDeviation(frequency);
        String sign = cents >= 0 ? "+" : "";
        return String.format("%s%d¢", sign, (int) Math.round(cents));
    }
}
