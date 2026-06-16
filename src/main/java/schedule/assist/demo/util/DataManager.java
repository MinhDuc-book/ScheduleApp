package schedule.assist.demo.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import schedule.assist.demo.model.TaskModel;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    public static boolean recoveredFromCorrupt = false;
    private static String FILE_PATH = resolveDefaultFilePath();

    private static String resolveDefaultFilePath() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isEmpty()) {
                return new File(appData, "ScheduleAssistant/my_tasks.json").getAbsolutePath();
            }
        }
        String userHome = System.getProperty("user.home");
        return new File(userHome, ".schedule-assistant/my_tasks.json").getAbsolutePath();
    }

    // Setter for testing
    static void setFilePath(String path) {
        FILE_PATH = path;
    }
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveTasks(List<TaskModel> taskList) {
        if (recoveredFromCorrupt && (taskList == null || taskList.isEmpty())) {
            System.out.println("Skipping save: recovered from corrupt JSON and task list is empty.");
            return;
        }

        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            boolean created = parent.mkdirs();
            if (!created) {
                System.err.println("Failed to create directory: " + parent.getAbsolutePath());
            }
        }

        // Write to temp file first to avoid truncating the original on serialization failure
        File tempFile = new File(FILE_PATH + ".tmp");
        try (FileWriter writer = new FileWriter(tempFile)) {
            gson.toJson(taskList, writer);
        } catch (Exception e) {
            e.printStackTrace();
            tempFile.delete();
            return;
        }

        // Replace original with temp file
        if (file.exists() && !file.delete()) {
            System.err.println("Failed to delete original file: " + file.getAbsolutePath());
            tempFile.delete();
            return;
        }
        if (tempFile.renameTo(file)) {
            if (taskList != null && !taskList.isEmpty()) {
                recoveredFromCorrupt = false;
            }
            System.out.println("Đã lưu dữ liệu thành công!");
        } else {
            System.err.println("Failed to rename temp file to: " + file.getAbsolutePath());
            tempFile.delete();
        }
    }

    // 2. HÀM ĐỌC DỮ LIỆU
    public static List<TaskModel> loadTasks() {
        File file = new File(FILE_PATH);

        // File không tồn tại hoặc rỗng → trả về list trống, chạy mặc định
        if (!file.exists() || file.length() == 0) return new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<TaskModel>>(){}.getType();
            List<TaskModel> result = gson.fromJson(reader, listType);

            // Parse ra null (JSON hỏng) → throw exception to trigger backup
            if (result == null) {
                throw new Exception("JSON parsed to null (possibly malformed).");
            }
            return result;

        } catch (Exception e) {  // đổi IOException → Exception để bắt cả JsonSyntaxException
            e.printStackTrace();
            System.err.println("Error reading JSON file. Backing up corrupted file.");
            backupCorruptFile(file);
            return new ArrayList<>();
        }
    }

    private static void backupCorruptFile(File corruptFile) {
        recoveredFromCorrupt = true;
        if (!corruptFile.exists()) return;

        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File backupFile = new File(corruptFile.getAbsolutePath() + ".bak." + timestamp);

        int counter = 1;
        while (backupFile.exists()) {
            backupFile = new File(corruptFile.getAbsolutePath() + ".bak." + timestamp + "_" + counter);
            counter++;
        }

        boolean renamed = corruptFile.renameTo(backupFile);
        if (renamed) {
            System.err.println("Corrupt file backed up to: " + backupFile.getName());
        } else {
            System.err.println("Failed to backup corrupt file!");
        }
    }
}