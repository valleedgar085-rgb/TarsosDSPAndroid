package be.hogent.tarsos.dsp.app;

import be.hogent.tarsos.dsp.AudioFormat;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.pitch.PitchDetector;
import be.hogent.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import be.hogent.tarsos.dsp.util.AudioFloatConverter;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Simple command-line entry point that demonstrates pitch detection with the TarsosDSP core.
 *
 * <p>The CLI supports the following commands:
 * <ul>
 *     <li>{@code list-algorithms} – show the available pitch detection algorithms.</li>
 *     <li>{@code detect <audio-file> [algorithm]} – run pitch detection on a WAV/AIFF file
 *     (defaults to the {@code YIN} algorithm).</li>
 * </ul>
 */
public final class PitchDetectCli {

    private static final int DEFAULT_BUFFER_SIZE = 2048;
    private static final DecimalFormat HERTZ_FORMAT = new DecimalFormat("0.00");
    private static final DecimalFormat PROBABILITY_FORMAT = new DecimalFormat("0.000");

    private PitchDetectCli() {
    }

    public static void main(String[] args) {
        if (args.length == 0 || "--help".equalsIgnoreCase(args[0]) || "-h".equalsIgnoreCase(args[0])) {
            printHelp();
            return;
        }

        try {
            switch (args[0]) {
                case "list-algorithms":
                    listAlgorithms();
                    break;
                case "detect":
                    runDetectionCommand(args);
                    break;
                default:
                    System.err.println("Unknown command: " + args[0]);
                    printHelp();
            }
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
            if (Boolean.getBoolean("tarsos.cli.debug")) {
                ex.printStackTrace(System.err);
            }
            System.exit(1);
        }
    }

