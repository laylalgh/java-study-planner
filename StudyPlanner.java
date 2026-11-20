import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class StudyPlanner {
    private static final Path SAVE_FILE = Path.of("tasks.tsv");

    record Task(String course, String title, LocalDate due, boolean done) {
        String toRow() {
            return String.join("\t", course, title, due.toString(), Boolean.toString(done));
        }
    }

    public static void main(String[] args) throws IOException {
        List<Task> tasks = loadTasks();

        try (Scanner input = new Scanner(System.in)) {
            System.out.println("Study Planner");
            while (true) {
                tasks.sort(Comparator.comparing(Task::done).thenComparing(Task::due));
                System.out.println("\n1. List tasks  2. Add task  3. Mark complete  0. Exit");
                if (!input.hasNextLine()) {
                    return;
                }
                String choice = input.nextLine().strip();
                if (choice.equals("0")) {
                    return;
                }
                try {
                    switch (choice) {
                        case "1" -> listTasks(tasks);
                        case "2" -> {
                            String course = readField(input, "Course");
                            String title = readField(input, "Task");
                            LocalDate due = LocalDate.parse(readField(input, "Due date (YYYY-MM-DD)"));
                            List<Task> updated = new ArrayList<>(tasks);
                            updated.add(new Task(course, title, due, false));
                            saveTasks(updated);
                            tasks = updated;
                            System.out.println("Task added.");
                        }
                        case "3" -> {
                            listTasks(tasks);
                            if (tasks.isEmpty()) {
                                break;
                            }
                            int index = Integer.parseInt(readField(input, "Task number")) - 1;
                            if (index < 0 || index >= tasks.size()) {
                                throw new IllegalArgumentException("Choose a task number from the list.");
                            }
                            Task task = tasks.get(index);
                            List<Task> updated = new ArrayList<>(tasks);
                            updated.set(index, new Task(task.course(), task.title(), task.due(), true));
                            saveTasks(updated);
                            tasks = updated;
                            System.out.println("Task marked complete.");
                        }
                        default -> System.out.println("Choose 0, 1, 2, or 3.");
                    }
                } catch (DateTimeParseException error) {
                    System.out.println("Enter a valid date in YYYY-MM-DD format.");
                } catch (NumberFormatException error) {
                    System.out.println("Enter a whole task number.");
                } catch (IllegalArgumentException error) {
                    System.out.println(error.getMessage());
                } catch (IOException error) {
                    System.out.println("Could not save tasks. " + error.getMessage());
                }
            }
        }
    }

    private static String readField(Scanner input, String label) {
        System.out.print(label + ": ");
        String value = input.hasNextLine() ? input.nextLine().strip() : "";
        if (value.isEmpty() || value.contains("\t")) {
            throw new IllegalArgumentException(label + " must contain text without tabs.");
        }
        return value;
    }

    private static void listTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("No tasks yet.");
            return;
        }
        LocalDate today = LocalDate.now();
        for (int index = 0; index < tasks.size(); index++) {
            Task task = tasks.get(index);
            String status = task.done() ? "DONE" : task.due().isBefore(today) ? "OVERDUE" : "OPEN";
            System.out.printf("%d. [%s] %s | %s | Due %s%n",
                    index + 1, status, task.course(), task.title(), task.due());
        }
    }

    private static List<Task> loadTasks() throws IOException {
        List<Task> tasks = new ArrayList<>();
        if (Files.notExists(SAVE_FILE)) {
            return tasks;
        }
        for (String row : Files.readAllLines(SAVE_FILE)) {
            String[] fields = row.split("\t", -1);
            if (fields.length != 4 || (!fields[3].equals("true") && !fields[3].equals("false"))) {
                throw new IOException("Invalid task row in " + SAVE_FILE);
            }
            try {
                tasks.add(new Task(fields[0], fields[1], LocalDate.parse(fields[2]),
                        Boolean.parseBoolean(fields[3])));
            } catch (DateTimeParseException error) {
                throw new IOException("Invalid due date in " + SAVE_FILE, error);
            }
        }
        return tasks;
    }

    private static void saveTasks(List<Task> tasks) throws IOException {
        Path nextFile = SAVE_FILE.resolveSibling("tasks.tsv.tmp");
        Files.write(nextFile, tasks.stream().map(Task::toRow).toList());
        Files.move(nextFile, SAVE_FILE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
}
