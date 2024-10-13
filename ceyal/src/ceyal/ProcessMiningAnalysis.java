package ceyal;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class ProcessMiningAnalysis {
    private static final String[] EVENTS = {"Task A", "Task B", "Task C", "Task D"};
    private static final String[] RESOURCES = {"User1", "User2", "User3", "User4"};
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        String filename = "sample_event_log.csv";
        generateEventLog(filename);
    }

    private static void generateEventLog(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Event,Timestamp,Resource,Cost,Duration\n"); // Header

            for (int i = 0; i < 50; i++) { // Generate 50 random events
                String event = EVENTS[RANDOM.nextInt(EVENTS.length)];
                String timestamp = generateRandomTimestamp();
                String resource = RESOURCES[RANDOM.nextInt(RESOURCES.length)];
                double cost = RANDOM.nextDouble() * 100; // Cost between 0 and 100
                double duration = 1 + RANDOM.nextDouble() * 9; // Duration between 1 and 10

                String logEntry = String.join(",", event, timestamp, resource, String.format("%.2f", cost), String.format("%.2f", duration));
                writer.write(logEntry);
                writer.newLine();
            }

            System.out.println("Event log generated: " + filename);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    private static String generateRandomTimestamp() {
        LocalDateTime start = LocalDateTime.now().minusDays(30); // Start date 30 days ago
        LocalDateTime end = LocalDateTime.now();
        long secondsBetween = java.time.Duration.between(start, end).toSeconds();
        long randomSeconds = RANDOM.nextLong() % secondsBetween;
        LocalDateTime randomDateTime = start.plusSeconds(randomSeconds);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return randomDateTime.format(formatter);
    }
}