    private static void runDetectionCommand(String[] args) throws IOException, UnsupportedAudioFileException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: detect <audio-file> [algorithm]");
        }

        Path audioPath = resolveAudioPath(args[1]);
        if (!Files.isRegularFile(audioPath)) {
            throw new IllegalArgumentException("Audio file not found: " + audioPath);
        }

        PitchEstimationAlgorithm algorithm = determineAlgorithm(args);
        detectPitch(audioPath, algorithm);
    }

    private static PitchEstimationAlgorithm determineAlgorithm(String[] args) {
        if (args.length < 3) {
            return PitchEstimationAlgorithm.YIN;
        }

        String requested = args[2].toUpperCase(Locale.ROOT);
        for (PitchEstimationAlgorithm candidate : PitchEstimationAlgorithm.values()) {
            if (candidate.name().equals(requested)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Unknown algorithm '" + args[2] + "'. Try list-algorithms.");
    }

    private static void detectPitch(Path audioPath, PitchEstimationAlgorithm algorithm)
            throws IOException, UnsupportedAudioFileException {

        try (AudioInputStream sourceStream = AudioSystem.getAudioInputStream(audioPath.toFile());
             AudioInputStream pcmStream = convertToPcm(sourceStream)) {

            javax.sound.sampled.AudioFormat javaFormat = pcmStream.getFormat();
            AudioFormat dspFormat = toTarsosFormat(javaFormat);
            AudioFloatConverter converter = AudioFloatConverter.getConverter(dspFormat);
            if (converter == null) {
                throw new IllegalStateException("Unsupported audio format: " + javaFormat);
            }

            int bufferSize = DEFAULT_BUFFER_SIZE;
            int frameSize = dspFormat.getFrameSize();
            byte[] byteBuffer = new byte[bufferSize * frameSize];
            float[] floatBuffer = new float[bufferSize];

            PitchDetector detector = algorithm.getDetector(dspFormat.getSampleRate(), bufferSize);
            long framesProcessed = 0;
            List<Double> detectedPitches = new ArrayList<>();

            int bytesRead;
            while ((bytesRead = pcmStream.read(byteBuffer)) != -1) {
                int framesRead = bytesRead / frameSize;
                if (framesRead <= 0) {
                    continue;
                }

                Arrays.fill(floatBuffer, 0f);
                converter.toFloatArray(byteBuffer, 0, floatBuffer, 0, framesRead);

                PitchDetectionResult result = detector.getPitch(floatBuffer);
                double timeSeconds = framesProcessed / dspFormat.getSampleRate();

                if (result != null && result.getPitch() > 0) {
                    detectedPitches.add((double) result.getPitch());
                    System.out.println(formatDetectionLine(timeSeconds, result));
                }

                framesProcessed += framesRead;
            }

            System.out.println();
            if (detectedPitches.isEmpty()) {
                System.out.println("No pitch detected. Try a monophonic sample with clear pitch.");
            } else {
                double summaryPitch = median(detectedPitches);
                System.out.printf(Locale.US, "Summary (algorithm=%s): median pitch %s Hz across %d frames.%n",
                        algorithm.name(), HERTZ_FORMAT.format(summaryPitch), detectedPitches.size());
            }
        }
    }

    private static String formatDetectionLine(double timeSeconds, PitchDetectionResult result) {
        String pitch = HERTZ_FORMAT.format(result.getPitch());
        String probability = PROBABILITY_FORMAT.format(result.getProbability());
        return String.format(Locale.US, "%6.3f s | %8s Hz | probability %s", timeSeconds, pitch, probability);
    }

    private static double median(Collection<Double> values) {
        double[] sorted = values.stream().mapToDouble(Double::doubleValue).sorted().toArray();
        if (sorted.length == 0) {
            return Double.NaN;
        }
        int middle = sorted.length / 2;
        if (sorted.length % 2 == 0) {
            return (sorted[middle - 1] + sorted[middle]) / 2.0;
        }
        return sorted[middle];
    }

    private static AudioInputStream convertToPcm(AudioInputStream sourceStream) {
        javax.sound.sampled.AudioFormat baseFormat = sourceStream.getFormat();

        if (javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED.equals(baseFormat.getEncoding())
                && baseFormat.getSampleSizeInBits() == 16) {
            return sourceStream;
        }

        javax.sound.sampled.AudioFormat targetFormat = new javax.sound.sampled.AudioFormat(
                javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);

        return AudioSystem.getAudioInputStream(targetFormat, sourceStream);
    }

    private static AudioFormat toTarsosFormat(javax.sound.sampled.AudioFormat javaFormat) {
        AudioFormat.Encoding encoding = mapEncoding(javaFormat.getEncoding());
        return new AudioFormat(encoding,
                javaFormat.getSampleRate(),
                javaFormat.getSampleSizeInBits(),
                javaFormat.getChannels(),
                javaFormat.getFrameSize(),
                javaFormat.getFrameRate(),
                javaFormat.isBigEndian());
    }

    private static AudioFormat.Encoding mapEncoding(javax.sound.sampled.AudioFormat.Encoding encoding) {
        if (javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED.equals(encoding)) {
            return AudioFormat.Encoding.PCM_SIGNED;
        }
        if (javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED.equals(encoding)) {
            return AudioFormat.Encoding.PCM_UNSIGNED;
        }
        if (javax.sound.sampled.AudioFormat.Encoding.ALAW.equals(encoding)) {
            return AudioFormat.Encoding.ALAW;
        }
        if (javax.sound.sampled.AudioFormat.Encoding.ULAW.equals(encoding)) {
            return AudioFormat.Encoding.ULAW;
        }
        return new AudioFormat.Encoding(encoding.toString());
    }

    private static void listAlgorithms() {
        System.out.println("Available pitch detection algorithms:");
        for (PitchEstimationAlgorithm algorithm : PitchEstimationAlgorithm.values()) {
            System.out.println(" - " + algorithm.name());
        }
    }

    private static Path resolveAudioPath(String userInput) {
        Path direct = Path.of(userInput);
        if (Files.isRegularFile(direct)) {
            return direct.toAbsolutePath();
        }

        String pwd = System.getenv("PWD");
        if (pwd != null && !pwd.isBlank()) {
            Path fromPwd = Path.of(pwd).resolve(userInput).normalize();
            if (Files.isRegularFile(fromPwd)) {
                return fromPwd.toAbsolutePath();
            }
        }

        return direct.toAbsolutePath();
    }

    private static void printHelp() {
        System.out.println("TarsosDSP Pitch Detection CLI\n");
        System.out.println("Usage:");
        System.out.println("  java -jar tarsos-dsp-1.7.0.jar list-algorithms");
        System.out.println("  java -jar tarsos-dsp-1.7.0.jar detect <audio-file> [algorithm]\n");
        System.out.println("Examples:");
        System.out.println("  java -jar tarsos-dsp-1.7.0.jar detect ./tests/be/hogent/tarsos/dsp/test/resources/piano.ff.A4.wav");
        System.out.println("  java -jar tarsos-dsp-1.7.0.jar detect $(pwd)/sample.wav MPM\n");
        System.out.println("Set -Dtarsos.cli.debug=true for full stack traces on errors.");
    }
}
