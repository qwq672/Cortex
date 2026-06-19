package awa.qwq672.cortex.DataCollect;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RecordManager {
    private static boolean recording = false;
    private static Set<UUID> recordingPlayers = new HashSet<>();
    private static BufferedWriter writer = null;
    private static Path currentOutputPath = null;
    private static int currentMode = 2;

    public static void startRecording(String filename) {
        if (recording) return;
        try {
            Path dir = Paths.get("cortex_data");
            if (!Files.exists(dir)) Files.createDirectories(dir);
            currentOutputPath = dir.resolve(filename + ".jsonl");
            writer = new BufferedWriter(new FileWriter(currentOutputPath.toFile()));
            recording = true;
            CortexDataCollect.LOGGER.info("Recording to {}", currentOutputPath);
        } catch (IOException e) {
            CortexDataCollect.LOGGER.error("Failed to start recording", e);
        }
    }

    public static void stopRecording() {
        if (!recording) return;
        try {
            if (writer != null) writer.close();
        } catch (IOException e) {
            CortexDataCollect.LOGGER.error("Error closing file", e);
        }
        recording = false;
        recordingPlayers.clear();
        CortexDataCollect.LOGGER.info("Stopped recording");
    }

    public static boolean isRecording() { return recording; }
    public static void addRecordingPlayer(UUID uuid) { recordingPlayers.add(uuid); }
    public static void removeRecordingPlayer(UUID uuid) { recordingPlayers.remove(uuid); }
    public static boolean isRecordingPlayer(UUID uuid) { return recordingPlayers.contains(uuid); }
    public static int getRecordingPlayerCount() { return recordingPlayers.size(); }
    public static void setMode(int mode) { currentMode = mode; }
    public static int getMode() { return currentMode; }

    public static void writeLine(String jsonLine) {
        if (!recording || writer == null) return;
        try {
            writer.write(jsonLine);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            CortexDataCollect.LOGGER.error("Failed write", e);
        }
    }
}