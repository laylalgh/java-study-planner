# Study Planner

A Java command-line app for keeping track of coursework deadlines. Add a task with its course, title, and due date, then mark it complete when you finish it. Open tasks appear first, ordered by deadline, and past deadlines are flagged.

## Run locally

From the project folder with JDK 17 or newer installed:

```sh
java StudyPlanner.java
```

Choose **2** to add an assignment, **1** to list assignments, or **3** to complete one by its number. Example coursework to try is `Operating Systems`, `Review paging`, and `2027-01-10`.

Tasks are stored locally in a tab-separated file named `tasks.tsv`, so they stay available between runs.

## Java concepts

- A record represents each task.
- An `ArrayList<Task>` stores tasks, and a `Comparator` orders them by status and date.
- `LocalDate` validates dates and identifies overdue tasks.
- Java file I/O saves and reloads tasks without external dependencies.
