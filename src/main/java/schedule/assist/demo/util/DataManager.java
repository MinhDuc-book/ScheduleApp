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
    // Tên file sẽ được lưu ngay trong thư mục chạy project
    private static final String FILE_PATH = "my_tasks.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // 1. HÀM LƯU DỮ LIỆU
    public static void saveTasks(List<TaskModel> taskList) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            // Gson tự động biến cả cái List thành chuỗi JSON và ghi ra file
            gson.toJson(taskList, writer);
            System.out.println("Đã lưu dữ liệu thành công!");
        } catch (IOException e) {
            e.printStackTrace();
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

            // Parse ra null (JSON hỏng) → trả về list trống
            return result != null ? result : new ArrayList<>();

        } catch (Exception e) {  // đổi IOException → Exception để bắt cả JsonSyntaxException
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}